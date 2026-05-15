<template>
  <div class="container">
    <h1 class="title">BP 参数历史记录</h1>

    <!-- 当没有数据时显示的空状态 -->
    <div class="image-container" v-if="!visual">
      <el-empty description="暂无数据" style="background-color: #fff;"></el-empty>
    </div>

    <!-- 表格组件 -->
    <el-table
        v-else
        border
        :data="table"
        class="custom-table"
        height="550"
        :row-style="rowStyle"
        v-loading="loading"
        element-loading-text="加载中..."
    >
      <el-table-column label="序号" width="60">
        <template #header>
          序号
        </template>
        <template #default="scope">
          {{ scope.$index + 1 }}
          <!-- 显示递增的序号 -->
        </template>
      </el-table-column>

      <!-- 表格列 -->
      <el-table-column prop="inputLayer" label="输入层" min-width="200" />
      <el-table-column prop="outputLayer" label="输出层" min-width="200" />
      <el-table-column prop="intermediateLayer" label="中间层" min-width="200" />
      <el-table-column prop="options" label="选项" min-width="200" />
      <el-table-column prop="numberOfCycles" label="循环次数" min-width="200" />
      <el-table-column prop="learningRate" label="学习率" min-width="200" />
      <el-table-column prop="errorTargetValue" label="误差目标值" min-width="200" />

      <!-- 操作列 -->
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button type="primary" @click="apply(scope.row)">应用</el-button>
        </template>
      </el-table-column>

      <!-- ID 列 (隐藏) -->
      <el-table-column
          label="ID"
          prop="id"
          width="100"
          :show-overflow-tooltip="true"
          :hidden="true"
      />
    </el-table>

    <!-- 分页器 -->
    <el-pagination
        v-if="total > 0 && visual"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
    >
    </el-pagination>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      visual: false,
      table: [], // 表格数据
      total: 0, // 总记录数
      currentPage: 1, // 当前页码
      pageSize: 10, // 每页显示条数
      loading: false, // 加载状态
    };
  },
  methods: {
    // 获取历史记录数据
    async fetchHistoryData() {
      this.loading = true;
      try {
        // 从 localStorage 获取 JWT token
        const token = localStorage.getItem("Authorization"); // 获取 token
        if (!token) {
          this.$message.error("请先登录");
          this.loading = false;
          return;
        }

        // 发送请求，获取历史记录，确保 Authorization 头包含 token
        const response = await axios.post(
            "http://localhost:8080/BPrecords",
            {
              page: this.currentPage,
              pageSize: this.pageSize,
            },
            {
              headers: {
                Authorization: `Bearer ${token}`, // 将 token 添加到请求头
              },
            }
        );

        if (response.data.code === 1) {
          this.table = response.data.data.records || [];  // 修改这行！
          this.total = response.data.data.total || 0;    // 修改这行！
          this.visual = true;
        } else {
          this.$message.error(response.data.msg || "获取历史记录失败");
          this.table = [];
          this.total = 0;
          this.visual = false;
        }
      } catch (error) {
        console.error("获取历史记录失败：", error);
        this.$message.error("网络错误，请稍后再试");
        this.visual = false;
      } finally {
        this.loading = false;
      }
    },

    // 每页显示条数变化时的处理函数
    handleSizeChange(newSize) {
      this.pageSize = newSize;
      this.fetchHistoryData();
    },

    // 当前页码变化时的处理函数
    handleCurrentChange(newPage) {
      this.currentPage = newPage;
      this.fetchHistoryData();
    },

    // 应用按钮点击事件
    async apply(row) {
      // 从 sessionStorage 获取 tableData
      const tableData = JSON.parse(sessionStorage.getItem("fileUploadData") || "[]");
      console.log("Table Data from sessionStorage:", tableData); // 调试输出

      // 检查 tableData 是否为空
      if (tableData.length === 0) {
        this.$message.error("用户输入数据为空！");
        return;
      }

      // 修改：将每个项变为字典格式，包含 aveLength 和 lisanValue
      const userInput = tableData.map((item) => ({
        aveLength: item.aveLength, // 从 tableData 获取 aveLength
        lisanValue: item.lisanValue, // 从 tableData 获取 lisanValue
      }));

      // 组合表格数据和 sessionStorage 数据，构建 JSON 格式
      const requestData = {
        modelParameters: {
          input_layer: row.inputLayer,
          output_layer: row.outputLayer,
          intermediate_layer: row.intermediateLayer,
          options: row.options,
          number_of_cycles: row.numberOfCycles,
          learning_rate: row.learningRate,
          error_target_value: row.errorTargetValue,
        },
        userInput: userInput, // 数值格式的 userInput
      };

      console.log("Request Data to API:", requestData);

      // 调用 API 并传递数据
      this.processDataAndSend(requestData, row);
    },

    // 新增：处理数据并发送到后端
    async processDataAndSend(requestData, row) {
      try {
        // 1. 调用 /predict 接口获取预测值
        const predictionResponse = await axios.post("http://localhost:5000/predict", requestData);
        console.log("API Response:", predictionResponse.data);

        if (predictionResponse.data.code !== 1) {
          this.$message.error(predictionResponse.data.msg || "预测失败！");
          return;
        }

        // 获取 predictions 数组
        const predictions = predictionResponse.data.data.predictions;

        // 2. 组合数据成指定格式
        const combinedData = requestData.userInput.map((input, index) => {
          const prediction = predictions[index]; // 获取对应的预测结果
          return {
            aveLength: input.aveLength,
            lisanValue: input.lisanValue,
            hardness: prediction.hardness, // 假设返回的是包含 hardness, yieldStrength, strengthExtension 的对象
            yieldStrength: prediction.yield_strength, // 修改为 yield_strength
            strengthExtension: prediction.strength_extension, // 修改为 strength_extension
            inputLayer: row.inputLayer,
            outputLayer: row.outputLayer,
            intermediateLayer: row.intermediateLayer,
            options: row.options,
            numberOfCycles: row.numberOfCycles,
            learningRate: row.learningRate,
            errorTargetValue: row.errorTargetValue,
          };
        });

        console.log("Combined Data:", combinedData);

        // 3. 发送到后端存储
        await this.sendDataToBackend(combinedData);
      } catch (error) {
        console.error("Error processing data:", error);
        this.$message.error("处理数据或发送到后端时出错");
      }
    },

    // 新增：发送数据到后端
    async sendDataToBackend(data) {
      try {
        // 从 localStorage 获取 JWT token
        const token = localStorage.getItem("Authorization"); // 获取 token
        if (!token) {
          this.$message.error("请先登录");
          return;
        }
        const response = await axios.post("http://localhost:8080/bpparameter", data, {
          headers: {
            Authorization: `Bearer ${token}`, // 将 token 添加到请求头
          },
        });

        if (response.data.code === 1) {
          this.$message.success("数据保存成功！");
        } else {
          this.$message.error(response.data.msg || "数据保存失败！");
        }
      } catch (error) {
        console.error("Backend API Error:", error);
        this.$message.error("请求后端API失败，请稍后再试");
      }
    },

    // 行样式
    rowStyle({ rowIndex }) {
      return rowIndex % 2 === 0 ? { background: "#f9f9f9" } : {};
    },
  },

  mounted() {
    this.fetchHistoryData(); // 组件加载时获取历史数据
  },
};
</script>

<style lang="less" scoped>
.container {
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 20px;
}

.image-container {
  margin: 20px 0;
}

.custom-table {
  width: 80%;
  margin: 0 auto;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ddd;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.el-table th {
  background-color: #42a5f5;
  color: white;
  text-align: center;
}

.el-table td {
  text-align: center;
  font-size: 14px;
}
</style>
