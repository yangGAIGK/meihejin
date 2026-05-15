<template>
  <div class="container">
    <h1 class="title">bp历史记录</h1>

    <!-- 当没有数据时显示的空状态 -->
    <div class="image-container" v-if="!visual">
      <el-empty description="暂无数据" class="empty-state"></el-empty>
    </div>

    <!-- 表格组件 -->
    <div class="table-wrapper" v-else>
      <el-table
          border
          :data="table"
          class="custom-table"
          ref="table"
          v-loading="loading"
          @selection-change="handleSelectionChange"
          :header-cell-style="{ background: '#EBF2F9', color: '#333' }"
      >
        <!-- 勾选框列 -->
        <el-table-column type="selection" width="55"></el-table-column>

        <!-- 序号列 -->
        <el-table-column label="序号" width="60" align="center">
          <template slot-scope="scope">
            {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
          </template>
        </el-table-column>

        <!-- 其他数据列，按后端返回的字段名绑定 -->
        <el-table-column prop="inputLayer" label="输入层" min-width="200" align="center" />
        <el-table-column prop="outputLayer" label="输出层" min-width="200" align="center" />
        <el-table-column prop="intermediateLayer" label="中间层" min-width="200" align="center" />
        <el-table-column prop="options" label="选项" min-width="200" align="center" />
        <el-table-column prop="numberOfCycles" label="循环次数" min-width="200" align="center" />
        <el-table-column prop="learningRate" label="学习率" min-width="200" align="center" />
        <el-table-column prop="errorTargetValue" label="误差目标值" min-width="200" align="center" />
        <el-table-column prop="createTime" label="创建时间" min-width="200" align="center" />
        <el-table-column prop="updateTime" label="更新时间" min-width="200" align="center" />
      </el-table>
    </div>

    <!-- 按钮区域 -->
    <div class="button-container" v-if="visual">
      <el-button type="danger" @click="batchDelete" :disabled="selectedRows.length === 0" size="small">
        删除
      </el-button>
      <el-button type="success" @click="exportData" size="small">导出数据</el-button>
    </div>

    <!-- 分页组件 -->
    <el-pagination
        v-if="total > 0"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :hide-on-single-page="true"
        class="pagination"
    >
    </el-pagination>
  </div>
</template>

<script>
import axios from 'axios';
import ExportJsonExcel from 'js-export-excel';

export default {
  data() {
    return {
      visual: false,
      table: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      loading: false,
      selectedRows: [], // 用于存储选中的行数据
    };
  },
  methods: {
    async fetchHistoryData() {
      this.loading = true;
      try {
        const token = localStorage.getItem('Authorization');
        if (!token) {
          this.$message.error('请先登录');
          this.loading = false;
          return;
        }

        const response = await axios.post(
            'http://localhost:8080/BPrecords', // 确保URL正确
            {
              page: this.currentPage,
              pageSize: this.pageSize,
            },
            {
              headers: {
                Authorization: `Bearer ${token}`,
              },
            }
        );

        if (response.data.code === 1) {
          // 移除 createUser 字段
          this.table = response.data.data.records.map(record => {
            const { createUser, ...rest } = record;
            return rest;
          }) || [];
          this.total = response.data.data.total || 0;
          this.visual = true;
        } else {
          this.$message.error(response.data.msg || '获取历史记录失败');
          this.table = [];
          this.total = 0;
          this.visual = false;
        }
      } catch (error) {
        console.error('获取历史记录失败：', error);
        this.$message.error('网络错误，请稍后再试');
        this.visual = false;
      } finally {
        this.loading = false;
      }
    },
    handleSizeChange(newSize) {
      this.pageSize = newSize;
      this.fetchHistoryData();
    },
    handleCurrentChange(newPage) {
      this.currentPage = newPage;
      this.fetchHistoryData();
    },
    // 处理表格选择事件
    handleSelectionChange(selection) {
      this.selectedRows = selection;
    },
    // 批量删除
    async batchDelete() {
      try {
        // 提取选中的行的 ID
        const idsToDelete = this.selectedRows.map(row => row.id);

        // 确认删除
        const confirmDelete = await this.$confirm(
            `确定要删除选中的 ${idsToDelete.length} 条记录吗?`,
            '提示',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning',
            }
        );

        if (confirmDelete) {
          const token = localStorage.getItem('Authorization');
          const response = await axios.delete('http://localhost:8080/BPrecords/delete', {
            headers: {
              Authorization: `Bearer ${token}`,
            },
            data: { ids: idsToDelete }, // 将 ID 列表放在 data 中
          });

          if (response.data.code === 1) {
            this.$message.success('删除成功');
            this.fetchHistoryData(); // 刷新数据
            this.selectedRows = []; // 清空选择
            this.$refs.table.clearSelection(); // 清空表格的选择
          } else {
            this.$message.error(response.data.msg || '删除失败');
          }
        }
      } catch (error) {
        console.error('删除失败：', error);
        this.$message.error('删除过程中发生错误，请稍后再试');
      }
    },
    exportData() {
      const option = {};
      option.fileName = '历史记录';

      let sheetFilter = [
        'inputLayer',
        'outputLayer',
        'intermediateLayer',
        'options',
        'numberOfCycles',
        'learningRate',
        'errorTargetValue',
        'createTime',
        'updateTime',
      ];
      let sheetHeader = [
        '输入层',
        '输出层',
        '中间层',
        '选项',
        '循环次数',
        '学习率',
        '误差目标值',
        '创建时间',
        '更新时间',
      ];

      option.datas = [
        {
          sheetData: this.table,
          sheetName: 'sheet',
          sheetFilter: sheetFilter,
          sheetHeader: sheetHeader,
          columnWidths: [15, 15, 10, 10, 10, 10, 10, 20, 20],
        },
      ];
      option.type = 'xlsx';

      const toExcel = new ExportJsonExcel(option);
      toExcel.saveExcel();
    },
  },
  mounted() {
    this.fetchHistoryData();
  },
};
</script>

<style lang="less" scoped>
/*  主色调  */
@primary-color: #409eff;
/*  浅灰色背景  */
@light-gray-bg: #f0f2f5;
/*  文字颜色  */
@text-color: #333;
/*  辅助文字颜色  */
@sub-text-color: #999;
/*  边框颜色  */
@border-color: #dcdfe6;
/* 圆角大小 */
@border-radius: 4px;

.container {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  text-align: center;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Microsoft YaHei', Arial, sans-serif;
}

/* 标题样式 */
.title {
  font-size: 32px; /* 加大字号 */
  font-weight: 600; /* 加粗 */
  color: @text-color;
  margin-bottom: 20px;
  text-align: center; /* 居中 */
}

/* 表格 */
.table-wrapper {
  height: 550px;
  overflow-y: auto;
  border: 1px solid @border-color;
  border-radius: 4px;
}

.custom-table {
  width: 100%;
  border: none;

  /* 表头样式 */
  th {
    background-color: #ebf2f9;
    color: @text-color;
    font-weight: 500;
    text-align: center;
    padding: 12px 0;
    border-bottom: 1px solid @border-color;
  }

  /* 单元格样式 */
  td {
    text-align: center;
    font-size: 14px;
    color: @text-color;
    padding: 12px 0;
  }

  /* 斑马纹 */
  .el-table__body tr:nth-child(even) {
    background-color: #f9f9f9;
  }
}

/* 按钮区域 */
.button-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

/* 分页 */
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 空数据状态 */
.image-container {
  position: relative;
  height: 550px;
}

.empty-state {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  color: @sub-text-color;
}
</style>
