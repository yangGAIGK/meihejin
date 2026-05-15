<template>
  <div class="container">
    <h1 class="title">参数设置</h1>
    <div class="form-wrapper">
      <!-- BP 表单 -->
      <div class="form-section bp">
        <div class="section-title">BP</div>
        <el-form
            :model="ruleForm"
            status-icon
            :rules="rules"
            ref="bpRuleForm"
            label-width="160px"
            class="demo-ruleForm"
        >
          <el-form-item label="输入层神经元个数" prop="inputLayer" class="form-item">
            <el-input v-model="ruleForm.inputLayer" autocomplete="off" type="number" step="any" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item label="输出层神经元个数" prop="outputLayer" class="form-item">
            <el-input v-model="ruleForm.outputLayer" autocomplete="off" type="number" step="any" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item label="中间层神经元个数" prop="intermediateLayer" class="form-item">
            <el-input v-model="ruleForm.intermediateLayer" autocomplete="off" type="number" step="any" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item label="激活函数选择" prop="options" class="form-item">
            <el-select v-model="ruleForm.options" placeholder="请选择激活函数" class="custom-select">
              <el-option label="sigmoid函数" value="sigmoid"></el-option>
              <el-option label="ReLU函数" value="relu"></el-option>
              <el-option label="Tanh函数" value="tanh"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="循环次数" prop="numberOfCycles" class="form-item">
            <el-input v-model="ruleForm.numberOfCycles" autocomplete="off" placeholder="常取次数0-100" type="number" step="any" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item label="学习率" prop="learningRate" class="form-item">
            <el-input v-model="ruleForm.learningRate" placeholder="通常0-1" type="number" step="any" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item label="误差目标值" prop="errorTargetValue" class="form-item">
            <el-input v-model="ruleForm.errorTargetValue" autocomplete="off" placeholder="通常0-1" type="number" step="any" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item class="button-group">
            <el-button type="primary" @click="submitForm('bpRuleForm')">提交</el-button>
            <el-button type="danger" @click="resetForm('bpRuleForm')">重置</el-button>
            <el-button type="success" @click="loadLatestData('bpRuleForm', '/BPrecords')">加载上次数据</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- PSO 表单 -->
      <div class="form-section pso">
        <div class="section-title">PSO</div>
        <el-form
            :model="aruleForm"
            status-icon
            :rules="rules"
            ref="psoRuleForm"
            label-width="160px"
            class="demo-ruleForm"
        >
          <el-form-item label="粒子最大迭代次数" prop="maxNum" class="form-item">
            <el-input v-model="aruleForm.maxNum" autocomplete="off" placeholder="推荐范围50-100" type="number" step="any" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item label="最高值 (topValue)" prop="topValue" class="form-item">
            <el-input v-model="aruleForm.topValue" autocomplete="off" placeholder="推荐 50" type="number" step="any" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item label="最低值 (lowValue)" prop="lowValue" class="form-item">
            <el-input v-model="aruleForm.lowValue" autocomplete="off" placeholder="推荐 1" type="number" step="any" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item label="粒子群大小 (swarmSize)" prop="swarmSize" class="form-item">
            <el-input v-model="aruleForm.swarmSize" autocomplete="off" placeholder="推荐 30" type="number" step="any" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item label="个体经验学习因子" prop="individualFactor" class="form-item">
            <el-input v-model="aruleForm.individualFactor" placeholder="通常 1.5" type="number" step="any" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item label="群体经验学习因子" prop="groupFactor" class="form-item">
            <el-input v-model="aruleForm.groupFactor" placeholder="通常 2.0" type="number" step="any" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item label="惯性因子" prop="inertiaFactor" class="form-item">
            <el-input v-model="aruleForm.inertiaFactor" autocomplete="off" placeholder="推荐取值范围[0.4,2]" type="number" step="any" class="custom-input"></el-input>
          </el-form-item>
          <el-form-item class="button-group">
            <el-button type="primary" @click="submitForm('psoRuleForm')">提交</el-button>
            <el-button type="danger" @click="resetForm('psoRuleForm')">重置</el-button>
            <el-button type="success" @click="loadLatestData('psoRuleForm', '/PSOrecords')">加载上次数据</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      ruleForm: {
        inputLayer: null,
        outputLayer: null,
        intermediateLayer: null,
        options: "",
        numberOfCycles: null,
        learningRate: null,
        errorTargetValue: null
      },
      aruleForm: {
        maxNum: null,
        topValue: null,
        lowValue: null,
        swarmSize: null,
        individualFactor: null,
        groupFactor: null,
        inertiaFactor: null
      },
      rules: {
        inputLayer: [{ required: true, message: "请输入输入层神经元个数", trigger: "blur" }],
        outputLayer: [{ required: true, message: "请输入输出层神经元个数", trigger: "blur" }],
        intermediateLayer: [{ required: true, message: "请输入中间层神经元个数", trigger: "blur" }],
        options: [{ required: true, message: "请选择激活函数", trigger: "change" }],
        numberOfCycles: [{ required: true, message: "请输入循环次数", trigger: "blur" }],
        learningRate: [{ required: true, message: "请输入学习率", trigger: "blur" }],
        errorTargetValue: [{ required: true, message: "请输入误差目标值", trigger: "blur" }],
        maxNum: [{ required: true, message: "请输入粒子最大迭代次数", trigger: "blur" }],
        topValue: [{ required: true, message: "请输入最高值", trigger: "blur" }],
        lowValue: [{ required: true, message: "请输入最低值", trigger: "blur" }],
        swarmSize: [{ required: true, message: "请输入粒子群大小", trigger: "blur" }],
        individualFactor: [{ required: true, message: "请输入个体经验学习因子", trigger: "blur" }],
        groupFactor: [{ required: true, message: "请输入群体经验学习因子", trigger: "blur" }],
        inertiaFactor: [{ required: true, message: "请输入惯性因子", trigger: "blur" }]
      }
    };
  },
  methods: {
    // 检测登录状态并获取用户ID
    checkLoginStatus() {
      this.uid = localStorage.getItem("uid");
      if (!this.uid) {
        this.$message.error("未登录，请先登录！");
      }
    },
    submitForm(formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          let formData = {};
          if (formName === 'psoRuleForm') {
            formData = {
              maxNum: parseFloat(this.aruleForm.maxNum) || 100,
              topValue: parseFloat(this.aruleForm.topValue) || 50,
              lowValue: parseFloat(this.aruleForm.lowValue) || 1,
              swarmSize: parseFloat(this.aruleForm.swarmSize) || 30,
              individualFactor: parseFloat(this.aruleForm.individualFactor) || 1.5,
              groupFactor: parseFloat(this.aruleForm.groupFactor) || 2.0,
              inertiaFactor: parseFloat(this.aruleForm.inertiaFactor) || 0.9
            };
          } else {
            formData = {
              inputLayer: parseFloat(this.ruleForm.inputLayer) || 0,
              outputLayer: parseFloat(this.ruleForm.outputLayer) || 0,
              intermediateLayer: parseFloat(this.ruleForm.intermediateLayer) || 0,
              options: this.ruleForm.options || "",
              numberOfCycles: parseFloat(this.ruleForm.numberOfCycles) || 0,
              learningRate: parseFloat(this.ruleForm.learningRate) || 0.01,
              errorTargetValue: parseFloat(this.ruleForm.errorTargetValue) || 0.001
            };
          }

          console.log("请求体数据：", formData);

          const endpoint = formName === "bpRuleForm" ? "/parameter/BP" : "/parameter/PSO";

          const token = localStorage.getItem('Authorization')
          if (!token) {
            this.$message.error('未找到有效的Token，请登录');
            return;
          }

          const config = {
            headers: {
              'Authorization': `Bearer ${token}`,
            }
          };

          axios.post(`http://localhost:8080${endpoint}`, formData, config)
              .then(response => {
                if (response.data.code === 1) {
                  this.$message.success('提交成功！');
                } else {
                  this.$message.error('提交失败：' + response.data.msg);
                }
              })
              .catch(error => {
                console.error('提交失败：', error.response);
                this.$message.error('提交失败');
              });
        } else {
          this.$message.error('表单验证失败，请检查输入');
          return false;
        }
      });
    },
    resetForm(formName) {
      this.$refs[formName].resetFields();
    },
    loadLatestData(formName, endpoint) {
      axios.post(`http://localhost:8080${endpoint}`)
          .then(response => {
            if (response.data.code === 1 && response.data.data) {
              const latestData = response.data.data;
              const formData = this[formName.replace("RuleForm", "")];

              if (formName === "bpRuleForm") {
                formData.inputLayer = latestData.inputLayer || null;
                formData.outputLayer = latestData.outputLayer || null;
                formData.intermediateLayer = latestData.intermediateLayer || null;
                formData.options = latestData.options || "";
                formData.numberOfCycles = latestData.numberOfCycles || null;
                formData.learningRate = latestData.learningRate || null;
                formData.errorTargetValue = latestData.errorTargetValue || null;
              } else if (formName === "psoRuleForm") {
                formData.maxNum = latestData.maxNum || null;
                formData.individualFactor = latestData.individualFactor || null;
                formData.groupFactor = latestData.groupFactor || null;
                formData.inertiaFactor = latestData.inertiaFactor || null;
              }

              this.$message.success("最新数据加载成功！");
            } else {
              this.$message.error("加载失败：" + response.data.msg);
            }
          })
          .catch(error => {
            console.error("加载失败：", error);
            this.$message.error("加载失败");
          });
    }
  }
};
</script>

