import os
import random
import time
import copy
import cv2
import numpy as np
import matplotlib.pyplot as plt
from collections import defaultdict

import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import Dataset, DataLoader, Subset
from torchvision import models, transforms
from torch.optim.lr_scheduler import CosineAnnealingLR 

# ===========================================================
# 1. 工具函数定义
# ===========================================================
def setup_seed(seed):
    """固定随机种子"""
    random.seed(seed)
    np.random.seed(seed)
    torch.manual_seed(seed)
    torch.cuda.manual_seed(seed)
    torch.cuda.manual_seed_all(seed)
    torch.backends.cudnn.deterministic = True
    torch.backends.cudnn.benchmark = False
    print(f"✅ Random seed fixed to: {seed}")

# ===========================================================
# 2. 定制化损失函数: Weighted Huber Loss
# ===========================================================
class WeightedHuberLoss(nn.Module):
    def __init__(self, delta=1.0, high_temp_thresh=400.0, high_weight=1.5):
        """
        :param delta: Huber Loss 的阈值 (小于此值用MSE，大于用MAE)
        :param high_temp_thresh: 高温阈值，超过此温度加大惩罚
        :param high_weight: 高温样本的权重系数
        """
        super().__init__()
        self.hub_loss = nn.HuberLoss(delta=delta, reduction='none')
        self.high_temp_thresh = high_temp_thresh
        self.high_weight = high_weight

    def forward(self, preds, labels):
        # 1. 计算基础 Loss
        loss = self.hub_loss(preds, labels)
        
        # 2. 还原真实温度 (根据 Dataset 的归一化逻辑反推)
        # Label = (Temp - 250) / 200  => Temp = Label * 200 + 250
        # 注意: 这里的 labels 已经在 GPU 上
        real_temps = labels * 200.0 + 250.0
        
        # 3. 计算权重向量
        weights = torch.ones_like(loss)
        # 找出高温样本的索引
        high_temp_mask = real_temps > self.high_temp_thresh
        weights[high_temp_mask] = self.high_weight
        
        # 4. 加权平均
        return (loss * weights).mean()

# ===========================================================
# 3. 增强版 Dataset (支持坏例过采样)
# ===========================================================
class MagnesiumDataset(Dataset):
    # 🟢 [修改] 新增 bad_cases 和 oversample_factor 参数
    def __init__(self, img_dir, transform=None):
        self.img_dir = img_dir
        self.transform = transform
        
        valid_extensions = ('.jpg', '.jpeg', '.png', '.tif', '.tiff', '.bmp')
        self.all_files = [
            f for f in os.listdir(img_dir) 
            if f.lower().endswith(valid_extensions)
        ]
        
        # 🟢 [核心逻辑] 坏例过采样
        # 如果传入了坏例列表，就把它们在列表中复制多份
        # 组别映射表
        self.group_map = {
            'G11': 0, 'G12': 1, 'G13': 2, 'G14': 3, 'G15': 4,
            'G6': 5, 'G7': 6, 'G8': 7, 'G9': 8, 'G10': 9,
           
        }

    def __len__(self):
        return len(self.all_files)

    def __getitem__(self, idx):
        img_name = self.all_files[idx]
        img_path = os.path.join(self.img_dir, img_name)
        
        # --- Read Image ---
        try:
            raw_data = np.fromfile(img_path, dtype=np.uint8)
            image_bgr = cv2.imdecode(raw_data, cv2.IMREAD_COLOR)
        except Exception as e:
            image_bgr = None

        if image_bgr is None:
            return torch.zeros((3, 224, 224)), torch.zeros(192), torch.tensor(0, dtype=torch.long), torch.tensor(0.0), img_name
            
        # Convert to Lab
        image_rgb = cv2.cvtColor(image_bgr, cv2.COLOR_BGR2RGB)
        image_lab = cv2.cvtColor(image_rgb, cv2.COLOR_RGB2Lab)
        l, a, b = cv2.split(image_lab)

        # --- Preprocessing ---
        l_median_new = np.median(l) 
        shift = 128.0 - l_median_new
        l_aligned = l.astype(np.float32) + shift
        l_aligned = np.clip(l_aligned, 0, 255).astype(np.uint8)

        a_blur = cv2.GaussianBlur(a, (5, 5), 0)
        b_blur = cv2.GaussianBlur(b, (5, 5), 0)
        image_lab_processed = cv2.merge((l_aligned, a_blur, b_blur))
        
        # --- Multi-scale Fusion ---
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

        # --- Group ID ---
        try:
            group_str = img_name.split('_')[0]
            group_id = self.group_map.get(group_str, 0)
        except:
            group_id = 0
        group_id_tensor = torch.tensor(group_id, dtype=torch.long)

        # --- Label ---
        try:
            temp_str = img_name.split('_')[1]
            temperature = float(temp_str)
        except:
            temperature = 250.0 
        label = (temperature - 250.0) / 200.0 
        label = torch.tensor(label, dtype=torch.float32)

        if self.transform:
            image = self.transform(image_lab_processed) 
            
        return image, hist_feat, group_id_tensor, label, img_name

