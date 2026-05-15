<template>
  <div class="container">
    <el-form :inline="true" :model="formData" class="demo-form-inline">
      <el-form-item label="文件上传:" class="form-item">
        <el-upload
            class="upload-demo"
            action="https://jsonplaceholder.typicode.com/posts/"
            :on-change="handleFileChange"
            :before-upload="beforeUpload"
            :on-remove="handleFileRemove"
            :limit="1"
            :on-exceed="handleExceed"
            :file-list="fileList">
          <el-button type="primary">点击上传</el-button>
        </el-upload>
      </el-form-item>
      <el-form-item class="button-group">
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
import * as XLSX from 'xlsx'; // 引入 xlsx 库

export default {
  data() {
    return {
      formData: {
        aveLength: "",
        lisanValue: ""
      },
      tableData: [],
      visual: false,
      fileList: []
    };
  },
  mounted() {
    // 页面加载时从 sessionStorage 获取之前存储的数据
    const storedData = sessionStorage.getItem('fileUploadData');
    if (storedData) {
      this.tableData = JSON.parse(storedData); // 将存储的 JSON 字符串解析为数组
      this.visual = true; // 如果有数据，显示表格
    }
  },
  methods: {
    handleFileChange(file, fileList) {
      // 确保只处理一次上传
      if (file.status === 'ready') { // 仅在文件刚选择时处理
        this.fileList = fileList;
        this.parseFile(file.raw);
      }
    },
    handleFileRemove(file, fileList) {
      this.fileList = fileList;
      this.clearTable();
    },
    handleExceed(files, fileList) {
      this.$message.warning("超出文件上传数量限制");
    },
    beforeUpload(file) {
      const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
          file.type === 'application/vnd.ms-excel';
      if (!isExcel) {
        this.$message.error('只能上传 Excel 文件 (.xlsx 或 .xls)');
      }
      return isExcel;
    },
    parseFile(file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        const data = new Uint8Array(e.target.result);
        const workbook = XLSX.read(data, {type: 'array'});
        const sheetName = workbook.SheetNames[0]; // 获取第一个工作表
        const sheet = workbook.Sheets[sheetName];
        const rows = XLSX.utils.sheet_to_json(sheet, {header: 1}); // 将工作表转换为 JSON 数组

        // 清空现有数据
        this.tableData = [];

        // 解析数据并插入到 tableData 中
        rows.forEach(row => {
          const [aveLength, lisanValue] = row;

          // 检查 aveLength 和 lisanValue 是否为有效的正数
          if (
              typeof aveLength === 'number' &&
              typeof lisanValue === 'number' &&
              !isNaN(aveLength) &&
              !isNaN(lisanValue) &&
              aveLength > 0 &&
              lisanValue > 0
          ) {
            this.tableData.push({
              aveLength: parseFloat(aveLength),
              lisanValue: parseFloat(lisanValue)
            });
          }
        });

        // 使用 sessionStorage 保存数据
        sessionStorage.setItem('fileUploadData', JSON.stringify(this.tableData));
        this.$message.success("数据上传成功");
        this.visual = true;
        // 更新 Vuex 状态
        this.$store.commit("getInsertData", this.tableData);
      };
      reader.readAsArrayBuffer(file); // 以二进制格式读取文件
    },
    reset() {
      this.fileList = [];
      this.visual = this.tableData.length > 0; // 根据数据是否为空更新 visual 状态
    },
    clearTable() {
      this.tableData = [];
      this.visual = false;
      this.$message.warning("已清除所有数据");
      sessionStorage.removeItem('fileUploadData'); // 清除 sessionStorage 中的数据
      this.$store.commit("getInsertData", this.tableData); // 更新 Vuex 状态
    },
    goToNextPage() {
      // 跳转到 data2-next 页面
      this.$router.push('/data-next'); // 跳转路径为 /data2-next
    }
  }
};
</script>

<style lang="less" scoped>
.container {
  max-width: 860px;
  margin: 20px auto;
  padding: 24px;
  background-color: var(--bg-elevated) !important;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  color: var(--text-primary);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0; left: 10%; width: 80%; height: 1px;
    background: linear-gradient(90deg, transparent, var(--accent-primary), transparent);
    opacity: 0.3;
    border-radius: var(--radius-md);
  }
}

.demo-form-inline {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: flex-end;
}

.form-item { margin-bottom: 15px; }

.button-group { display: flex; gap: 10px; }

.image-container { text-align: center; margin-top: 20px; }

.preview-image {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  border: 1px solid var(--border-subtle);
}

.data-table {
  margin-top: 20px;
  background-color: var(--bg-elevated) !important;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--border-subtle) !important;
}

/deep/ .el-empty { background-color: transparent !important; }
/deep/ .el-empty__description p { color: var(--text-muted) !important; }
/deep/ .el-upload-list__item { color: var(--text-primary) !important; }
/deep/ .el-upload-list__item:hover { background: rgba(0,212,255,0.05) !important; }
</style>