import os
import cv2
import numpy as np
import torch
import torch.nn as nn
from torchvision import models, transforms
from flask import Blueprint, request, jsonify
from flask_cors import CORS

bp5 = Blueprint('app5', __name__)
CORS(bp5)

# ===========================================================
# 模型定义 (从 main03_2.py 提取)
# ===========================================================
class SEBlock(nn.Module):
    def __init__(self, channel, reduction=16):
        super(SEBlock, self).__init__()
        self.avg_pool = nn.AdaptiveAvgPool2d(1)
        self.fc = nn.Sequential(
            nn.Linear(channel, channel // reduction, bias=False),
            nn.ReLU(inplace=True),
            nn.Linear(channel // reduction, channel, bias=False),
            nn.Sigmoid()
        )
    def forward(self, x):
        b, c, _, _ = x.size()
        y = self.avg_pool(x).view(b, c)
        y = self.fc(y).view(b, c, 1, 1)
        return x * y.expand_as(x)

class HybridResNet(nn.Module):
    def __init__(self):
        super(HybridResNet, self).__init__()
        base_model = models.resnet18(weights=None)
        self.features = nn.Sequential(*list(base_model.children())[:-2])
        self.se_block = SEBlock(512)
        self.avgpool = nn.AdaptiveAvgPool2d((1, 1))
        
        self.group_embed = nn.Embedding(num_embeddings=10, embedding_dim=16)
        
        self.stats_fc = nn.Sequential(
            nn.Linear(216, 128),  
            nn.ReLU(),
            nn.BatchNorm1d(128),
            nn.Linear(128, 64),
            nn.ReLU(),
            nn.BatchNorm1d(64)
        )
        
        self.w_cnn = nn.Parameter(torch.tensor(1.0))
        self.w_stats = nn.Parameter(torch.tensor(2.0))
        
        self.final_regressor = nn.Sequential(
            nn.Linear(512 + 64, 128),
            nn.ReLU(),
            nn.Dropout(0.2),
            nn.Linear(128, 1)
        )

    def forward(self, x, hist_vec, group_id):
        feat_map = self.features(x)
        feat_map = self.se_block(feat_map)
        cnn_feat = self.avgpool(feat_map)
        cnn_feat = torch.flatten(cnn_feat, 1)
        
        mean_stats = torch.mean(x, dim=[2, 3])
        std_stats = torch.std(x, dim=[2, 3])
        mean_a = mean_stats[:, 1:2] 
        mean_b = mean_stats[:, 2:3]
        diff_ab = mean_a - mean_b
        sum_ab  = mean_a + mean_b
        basic_stats = torch.cat([mean_stats, std_stats, diff_ab, sum_ab], dim=1) 
        
        group_feat = self.group_embed(group_id) 
        total_stats = torch.cat([basic_stats, hist_vec, group_feat], dim=1)
        stats_out = self.stats_fc(total_stats)
        
        weighted_cnn = cnn_feat * torch.abs(self.w_cnn)
        weighted_stats = stats_out * torch.abs(self.w_stats)
        combined = torch.cat([weighted_cnn, weighted_stats], dim=1)
        
        out = self.final_regressor(combined)
        return out

# 初始化模型
device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
model = HybridResNet().to(device)
MODEL_PATH = os.path.join(os.path.dirname(os.path.dirname(__file__)), 'magnesium_hybrid_final_model.pth')
# Try to load existing local model if available, else load from current dir
if not os.path.exists(MODEL_PATH):
    MODEL_PATH = 'magnesium_hybrid_final_model.pth'

if os.path.exists(MODEL_PATH):
    try:
        model.load_state_dict(torch.load(MODEL_PATH, map_location=device))
        print(f"✅ 成功加载温度预测模型: {MODEL_PATH}")
    except Exception as e:
        print(f"❌ 加载模型 {MODEL_PATH} 失败: {e}")
else:
    print(f"⚠️ 未找到温度预测模型文件 {MODEL_PATH}，如果在训练中请忽略此消息。")

# 必须调用 eval()，否则 BatchNorm 遇到 batch_size=1 时会报错
model.eval()

# 转换操作
transform = transforms.Compose([
    transforms.ToPILImage(),
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
])

def process_image(image_bytes):
    raw_data = np.frombuffer(image_bytes, np.uint8)
    image_bgr = cv2.imdecode(raw_data, cv2.IMREAD_COLOR)
    if image_bgr is None:
        raise ValueError("Invalid image file")

    image_rgb = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2RGB)
    image_lab = cv2.cvtColor(image_rgb, cv2.COLOR_RGB2Lab)
    l, a, b = cv2.split(image_lab)

    l_median_new = np.median(l) 
    shift = 128.0 - l_median_new
    l_aligned = l.astype(np.float32) + shift
    l_aligned = np.clip(l_aligned, 0, 255).astype(np.uint8)

    a_blur = cv2.GaussianBlur(a, (5, 5), 0)
    b_blur = cv2.GaussianBlur(b, (5, 5), 0)
    image_lab_processed = cv2.merge((l_aligned, a_blur, b_blur))
    
    hist_feats = []
    scales = [16, 32, 48] 
    
    for bins in scales:
        hist_a = cv2.calcHist([a_blur], [0], None, [bins], [0, 256])
        hist_b = cv2.calcHist([b_blur], [0], None, [bins], [0, 256])
        cv2.normalize(hist_a, hist_a, alpha=0, beta=1, norm_type=cv2.NORM_MINMAX)
        cv2.normalize(hist_b, hist_b, alpha=0, beta=1, norm_type=cv2.NORM_MINMAX)
        hist_feats.append(hist_a.flatten())
        hist_feats.append(hist_b.flatten())
        
    hist_feat_np = np.concatenate(hist_feats)
    hist_feat = torch.tensor(hist_feat_np, dtype=torch.float32)

    image_tensor = transform(image_lab_processed)
    return image_tensor, hist_feat

@bp5.route('/predict_temperature', methods=['POST'])
def predict_temperature():
    if 'file' not in request.files:
        return jsonify({'code': 0, 'msg': '没有文件上传', 'error': 'No file part'}), 400
    
    file = request.files['file']
    if file.filename == '':
        return jsonify({'code': 0, 'msg': '没有选择文件', 'error': 'No selected file'}), 400

    try:
        image_bytes = file.read()
        image_tensor, hist_feat = process_image(image_bytes)
        
        inputs = image_tensor.unsqueeze(0).to(device)
        hists = hist_feat.unsqueeze(0).to(device)
        groups = torch.tensor([0], dtype=torch.long).to(device) # Default group_id to 0
        
        with torch.no_grad():
            # TTA x4 保证一致的推理表现
            out1 = model(inputs, hists, groups)
            out2 = model(torch.flip(inputs, [3]), hists, groups)
            out3 = model(torch.flip(inputs, [2]), hists, groups)
            out4 = model(torch.flip(inputs, [2, 3]), hists, groups)
            outputs = (out1 + out2 + out3 + out4) / 4.0
            
            # 反归一化
            pred_real = outputs.item() * 200.0 + 250.0

        return jsonify({'code': 1, 'data': {'temperature': round(pred_real, 2)}, 'msg': "计算成功"}), 200
        
    except Exception as e:
        import traceback
        traceback.print_exc()
        return jsonify({'code': 0, 'error': str(e), 'msg': "计算失败"}), 500
