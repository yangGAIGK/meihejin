<template>
  <div class="data2next-container">
    <h2 class="page-title">请选择您需要的模型</h2>
    <div class="model-container">
      <!-- 左侧 BP 部分 -->
      <div class="model-box left">
        <h3 class="model-title">BP</h3>
        <div class="model-description">
          <p>强大的非线性拟合能力：能精确捕捉镁合金成分（如 Al、Zn 含量）、热处理温度、轧制速率等多变量与屈服强度、延伸率间的复杂非线性关系，尤其适用于 AZ 系列等成熟合金体系的性能预测。
          </p><p>自学习与泛化能力：通过多层神经元网络结构，可自动提取数据特征，对未见过的合金成分组合仍能保持较好的预测精度。
        </p><p>模块化设计：易于与其他算法结合，例如通过引入卷积层处理微观组织图像数据，实现成分 - 工艺 - 组织 - 性能的全链条预测。</p>
        </div>
        <!-- '选我'按钮在最下方 -->
        <el-button type="primary" @click="goToNextPage('bp')">选我</el-button>
      </div>

      <!-- 中间 PSO 部分 -->
      <div class="model-box middle">
        <h3 class="model-title">PSO_BP</h3>
        <div class="model-description">
          <p>高效全局搜索：通过模拟鸟群协作，在高维参数空间中快速定位最优解，显著缩短镁合金本构模型（如 Arrhenius 方程）参数优化周期。
          </p><p>动态适应性：实时调整粒子搜索方向，可同步优化镁合金热加工过程中动态变化的温度、应力等参数，提升工艺窗口预测精度。
        </p><p>低计算成本：相比遗传算法，PSO 通过更少的迭代次数即可达到同等优化效果，适合在线实时预测与控制。</p>
        </div>
        <!-- '选我'按钮在最下方 -->
        <el-button type="primary" @click="goToNextPage('pso')">选我</el-button>
      </div>

      <!-- 右侧 GA 部分 -->
      <div class="model-box right">
        <h3 class="model-title">GA_PSO_BP</h3>
        <div class="model-description">
          <p>多目标协同优化：可同时平衡强度、塑性、耐腐蚀性等多个性能指标，例如在 Mg-Al-Zn 合金中寻找 Al 含量与晶粒尺寸的最佳组合。
          </p><p>创新方案生成：通过交叉、变异操作探索传统实验难以覆盖的合金成分空间，成功设计出含 Y、Nd 等稀土元素的新型高强镁合金。
        </p><p>鲁棒性优化：对初始解不敏感，即使在数据噪声较大的情况下（如早期实验数据不足）仍能输出可靠的优化方案。</p>
        </div>
        <!-- '选我'按钮在最下方 -->
        <el-button type="primary" @click="goToNextPage('ga')">选我</el-button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "Data2Next",
  methods: {
    // 点击“选我”按钮时跳转到相应的页面
    goToNextPage(model) {
      if (model === 'bp') {
        this.$router.push('/bpModel'); // 跳转到 BP 模型的页面
      } else if (model === 'pso') {
        this.$router.push('/psoModel'); // 跳转到 PSO 模型的页面
      } else if (model === 'ga') {
        this.$router.push('/psobpModel'); // 跳转到 GA 模型的页面
      }
    }
  }
};
</script>

<style scoped>
/* 整个页面的容器 */
.data2next-container {
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
}

.page-title {
  text-align: center;
  font-size: 24px;
  margin-bottom: 30px;
}

.model-container {
  display: flex;
  justify-content: space-between;
}

.model-box {
  width: 30%;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  box-sizing: border-box;
  display: flex; /* 使得容器为flex布局 */
  flex-direction: column; /* 垂直排列内容 */
  justify-content: space-between; /* 让内容分布在容器中 */
  word-wrap: break-word; /* 防止长文本溢出并自动换行 */
  white-space: normal; /* 确保文本能够自动换行 */
}

.model-box h3 {
  text-align: center;
  font-size: 20px;
  margin-bottom: 20px;
}

.model-description {
  font-size: 16px;
  text-align: left; /* 修改这里，将文本对齐方式改为左对齐 */
  margin-bottom: 20px;
  flex-grow: 1; /* 让描述区域扩展并占据剩余空间 */
}

.el-button {
  width: 100%;
  font-size: 16px;
  margin-top: auto; /* 将按钮推到容器底部 */
}

.left {
  background-color: #f0f9eb;
}

.middle {
  background-color: #f6f9fc;
}

.right {
  background-color: #fef6f3;
}
</style>
