<template>
  <div class="container">
    <h1 class="title">PSO 参数历史记录</h1>

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
    >
      <el-table-column
          label="序号"
          width="60"
      >
        <template #header>
          序号
        </template>
        <template #default="scope">
          {{ scope.$index + 1 }} <!-- 显示递增的序号 -->
        </template>
      </el-table-column>

      <!-- 表格列 -->
      <el-table-column prop="maxNum" label="最大数值" min-width="200" />
      <el-table-column prop="topValue" label="上界值" min-width="200" />
      <el-table-column prop="lowValue" label="下界值" min-width="200" />
      <el-table-column prop="swarmSize" label="种群规模" min-width="200" />
      <el-table-column prop="individualFactor" label="个体因子" min-width="200" />
      <el-table-column prop="groupFactor" label="群体因子" min-width="200" />
      <el-table-column prop="inertiaFactor" label="惯性因子" min-width="200" />
      <el-table-column prop="createUser" label="创建用户" min-width="200" />
      <el-table-column prop="createTime" label="创建时间" min-width="200" />
      <el-table-column prop="updateTime" label="更新时间" min-width="200" />

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
        v-if="total > 0"
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
            "http://localhost:8080/PSOrecords",
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
          this.table = response.data.data.records || [];
          this.total = response.data.data.total || 0;
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
      const tableData = JSON.parse(sessionStorage.getItem('tableData') || '[]');
      console.log("Table Data from sessionStorage:", tableData);  // 调试输出

      // 检查 tableData 是否为空
      if (tableData.length === 0) {
        this.$message.error("用户输入数据为空！");
        return;
      }

      // 将每个项变为字典格式，包含 aveLength 和 lisanValue
      const userInput = tableData.map(item => ({
        aveLength: item.aveLength,  // 从 tableData 获取 aveLength
        lisanValue: item.lisanValue // 从 tableData 获取 lisanValue
      }));

      // 组合表格数据和 sessionStorage 数据，构建 JSON 格式
      const requestData = {
        modelParameters: {
          maxNum: row.maxNum,
          topValue: row.topValue,
          lowValue: row.lowValue,
          swarmSize: row.swarmSize,
          individualFactor: row.individualFactor,
          groupFactor: row.groupFactor,
          inertiaFactor: row.inertiaFactor,
        },
        userInput: userInput,  // 数值格式的 userInput
      };

      console.log("Request Data to API:", requestData);

      try {
        // 调用 API 并传递数据
        const response = await axios.post("http://127.0.0.1:5000/psopredict", requestData);
        console.log('API Response:', response.data);

        // 检查是否成功
        if (response.data.code === 1) {
          const predictions = response.data.data; // 获取预测结果

          // 组合数据成指定格式
          const combinedData = userInput.map((input, index) => ({
            ...input, // 包含 aveLength 和 lisanValue
            ...predictions[index], // 包含 hardness, yield_strength, strength_extension
            maxNum: row.maxNum,
            topValue: row.topValue,
            lowValue: row.lowValue,
            swarmSize: row.swarmSize,
            individualFactor: row.individualFactor,
            groupFactor: row.groupFactor,
            inertiaFactor: row.inertiaFactor,
          }));

          console.log("Combined Data:", combinedData);

          // 发送到后端存储
          await this.sendDataToBackend(combinedData);
        } else {
          this.$message.error(response.data.msg || "预测失败！");
        }
      } catch (error) {
        console.error('Error processing data:', error);
        this.$message.error("处理数据或发送到后端时出错");
      }
    },

    // 发送数据到后端
    async sendDataToBackend(data) {
      try {
        // 从 localStorage 获取 JWT token
        const token = localStorage.getItem("Authorization"); // 获取 token
        if (!token) {
          this.$message.error("请先登录");
          return;
        }
        const response = await axios.post("http://localhost:8080/psoparameter", data, {
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
        console.error('Backend API Error:', error);
        this.$message.error("请求后端API失败，请稍后再试");
      }
    },

    // 行样式
    rowStyle({rowIndex}) {
      return rowIndex % 2 === 0 ? {background: '#f9f9f9'} : {};
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