# ===========================================================
# 4. 模型定义
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
        
        base_model = models.resnet18(weights=models.ResNet18_Weights.DEFAULT)
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
        # CNN Branch
        feat_map = self.features(x)
        feat_map = self.se_block(feat_map)
        cnn_feat = self.avgpool(feat_map)
        cnn_feat = torch.flatten(cnn_feat, 1)
        
        # Statistical Branch
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
        
        # Fusion
        weighted_cnn = cnn_feat * torch.abs(self.w_cnn)
        weighted_stats = stats_out * torch.abs(self.w_stats)
        combined = torch.cat([weighted_cnn, weighted_stats], dim=1)
        
        out = self.final_regressor(combined)
        return out

# ===========================================================
# 5. 训练函数
# ===========================================================
def train_model(model, train_loader, test_loader, criterion, optimizer, scheduler, num_epochs=20):
    device = next(model.parameters()).device
    best_model_wts = copy.deepcopy(model.state_dict())
    best_mae = float('inf')
    
    train_loss_history = []
    test_mae_history = []

    print("-" * 60)
    print(f" Start Training ({num_epochs} Epochs)")
    print("-" * 60)

    for epoch in range(num_epochs):
        model.train()
        running_loss = 0.0
        
        for inputs, hists, groups, labels, _ in train_loader:
            inputs = inputs.to(device)
            hists = hists.to(device)
            groups = groups.to(device)
            labels = labels.to(device).unsqueeze(1)

            optimizer.zero_grad()
            outputs = model(inputs, hists, groups) 
            loss = criterion(outputs, labels)
            
            loss.backward()
            optimizer.step()
            
            running_loss += loss.item() * inputs.size(0)

        epoch_loss = running_loss / len(train_loader.dataset)

        # Test with TTA
        model.eval()
        val_mae_sum = 0.0
        with torch.no_grad():
            for inputs, hists, groups, labels, _ in test_loader:
                inputs = inputs.to(device)
                hists = hists.to(device)
                groups = groups.to(device)
                labels = labels.to(device).unsqueeze(1)
                
                # TTA x4
                out1 = model(inputs, hists, groups)
                out2 = model(torch.flip(inputs, [3]), hists, groups)
                out3 = model(torch.flip(inputs, [2]), hists, groups)
                out4 = model(torch.flip(inputs, [2, 3]), hists, groups)
                
                outputs = (out1 + out2 + out3 + out4) / 4.0
                
                preds_real = outputs * 200.0 + 250.0
                targets_real = labels * 200.0 + 250.0
                
                batch_mae = torch.abs(preds_real - targets_real)
                val_mae_sum += torch.sum(batch_mae).item()
        
        epoch_mae = val_mae_sum / len(test_loader.dataset)
        
        scheduler.step()
        current_lr = optimizer.param_groups[0]['lr']
        
        train_loss_history.append(epoch_loss)
        test_mae_history.append(epoch_mae)
        
        print(f'Epoch {epoch+1:02d}/{num_epochs} | LR: {current_lr:.6f} | Loss: {epoch_loss:.6f} | MAE: {epoch_mae:.2f}℃')

        if epoch_mae < best_mae:
            best_mae = epoch_mae
            best_model_wts = copy.deepcopy(model.state_dict())
            print(f" New Best! MAE: {best_mae:.2f}℃")

    model.load_state_dict(best_model_wts)
    return model, train_loss_history, test_mae_history