<style lang="less" scoped>
.container {
  width: 100%;
  max-width: none;
  min-height: 100%;
  padding: 20px 40px 40px;
  background-color: var(--bg-base);
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
  box-sizing: border-box;
}

.title {
  font-size: 28px;
  font-weight: 700;
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 24px;
  text-align: center;
  display: block;
}

.form-wrapper {
  display: flex;
  justify-content: flex-start;
  gap: 24px;
  height: 100%;
}

.form-section {
  width: 50%;
  margin-bottom: 0;
  display: flex;
  flex-direction: column;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: var(--accent-primary);
  text-align: center;
  position: relative;
  padding-bottom: 12px;
  letter-spacing: 1px;

  &::after {
    content: '';
    position: absolute;
    left: 50%;
    bottom: 0;
    transform: translateX(-50%);
    width: 80px;
    height: 2px;
    background: var(--accent-gradient);
    border-radius: 2px;
  }
}

/* 核心：白色圆角卡片 → 深色卡片 */
.demo-ruleForm {
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  padding: 28px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0; left: 10%; width: 80%; height: 1px;
    background: linear-gradient(90deg, transparent, var(--accent-primary), transparent);
    opacity: 0.35;
    border-radius: var(--radius-lg);
  }
}

.form-item {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin-bottom: 16px;
}

