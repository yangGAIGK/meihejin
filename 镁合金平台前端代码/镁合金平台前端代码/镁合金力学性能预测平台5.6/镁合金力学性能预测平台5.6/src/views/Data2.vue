<template>
  <div class="container">
    <el-form :inline="true" :model="formData" class="demo-form-inline">
      <el-form-item label="平均晶粒尺寸:" class="form-item">
        <el-input v-model="formData.aveLength" placeholder="请输入平均晶粒尺寸(μm)"></el-input>
      </el-form-item>
      <el-form-item label="离散特征值:" class="form-item">
        <el-input v-model="formData.lisanValue" placeholder="请输入离散特征值(μm)"></el-input>
      </el-form-item>
      <el-form-item class="button-group">
        <el-button type="primary" @click="insert">插入</el-button>
        <el-button type="danger" @click="reset">重置</el-button>
        <el-button type="warning" @click="clearTable" v-if="tableData.length > 0">清除数据</el-button>
        <!-- 添加下一步按钮 -->
        <el-button type="success" @click="goToNextPage">下一步</el-button>
      </el-form-item>
    </el-form>
    <div class="image-container" v-if="!visual">
      <el-empty description="暂无数据" style="background-color: #fff;"></el-empty>
    </div>
    <el-table :data="tableData" border class="data-table" height="550" style="width: 100%;" v-if="visual">
      <el-table-column prop="aveLength" label="平均晶粒尺寸" min-width="300" align="center"></el-table-column>
      <el-table-column prop="lisanValue" label="离散特征值" min-width="300" align="center"></el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  data() {
    return {
      formData: {
        aveLength: "",
        lisanValue: ""
      },
      tableData: [],
      visual: false
    };
  },
  mounted() {
    // 页面加载时从 sessionStorage 获取之前存储的数据
    const storedData = sessionStorage.getItem('tableData');
    if (storedData) {
      this.tableData = JSON.parse(storedData); // 将存储的 JSON 字符串解析为数组
      this.visual = true; // 如果有数据，显示表格
    }
  },
  methods: {
        // 检测登录状态并获取用户ID
    checkLoginStatus() {
      this.uid = localStorage.getItem("uid");
      if (!this.uid) {
        this.$message.error("未登录，请先登录！");
      }
    },
    async insert() {
      // 检查用户是否登录
      await this.fetchHistoryData();

      const { aveLength, lisanValue } = this.formData;

      // 校验是否为空或无效值
      if (!aveLength || !lisanValue || aveLength.trim() === "" || lisanValue.trim() === "") {
        this.$message.warning("输入框不能为空");
        return;
      }

      // 校验是否为数字
      const parsedAveLength = parseFloat(aveLength);
      const parsedLisanValue = parseFloat(lisanValue);

      if (isNaN(parsedAveLength) || isNaN(parsedLisanValue)) {
        this.$message.error("请输入有效的数字");
        return;
      }

      // 插入数据
      this.tableData.push({
        aveLength: parsedAveLength,
        lisanValue: parsedLisanValue
      });

      // 使用 sessionStorage 保存数据
      sessionStorage.setItem('tableData', JSON.stringify(this.tableData));

      this.$message.success("数据插入成功");
      this.reset();
      this.visual = true;

      // 更新 Vuex 状态
      this.$store.commit("getInsertData", this.tableData);
    },
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

        // 这里可以添加调用后端接口获取历史数据的逻辑
        // 示例：
        // const response = await axios.get('/api/history-data', {
        //   headers: {
        //     'Authorization': `Bearer ${token}`
        //   }
        // });
        // this.tableData = response.data;

      } catch (error) {
        console.error("获取历史数据失败：", error);
        this.$message.error("获取历史数据失败，请重试");
      } finally {
        this.loading = false;
      }
    },
    reset() {
      this.formData.lisanValue = "";
      this.formData.aveLength = "";
      this.visual = this.tableData.length > 0; // 根据数据是否为空更新 visual 状态
    },
    clearTable() {
      this.tableData = [];
      this.visual = false;
      this.$message.warning("已清除所有数据");
      sessionStorage.removeItem('tableData'); // 清除 sessionStorage 中的数据
      this.$store.commit("getInsertData", this.tableData); // 更新 Vuex 状态
    },
    goToNextPage() {
      // 跳转到 data2-next 页面
      this.$router.push('/data2-next'); // 跳转路径为 /data2-next
    }
  }
};
</script>

<style lang="less" scoped>
.container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.demo-form-inline {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.form-item {
  margin-bottom: 15px;
}

.button-group {
  display: flex;
  gap: 10px;
}

.image-container {
  text-align: center;
  margin-top: 20px;
}

.preview-image {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.data-table {
  margin-top: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.el-button {
  font-size: 14px;
  padding: 10px 20px;
  border-radius: 5px;
}

.el-button--primary {
  background-color: #409eff;
  border-color: #409eff;
  color: #fff;
}

.el-button--danger {
  background-color: #f56c6c;
  border-color: #f56c6c;
  color: #fff;
}

.el-button--success {
  background-color: #67c23a;
  border-color: #67c23a;
  color: #fff;
}
</style>