# ===========================================================
# 6. 分析与绘图函数
# ===========================================================
def analyze_bad_cases(model, test_loader, device, top_k=10):
    model.eval()
    all_results = [] 
    
    print(f"\n🔍 正在进行最终全量测试分析...")
    
    with torch.no_grad():
        for inputs, hists, groups, labels, filenames in test_loader:
            inputs = inputs.to(device)
            hists = hists.to(device)
            groups = groups.to(device)
            labels = labels.to(device).unsqueeze(1)
            
            out1 = model(inputs, hists, groups)
            out2 = model(torch.flip(inputs, [3]), hists, groups)
            out3 = model(torch.flip(inputs, [2]), hists, groups)
            out4 = model(torch.flip(inputs, [2, 3]), hists, groups)
            outputs = (out1 + out2 + out3 + out4) / 4.0
            
            preds_real = outputs * 200.0 + 250.0
            targets_real = labels * 200.0 + 250.0
            
            batch_errors = torch.abs(preds_real - targets_real)
            
            for i in range(len(filenames)):
                res = {
                    'name': filenames[i],
                    'actual': targets_real[i].item(),
                    'pred': preds_real[i].item(),
                    'error': batch_errors[i].item()
                }
                all_results.append(res)
                
    all_results.sort(key=lambda x: x['error'], reverse=True)

    # -------------------------------------------------------
    # 🟢 新增：误差分布与目标达成情况分析
    # -------------------------------------------------------
    errors = np.array([r['error'] for r in all_results])
    total_samples = len(errors)
    mae = np.mean(errors)

    print("\n" + "="*60)
    print("📉 误差分布分析:")
    thresholds = [1, 2, 3, 4, 5, 10]
    for thr in thresholds:
        count = np.sum(errors <= thr)
        ratio = (count / total_samples) * 100
        print(f"  误差 ≤ {thr:2d}℃   {ratio:5.1f}%")

    print(f"\n🎯 目标达成情况:")
    p5 = (np.sum(errors <= 5) / total_samples) * 100
    print(f"  误差在5℃以内的样本比例: {p5:.1f}%")

    print(f"\n")
    if mae <= 5.0:
        print(f"✅ 达成目标! 平均MAE为 {mae:.2f}℃ (≤ 5℃)")
    else:
        print(f"⚠️ 接近目标! 平均MAE为 {mae:.2f}℃ 略高于 5℃ 目标")
    
    print("\n" + "="*60)
    print(f"🛑 误差最大的 {top_k} 张图片 (Bad Cases):")
    print(f"{'Filename':<20} | {'Actual':<10} | {'Pred':<10} | {'Error':<10}")
    print("-" * 60)
    for res in all_results[:top_k]:
        print(f"{res['name']:<20} | {res['actual']:<10.2f} | {res['pred']:<10.2f} | {res['error']:<10.2f}")
    print("="*60 + "\n")
    
    actuals = [r['actual'] for r in all_results]
    preds = [r['pred'] for r in all_results]
    
    plt.figure(figsize=(8, 8))
    plt.scatter(actuals, preds, color='blue', alpha=0.6, label='Predictions')
    min_v, max_v = min(actuals + preds) - 5, max(actuals + preds) + 5
    plt.plot([min_v, max_v], [min_v, max_v], 'r--', linewidth=2, label='Ideal (y=x)')
    plt.xlabel('Actual Temperature (°C)')
    plt.ylabel('Predicted Temperature (°C)')
    plt.title(f'Prediction vs Actual (MAE: {np.mean([r["error"] for r in all_results]):.2f})')
    plt.legend()
    plt.grid(True, alpha=0.5)
    plt.show()