.form-label {
  display: inline-block;
  width: 160px;
  text-align: right;
  font-size: 14px;
  color: var(--text-secondary);
  margin-right: 0;
}

.custom-input {
  display: inline-block;
  width: auto;
  flex-grow: 1;
}

.custom-select {
  width: auto;
  flex-grow: 1;

  .el-input { font-size: 14px; }
}

.custom-select .el-input { width: 100%; flex-grow: 1; }
.custom-select .el-input__inner { width: 100%; font-size: 14px; }

.button-group {
  display: flex;
  justify-content: center;
  margin-top: auto;
}

/* el-switch 深色适配 */
/deep/ .el-switch__label {
  color: var(--text-secondary) !important;
  &.is-active { color: var(--accent-primary) !important; }
}

/* upload 区域深色 */
/deep/ .el-upload--picture-card {
  background-color: var(--bg-overlay) !important;
  border-color: var(--border-default) !important;
  color: var(--text-muted) !important;
  &:hover {
    border-color: var(--accent-primary) !important;
    color: var(--accent-primary) !important;
  }
}

/deep/ .el-upload__tip {
  color: var(--text-muted) !important;
  font-size: 12px;
}

/deep/ .el-upload-list--picture-card .el-upload-list__item {
  background-color: var(--bg-overlay) !important;
  border-color: var(--border-subtle) !important;
}
</style>

/*  浅灰色背景  */
