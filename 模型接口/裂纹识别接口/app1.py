from flask import Flask, request, jsonify, Blueprint
from flask_cors import CORS
import numpy as np
import scipy.io
from sklearn.preprocessing import MinMaxScaler
from sklearn.neural_network import MLPRegressor

# 创建 Blueprint
bp1 = Blueprint('app1', __name__)
CORS(bp1)  # 启用跨域支持

# 加载 .mat 文件 (只加载一次，避免重复加载)
data_file_path = 'data.mat'  # 修改为实际路径
output_file_path = 'output.mat'  # 修改为实际路径

data = scipy.io.loadmat(data_file_path)
output = scipy.io.loadmat(output_file_path)

# 确保 data 和 output 中的 'input' 和 'output' 变量存在
if 'input' in data and 'output' in output:
    input_data = data['input']
    output_data = output['output']
else:
    raise ValueError("输入数据或输出数据未正确加载，请检查 .mat 文件中的变量名称")

# 确保输入和输出数据的样本数量一致
num_samples = input_data.shape[0]
if num_samples != output_data.shape[0]:
    raise ValueError(f"输入和输出数据的样本数量不一致：输入样本数 = {num_samples}, 输出样本数 = {output_data.shape[0]}")

# 数据归一化
scaler_input = MinMaxScaler()
scaler_output = MinMaxScaler()

input_train = input_data[:48, :]
output_train = output_data[:48, :]

input_train_scaled = scaler_input.fit_transform(input_train)
output_train_scaled = scaler_output.fit_transform(output_train)

# 全局变量存储训练好的模型
mlp = None
last_model_params = {}  # 存储上次的模型参数


def train_model(hidden_layer_sizes=(11,), activation='logistic', solver='lbfgs', max_iter=500, learning_rate=0.01,
                tol=0.001):
    global mlp
    mlp = MLPRegressor(hidden_layer_sizes=hidden_layer_sizes, activation=activation, solver=solver, max_iter=max_iter,
                       learning_rate_init=learning_rate, tol=tol, random_state=42)
    mlp.fit(input_train_scaled, output_train_scaled)
    return mlp


@bp1.route('/predict', methods=['POST'])
def predict():
    try:
        # 获取前端传来的 JSON 数据
        data = request.get_json()

        # 从请求中提取模型参数
        model_parameters = data['modelParameters']
        number_of_cycles = int(model_parameters.get('number_of_cycles', 500))
        learning_rate = float(model_parameters.get('learning_rate', 0.01))
        error_target_value = float(model_parameters.get('error_target_value', 0.001))
        intermediate_layer = int(model_parameters.get('intermediate_layer', 11))
        options = model_parameters.get('options', 'lbfgs')

        # 根据选项选择激活函数
        hidden_layer_sizes = (intermediate_layer,)
        activation = options
        solver = 'lbfgs'

        # 激活函数的匹配
        if activation == "relu":
            activation = "relu"
        elif activation == "identity":
            activation = "identity"
        elif activation == "logistic":
            activation = "logistic"
        elif activation == "tanh":
            activation = "tanh"
        else:
            activation = "relu"

        # 当前模型参数
        current_model_params = {
            'hidden_layer_sizes': hidden_layer_sizes,
            'activation': activation,
            'solver': solver,
            'max_iter': number_of_cycles,
            'learning_rate': learning_rate,
            'tol': error_target_value
        }

        # 检查模型参数是否变化，如果变化则重新训练模型
        global mlp, last_model_params
        if current_model_params != last_model_params:
            mlp = train_model(hidden_layer_sizes, activation, solver, number_of_cycles, learning_rate,
                              error_target_value)
            last_model_params = current_model_params

        # 从请求中获取用户输入数据
        user_input = data['userInput']
        input_values = np.array([[item['aveLength'], item['lisanValue']] for item in user_input])

        # 对输入数据进行归一化
        input_scaled = scaler_input.transform(input_values)
        predictions_scaled = mlp.predict(input_scaled)

        # 将预测结果反归一化
        predictions = scaler_output.inverse_transform(predictions_scaled)

        # 分配到预测的各个字段
        results = []
        for pred in predictions:
            result = {
                'hardness': pred[0],  # 第一个输出字段
                'yield_strength': pred[1],  # 第二个输出字段
                'strength_extension': pred[2]  # 第三个输出字段
            }
            results.append(result)

        # 构建返回的数据：包含 modelParameters, userInput 和预测结果
        response_data = {
            'modelParameters': model_parameters,
            'userInput': user_input,
            'predictions': results
        }

        # 返回成功的响应
        return jsonify({'code': 1, 'data': response_data, 'msg': "计算成功"})

    except Exception as e:
        # 错误处理，返回错误信息
        return jsonify({'code': 0, 'error': str(e), 'msg': "计算失败"}), 400


# 创建 Flask 应用实例并在其中注册 Blueprint
app = Flask(__name__)
# CORS(app)  # 如果在 Blueprint 中启用了 CORS，这里就不需要再次启用
app.register_blueprint(bp1)

if __name__ == '__main__':
    app.run(debug=True)