# ===========================================================
# 7. 科学的数据划分 (Temperature Balanced Split)
# ===========================================================
def create_balanced_split(dataset, split_ratio=0.2):
    temp_dict = defaultdict(list)
    for idx, fname in enumerate(dataset.all_files):
        try:
            temp = float(fname.split('_')[1])
            temp_dict[temp].append(idx)
        except:
            temp_dict[250.0].append(idx) 
            
    train_indices = []
    test_indices = []
    for temp, indices in temp_dict.items():
        if len(indices) == 1:
            train_indices.extend(indices)
            continue
        np.random.shuffle(indices)
        split_point = int(np.floor(len(indices) * split_ratio))
        if split_point == 0 and len(indices) > 1:
            split_point = 1
        test_indices.extend(indices[:split_point])
        train_indices.extend(indices[split_point:])
        
    np.random.shuffle(train_indices)
    np.random.shuffle(test_indices)
    return train_indices, test_indices

# ===========================================================
# 8. 主程序
# ===========================================================
if __name__ == '__main__':
    setup_seed(42)

    data_transforms = {
        'train': transforms.Compose([
            transforms.ToPILImage(),
            transforms.Resize((224, 224)),
            transforms.RandomHorizontalFlip(p=0.5),
            transforms.RandomVerticalFlip(p=0.5),
            transforms.RandomRotation(degrees=10),
            transforms.ToTensor(),
        ]),
        'test': transforms.Compose([
            transforms.ToPILImage(),
            transforms.Resize((224, 224)),
            transforms.ToTensor(),
        ]),
    }

    data_dir = r'D:\Study\大三上\science\大创\JPG-处理图\JPG-处理图\zhaodu11-35'

    
    # 第一步: 创建干净的基础数据集用于划分
    base_ds = MagnesiumDataset(data_dir, transform=None) 

    if len(base_ds) > 0:
        # 🟢 2. 使用平衡划分获取基础索引 (此时不包含复制品)
        train_idx, test_idx = create_balanced_split(base_ds, split_ratio=0.2)
        
        # 🟢 3. 创建开启过采样的训练集对象
        train_ds_obj = MagnesiumDataset(
            data_dir, 
            transform=data_transforms['train']
        )
        
        # 创建纯净的测试集对象 (不要过采样)
        test_ds_obj  = MagnesiumDataset(
            data_dir, 
            transform=data_transforms['test']
        )
        
        train_dataset = Subset(train_ds_obj, train_idx)
        test_dataset  = Subset(test_ds_obj, test_idx)

        train_loader = DataLoader(train_dataset, batch_size=16, shuffle=True)
        test_loader = DataLoader(test_dataset, batch_size=16, shuffle=False)

        device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        print(f"Device: {device}")

        model = HybridResNet().to(device)
        
        # 使用高温加权 Huber Loss
        criterion = WeightedHuberLoss(delta=1.0, high_temp_thresh=400.0, high_weight=2)
        
        optimizer = optim.Adam(model.parameters(), lr=0.0005) 
        num_epochs = 200
        scheduler = CosineAnnealingLR(optimizer, T_max=num_epochs, eta_min=1e-6)

        trained_model, train_hist, test_hist = train_model(
            model, train_loader, test_loader, 
            criterion, optimizer, scheduler, 
            num_epochs=num_epochs
        )

        save_path = 'magnesium_hybrid_final_model.pth'
        torch.save(trained_model.state_dict(), save_path)
        print(f"💾 Model saved to: {save_path}")

        # 运行分析
        model.load_state_dict(torch.load(save_path))
        analyze_bad_cases(model, test_loader, device, top_k=15)
        
    else:
        print("❌ 错误：未找到图片，请检查路径！")