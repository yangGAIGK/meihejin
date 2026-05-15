from flask import Flask, request, jsonify, Blueprint
import numpy as np
from flask_cors import CORS
from scipy import io
from sklearn.preprocessing import MinMaxScaler

# 创建 Blueprint
bp2 = Blueprint('app2', __name__)
CORS(bp2)

# ====================== 辅助函数 ======================
def selection(position, cost, swarmSize, N):
    newpop = np.zeros((swarmSize, N))
    totalfit = np.sum(cost)
    prob = cost / totalfit
    cum_prob = np.cumsum(prob)

    ms = np.sort(np.random.rand(swarmSize))
    fitin = 0
    newin = 0

    while newin < swarmSize:
        if ms[newin] < cum_prob[fitin]:
            newpop[newin] = position[fitin]
            newin += 1
        else:
            fitin += 1
    return newpop

def cross(swarmSize, newpop, pc, N):
    newpop2 = np.zeros((swarmSize, N))

    if swarmSize % 2 == 0:
        cross_num = swarmSize
    else:
        cross_num = swarmSize - 1

    for i in range(0, cross_num, 2):
        if np.random.rand() < pc:
            r = np.random.rand()
            newpop2[i] = r * newpop[i] + (1 - r) * newpop[i + 1]
            newpop2[i + 1] = r * newpop[i + 1] + (1 - r) * newpop[i]
        else:
            newpop2[i] = newpop[i]
            newpop2[i + 1] = newpop[i + 1]
    return newpop2

def neural_network(x, inputnum, hiddennum, outputnum, input_data):
    w1 = x[:inputnum * hiddennum].reshape(hiddennum, inputnum)
    b1 = x[inputnum * hiddennum: inputnum * hiddennum + hiddennum]
    w2 = x[inputnum * hiddennum + hiddennum: inputnum * hiddennum + hiddennum + hiddennum * outputnum].reshape(outputnum, hiddennum)
    b2 = x[inputnum * hiddennum + hiddennum + hiddennum * outputnum:]

    hidden = np.tanh(np.dot(w1, input_data) + b1[:, np.newaxis])
    output = np.dot(w2, hidden) + b2[:, np.newaxis]
    return output

def fitness(x, inputnum, hiddennum, outputnum, inputn, outputn):
    pred = neural_network(x, inputnum, hiddennum, outputnum, inputn)
    return np.linalg.norm(pred - outputn)

# ====================== 主程序及预测函数 ======================
def train_model(input_data, output_data, maxNum, swarmSize, lowValue, topValue, individualFactor, groupFactor, inertiaFactor):
    inputnum = 2
    hiddennum = 6
    outputnum = 3

    input_train = input_data[:48].T
    output_train = output_data[:48, :3].T

    input_scaler = MinMaxScaler()
    output_scaler = MinMaxScaler()
    inputn = input_scaler.fit_transform(input_train.T).T
    outputn = output_scaler.fit_transform(output_train.T).T

    N = inputnum * hiddennum + hiddennum + hiddennum * outputnum + outputnum
    particles = [{
        'position': np.random.uniform(lowValue, topValue, N),
        'velocity': np.zeros(N),
        'best_pos': None,
        'best_cost': np.inf
    } for _ in range(swarmSize)]

    global_best = {'position': None, 'cost': np.inf}

    inertiaFactor_max = inertiaFactor
    inertiaFactor_min = inertiaFactor * 0.4

    for it in range(maxNum):
        w = inertiaFactor_max - (inertiaFactor_max - inertiaFactor_min) * (it / maxNum)

        for p in particles:
            p['cost'] = fitness(p['position'], inputnum, hiddennum, outputnum, inputn, outputn)
            if p['cost'] < p['best_cost']:
                p['best_pos'] = p['position'].copy()
                p['best_cost'] = p['cost']
                if p['best_cost'] < global_best['cost']:
                    global_best['position'] = p['best_pos'].copy()
                    global_best['cost'] = p['best_cost']

            r1, r2 = np.random.rand(2)
            p['velocity'] = (w * p['velocity'] +
                             individualFactor * r1 * (p['best_pos'] - p['position']) +
                             groupFactor * r2 * (global_best['position'] - p['position']))
            p['position'] = p['position'] + p['velocity']

            p['position'] = np.clip(p['position'], lowValue, topValue)

        positions = np.array([p['position'] for p in particles])
        costs = np.array([p['cost'] for p in particles])
        newpop = selection(positions, costs, swarmSize, N)
        newpop2 = cross(swarmSize, newpop, 0.7, N)

        for i in range(swarmSize):
            new_cost = fitness(newpop2[i], inputnum, hiddennum, outputnum, inputn, outputn)
            if new_cost < particles[i]['cost']:
                particles[i]['position'] = newpop2[i]

    best_weights = global_best['position']

    def predict(aveLength, lisanValue):
        input_vals = np.array([[aveLength, lisanValue]]).T
        inputn_test = input_scaler.transform(input_vals.T).T
        pred_norm = neural_network(best_weights, inputnum, hiddennum, outputnum, inputn_test)
        pred = output_scaler.inverse_transform(pred_norm.T).T
        hardness, yield_strength, strength_extension = pred.flatten()
        return hardness, yield_strength, strength_extension

    return predict

@bp2.route('/psopredict', methods=['POST'])
def psopredict():
    try:
        data = request.json
        model_parameters = data['modelParameters']
        user_input = data['userInput']

        # 将 JSON 数据中的键名映射到 train_model 函数的参数名
        train_params = {
            'maxNum': model_parameters['maxNum'],
            'swarmSize': model_parameters['swarmSize'],
            'lowValue': model_parameters['lowValue'],
            'topValue': model_parameters['topValue'],
            'individualFactor': model_parameters['individualFactor'],  # 注意小写
            'groupFactor': model_parameters['groupFactor'],           # 注意小写
            'inertiaFactor': model_parameters['inertiaFactor']
        }

        # 划分数据集
        data_mat = io.loadmat('data.mat')
        input_data = data_mat['input']
        output_data = data_mat['output']

        # 训练模型并获取预测函数
        predict_func = train_model(input_data, output_data, **train_params)

        # 进行预测，并将模型参数和预测结果合并
        results = []
        for input_val in user_input:
            aveLength = input_val['aveLength']
            lisanValue = input_val['lisanValue']
            hardness, yield_strength, strength_extension = predict_func(aveLength, lisanValue)
            # 合并模型参数和预测结果
            result = {
                **model_parameters,  # 添加模型参数
                **input_val,         # 添加用户输入
                'hardness': hardness,
                'yield_strength': yield_strength,
                'strength_extension': strength_extension
            }
            results.append(result)

        # 返回结果，包含 code 和 data
        return jsonify({
            'code': 1,  # 成功标志
            'data': results
        })

    except Exception as e:
        # 如果出现异常，返回失败标志
        return jsonify({
            'code': 0,  # 失败标志
            'error': str(e)
        })

# 创建 Flask 应用实例并在其中注册 Blueprint
app = Flask(__name__)
# CORS(app)  # 如果在 Blueprint 中启用了 CORS，这里就不需要再次启用
app.register_blueprint(bp2)

if __name__ == "__main__":
    app.run(debug=True)
