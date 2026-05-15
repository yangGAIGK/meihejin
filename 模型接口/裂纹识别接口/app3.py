from flask import Blueprint, jsonify, request
from flask_cors import CORS
import numpy as np
from sklearn.preprocessing import MinMaxScaler
from scipy.io import loadmat

# 创建 Blueprint
bp3 = Blueprint('app3', __name__)
CORS(bp3)

# ====================== 辅助函数 ======================
def fun(x, inputnum, hiddennum, outputnum, inputn, outputn):
    """
    计算适应度值
    """
    # 提取权重和偏置
    w1 = x[0:inputnum * hiddennum]
    B1 = x[inputnum * hiddennum:inputnum * hiddennum + hiddennum]
    w2 = x[inputnum * hiddennum + hiddennum:inputnum * hiddennum + hiddennum + hiddennum * outputnum]
    B2 = x[inputnum * hiddennum + hiddennum + hiddennum * outputnum:inputnum * hiddennum + hiddennum + hiddennum * outputnum + outputnum]

    # 手动计算神经网络输出
    w1 = np.reshape(w1, (hiddennum, inputnum))
    w2 = np.reshape(w2, (outputnum, hiddennum))
    B1 = np.reshape(B1, (hiddennum, 1))
    B2 = np.reshape(B2, (outputnum, 1))

    # 输入层到隐藏层
    hidden_layer_input = np.dot(w1, inputn.T) + B1
    hidden_layer_output = 1 / (1 + np.exp(-hidden_layer_input))  # sigmoid 激活函数

    # 隐藏层到输出层
    output_layer_input = np.dot(w2, hidden_layer_output) + B2
    output_layer_output = output_layer_input  # 线性激活函数

    # 计算误差（2范数）
    error = np.linalg.norm(output_layer_output - outputn.T)
    return error

# ====================== PSO 优化 BP 神经网络 ======================
def pso_bp_predict(model_parameters, user_input):
    """
    PSO 优化 BP 神经网络
    """
    # 提取模型参数
    maxNum = model_parameters['maxNum']
    swarmSize = model_parameters['swarmSize']
    lowValue = model_parameters['lowValue']
    topValue = model_parameters['topValue']
    individualFactor = model_parameters['individualFactor']
    groupFactor = model_parameters['groupFactor']
    inertiaFactor = model_parameters['inertiaFactor']

    # 初始化变量
    inputnum = 2  # 输入层节点数
    hiddennum = 11  # 隐藏层节点数
    outputnum = 3  # 输出层节点数（hardness, strength_extension, yield_strength）
    w1num = inputnum * hiddennum  # 输入层到隐层的权值个数
    w2num = outputnum * hiddennum  # 隐层到输出层的权值个数
    N = w1num + hiddennum + w2num + outputnum  # 待优化的变量的个数

    # 读取MAT文件
    data = loadmat('data.mat')
    input_data = data['input']  # 输入数据
    output_data = data['output']  # 输出数据

    # 归一化训练集输入数据
    scaler_input = MinMaxScaler()
    inputn_train = scaler_input.fit_transform(input_data)
    scaler_output = MinMaxScaler()
    outputn_train = scaler_output.fit_transform(output_data)

    # 初始化粒子群
    particles = np.random.uniform(lowValue, topValue, (swarmSize, N))
    velocities = np.zeros((swarmSize, N))
    personal_best_positions = np.copy(particles)
    personal_best_scores = np.full(swarmSize, np.inf)
    global_best_position = np.zeros(N)
    global_best_score = np.inf

    # 主循环
    for it in range(maxNum):
        for i in range(swarmSize):
            # 更新速度
            r1, r2 = np.random.rand(2)
            velocities[i] = (inertiaFactor * velocities[i] +
                            individualFactor * r1 * (personal_best_positions[i] - particles[i]) +
                            groupFactor * r2 * (global_best_position - particles[i]))

            # 更新位置
            particles[i] += velocities[i]
            particles[i] = np.clip(particles[i], lowValue, topValue)

            # 计算适应度值
            fitness = fun(particles[i], inputnum, hiddennum, outputnum, inputn_train, outputn_train)

            # 更新个体最优
            if fitness < personal_best_scores[i]:
                personal_best_scores[i] = fitness
                personal_best_positions[i] = particles[i]

            # 更新全局最优
            if fitness < global_best_score:
                global_best_score = fitness
                global_best_position = particles[i]

        # 自适应权重
        inertiaFactor = 0.9

    # 预测值生成
    w1 = global_best_position[0:inputnum * hiddennum]
    B1 = global_best_position[inputnum * hiddennum:inputnum * hiddennum + hiddennum]
    w2 = global_best_position[inputnum * hiddennum + hiddennum:inputnum * hiddennum + hiddennum + hiddennum * outputnum]
    B2 = global_best_position[inputnum * hiddennum + hiddennum + hiddennum * outputnum:inputnum * hiddennum + hiddennum + hiddennum * outputnum + outputnum]

    w1 = np.reshape(w1, (hiddennum, inputnum))
    w2 = np.reshape(w2, (outputnum, hiddennum))
    B1 = np.reshape(B1, (hiddennum, 1))
    B2 = np.reshape(B2, (outputnum, 1))

    # 接收前端输入数据
    input_user = np.array([[data['aveLength'], data['lisanValue']] for data in user_input])
    inputn_user = scaler_input.transform(input_user)  # 归一化输入数据

    # 计算预测值
    hidden_layer_input = np.dot(w1, inputn_user.T) + B1
    hidden_layer_output = 1 / (1 + np.exp(-hidden_layer_input))
    output_layer_input = np.dot(w2, hidden_layer_output) + B2
    predictions = output_layer_input.T

    # 反归一化预测值
    pre = scaler_output.inverse_transform(predictions)

    # 构建返回的 JSON 数据
    response = {
        "code": 1,
        "data": []
    }

    for i, input_data in enumerate(user_input):
        result = {
            "aveLength": input_data['aveLength'],
            "groupFactor": groupFactor,
            "hardness": pre[i, 0],  # 对应 pre(:, 1)
            "individualFactor": individualFactor,
            "inertiaFactor": inertiaFactor,
            "lisanValue": input_data['lisanValue'],
            "lowValue": lowValue,
            "maxNum": maxNum,
            "strength_extension": pre[i, 1],  # 对应 pre(:, 2)
            "swarmSize": swarmSize,
            "topValue": topValue,
            "yield_strength": pre[i, 2]  # 对应 pre(:, 3)
        }
        response['data'].append(result)

    return response

# ====================== API 接口 ======================
@bp3.route('/psobppredict', methods=['POST'])
def psobppredict():
    """
    API 接口
    """
    try:
        data = request.json
        model_parameters = data['modelParameters']
        user_input = data['userInput']

        # 调用 PSO 优化
        result = pso_bp_predict(model_parameters, user_input)
        return jsonify(result)
    except Exception as e:
        return jsonify({'code': 0, 'error': str(e)}), 400
