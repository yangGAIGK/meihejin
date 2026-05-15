<template>
  <div class="container">
    <h1 class="title">GA 参数历史记录</h1>

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
      <el-table-column label="序号" width="60" :render-header="renderHeader">
        <template slot-scope="scope">
          {{ scope.$index + 1 }} <!-- 显示递增的序号 -->
        </template>
      </el-table-column>

      <!-- GA 参数列 -->
      <el-table-column prop="crossoverProbability" label="交叉概率" min-width="200" />
      <el-table-column prop="variationProbability" label="变异概率" min-width="200" />
      <el-table-column prop="createUser" label="创建用户" min-width="200" />
      <el-table-column prop="createTime" label="创建时间" min-width="200" />
      <el-table-column prop="updateTime" label="更新时间" min-width="200" />

      <!-- 操作列 -->
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button type="danger" @click="deleteRecord(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>

      <!-- ID 列 (隐藏) -->
      <el-table-column label="ID" prop="id" width="100" :show-overflow-tooltip="true" :hidden="true" />
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
      visual: false, // 控制表格显示
      table: [], // 表格数据
      total: 0, // 总记录数
      currentPage: 1, // 当前页码
      pageSize: 10, // 每页显示条数
      loading: false, // 加载状态
    };
  },
  methods: {
    async fetchHistoryData() {
      this.loading = true;
      try {
        const token = localStorage.getItem("Authorization"); // 获取 token
        if (!token) {
          this.$message.error("请先登录");
          this.loading = false;
          return;
        }

        // 发送请求，获取历史记录，确保 Authorization 头包含 token
        const response = await axios.post(
            "http://localhost:8080/GArecords", // 修改为 GA 记录的 API URL
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

        // 打印响应数据，确认数据格式
        console.log('Response data:', response.data);

        if (response.data.code === 1) {
          this.table = response.data.data.records || []; // 将 records 赋值给表格数据
          this.total = response.data.data.total || 0; // 获取数据总条数
          this.visual = true; // 成功加载数据后显示表格
        } else {
          this.$message.error(response.data.msg || "获取历史记录失败");
          this.table = [];
          this.total = 0;
          this.visual = false; // 如果没有数据，隐藏表格
        }
      } catch (error) {
        console.error("获取历史记录失败：", error);
        this.$message.error("网络错误，请稍后再试");
        this.visual = false;
      } finally {
        this.loading = false;
      }
    },

    handleSizeChange(newSize) {
      this.pageSize = newSize;
      this.fetchHistoryData(); // 切换每页显示条数时重新加载数据
    },

    handleCurrentChange(newPage) {
      this.currentPage = newPage;
      this.fetchHistoryData(); // 切换页码时重新加载数据
    },

    // 删除记录
    async deleteRecord(id) {
      try {
        const confirmDelete = await this.$confirm(
            "确定要删除该记录吗？",
            "提示",
            {
              confirmButtonText: "确定",
              cancelButtonText: "取消",
              type: "warning",
            }
        );

        if (confirmDelete) {
          const response = await axios.post("http://localhost:8080/GArecords", {
            action: "delete",
            id: id,
          });

          if (response.data.code === 1) {
            this.$message.success("删除成功");
            this.fetchHistoryData(); // 删除后刷新数据
          } else {
            this.$message.error(response.data.msg || "删除失败");
          }
        }
      } catch (error) {
        this.$message.info("已取消删除");
      }
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
