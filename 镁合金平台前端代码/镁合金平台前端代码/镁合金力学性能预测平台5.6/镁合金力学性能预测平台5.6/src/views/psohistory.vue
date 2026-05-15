<template>
  <div class="container">
    <h1 class="title">历史记录</h1>

    <!-- 筛选条件 -->
    <div class="filter-container">
      <el-form :inline="true" class="filter-form" label-width="auto">
        <div class="filter-items">
          <el-form-item>
            <label class="custom-label">平均晶粒尺寸：</label>
            <div class="range-input">
              <el-input
                  v-model.number="filters.aveLengthMin"
                  placeholder="最小值"
                  size="small"
                  class="custom-input"
              ></el-input>
              <span class="separator">-</span>
              <el-input
                  v-model.number="filters.aveLengthMax"
                  placeholder="最大值"
                  size="small"
                  class="custom-input"
              ></el-input>
            </div>
          </el-form-item>

          <el-form-item>
            <label class="custom-label">离散特征值：</label>
            <div class="range-input">
              <el-input
                  v-model.number="filters.lisanValueMin"
                  placeholder="最小值"
                  size="small"
                  class="custom-input"
              ></el-input>
              <span class="separator">-</span>
              <el-input
                  v-model.number="filters.lisanValueMax"
                  placeholder="最大值"
                  size="small"
                  class="custom-input"
              ></el-input>
            </div>
          </el-form-item>

          <el-form-item>
            <label class="custom-label">硬度：</label>
            <div class="range-input">
              <el-input
                  v-model.number="filters.hardnessMin"
                  placeholder="最小值"
                  size="small"
                  class="custom-input"
              ></el-input>
              <span class="separator">-</span>
              <el-input
                  v-model.number="filters.hardnessMax"
                  placeholder="最大值"
                  size="small"
                  class="custom-input"
              ></el-input>
            </div>
          </el-form-item>

          <el-form-item>
            <label class="custom-label">屈服强度：</label>
            <div class="range-input">
              <el-input
                  v-model.number="filters.yieldStrengthMin"
                  placeholder="最小值"
                  size="small"
                  class="custom-input"
              ></el-input>
              <span class="separator">-</span>
              <el-input
                  v-model.number="filters.yieldStrengthMax"
                  placeholder="最大值"
                  size="small"
                  class="custom-input"
              ></el-input>
            </div>
          </el-form-item>

          <el-form-item>
            <label class="custom-label">抗拉强度：</label>
            <div class="range-input">
              <el-input
                  v-model.number="filters.strengthExtensionMin"
                  placeholder="最小值"
                  size="small"
                  class="custom-input"
              ></el-input>
              <span class="separator">-</span>
              <el-input
                  v-model.number="filters.strengthExtensionMax"
                  placeholder="最大值"
                  size="small"
                  class="custom-input"
              ></el-input>
            </div>
          </el-form-item>
        </div>

        <div class="filter-buttons">
          <el-button type="primary" class="filter-button" @click="filterData" size="small">
            <i class="el-icon-search"></i> 筛选
          </el-button>
          <el-button class="filter-button" @click="clearFilters" size="small">
            <i class="el-icon-close"></i> 取消筛选
          </el-button>
        </div>
      </el-form>
    </div>

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
          :header-cell-style="{ background: '#EBF2F9', color: '#333' }"
      >
        <!-- 勾选框列，根据 showCheckbox 决定是否显示 -->
        <el-table-column type="selection" width="55" v-if="showCheckbox"></el-table-column>

        <el-table-column label="序号" width="60" align="center">
          <template slot-scope="scope">
            {{ (currentPage - 1) * pageSize + scope.$index + 1 }}
          </template>
        </el-table-column>

        <!-- 其他数据列，按后端返回的字段名绑定 -->
        <el-table-column prop="aveLength" label="平均晶粒尺寸" min-width="160" align="center" />
        <el-table-column prop="lisanValue" label="离散特征值" min-width="160" align="center" />
        <el-table-column prop="hardness" label="硬度" min-width="200" align="center" />
        <el-table-column prop="yield_strength" label="屈服强度" min-width="200" align="center" />
        <el-table-column prop="strength_extension" label="抗拉强度" min-width="200" align="center" />

        <el-table-column
            v-if="showOptionalColumns"
            prop="maxNum"
            label="最大迭代次数"
            min-width="200"
            align="center"
        />
        <el-table-column
            v-if="showOptionalColumns"
            prop="topValue"
            label="上限值"
            min-width="200"
            align="center"
        />
        <el-table-column
            v-if="showOptionalColumns"
            prop="lowValue"
            label="下限值"
            min-width="200"
            align="center"
        />
        <el-table-column
            v-if="showOptionalColumns"
            prop="swarmSize"
            label="种群大小"
            min-width="200"
            align="center"
        />
        <el-table-column
            v-if="showOptionalColumns"
            prop="individualFactor"
            label="个体因子"
            min-width="200"
            align="center"
        />
        <el-table-column
            v-if="showOptionalColumns"
            prop="groupFactor"
            label="群体因子"
            min-width="200"
            align="center"
        />
        <el-table-column
            v-if="showOptionalColumns"
            prop="inertiaFactor"
            label="惯性因子"
            min-width="200"
            align="center"
        />

        <el-table-column prop="createTime" label="创建时间" min-width="200" align="center" />
        <el-table-column prop="updateTime" label="更新时间" min-width="200" align="center" />
      </el-table>
    </div>

    <!-- 按钮区域 -->
    <div class="button-container" v-if="visual">
      <el-button type="danger" @click="toggleCheckbox" v-if="!showCheckbox" size="small">删除</el-button>
      <el-button type="success" @click="exportData" v-if="!showCheckbox" size="small">导出数据</el-button>
      <template v-if="showCheckbox">
        <el-button type="danger" @click="batchDelete" size="small">确认删除</el-button>
        <el-button @click="toggleCheckbox" size="small">取消</el-button>
      </template>
      <el-button @click="toggleOptionalColumns" size="small">
        {{ showOptionalColumns ? '隐藏更多' : '显示更多' }}
      </el-button>
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
      // 新增：用于存储原始数据，包含 ID 和 UID
      originalTableData: [],
      filteredTable: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      loading: false,
      showCheckbox: false,
      filters: {
        aveLengthMin: null,
        aveLengthMax: null,
        lisanValueMin: null,
        lisanValueMax: null,
        hardnessMin: null,
        hardnessMax: null,
        yieldStrengthMin: null,
        yieldStrengthMax: null,
        strengthExtensionMin: null,
        strengthExtensionMax: null,
      },
      table: [],
      showOptionalColumns: false,
      tableHeight: 550,
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
            'http://localhost:8080/psohistory',
            {
              page: 1,
              pageSize: 10000,
            },
            {
              headers: {
                Authorization: `Bearer ${token}`,
              },
            }
        );

        if (response.data.code === 1) {
          // 保存原始数据
          this.originalTableData = response.data.data.records || [];
          this.filteredTable = response.data.data.records || [];
          this.total = response.data.data.total || 0;
          this.visual = true;
          this.table = this.processTableData(this.filteredTable); // 处理数据以隐藏 ID 和 UID
          this.filterData();
        } else {
          this.$message.error(response.data.msg || '获取历史记录失败');
          this.originalTableData = [];
          this.filteredTable = [];
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
    // 新增：处理表格数据，移除 ID 和 UID
    processTableData(data) {
      return data.map(({ id, uid, ...rest }) => rest);
    },
    filterData() {
      let filteredData = this.filteredTable.filter(item => {
        if (
            this.filters.aveLengthMin !== null &&
            item.aveLength < this.filters.aveLengthMin
        )
          return false;
        if (
            this.filters.aveLengthMax !== null &&
            item.aveLength > this.filters.aveLengthMax
        )
          return false;
        if (
            this.filters.lisanValueMin !== null &&
            item.lisanValue < this.filters.lisanValueMin
        )
          return false;
        if (
            this.filters.lisanValueMax !== null &&
            item.lisanValue > this.filters.lisanValueMax
        )
          return false;
        if (
            this.filters.hardnessMin !== null &&
            item.hardness < this.filters.hardnessMin
        )
          return false;
        if (
            this.filters.hardnessMax !== null &&
            item.hardness > this.filters.hardnessMax
        )
          return false;
        if (
            this.filters.yieldStrengthMin !== null &&
            item.yield_strength < this.filters.yieldStrengthMin
        )
          return false;
        if (
            this.filters.yieldStrengthMax !== null &&
            item.yield_strength > this.filters.yieldStrengthMax
        )
          return false;
        if (
            this.filters.strengthExtensionMin !== null &&
            item.strength_extension < this.filters.strengthExtensionMin
        )
          return false;
        if (
            this.filters.strengthExtensionMax !== null &&
            item.strength_extension > this.filters.strengthExtensionMax
        )
          return false;
        return true;
      });

      this.table = this.processTableData(filteredData.slice(0, this.pageSize));
      this.currentPage = 1;
    },
    handleSizeChange(newSize) {
      this.pageSize = newSize;
      this.filterData();
    },
    handleCurrentChange(newPage) {
      this.currentPage = newPage;
      const startIndex = (newPage - 1) * this.pageSize;
      const endIndex = newPage * this.pageSize;
      this.table = this.processTableData(this.filteredTable.slice(startIndex, endIndex));
    },
    toggleCheckbox() {
      this.showCheckbox = !this.showCheckbox;
      if (!this.showCheckbox) {
        this.$refs.table.clearSelection();
      }
    },
    async batchDelete() {
      try {
        // 使用原始数据获取选中的 ID
        const selectedIds = this.$refs.table.selection.map(item => {
          const originalItem = this.originalTableData.find(
              original =>
                  original.aveLength === item.aveLength &&
                  original.lisanValue === item.lisanValue &&
                  original.hardness === item.hardness &&
                  original.yield_strength === item.yield_strength &&
                  original.strength_extension === item.strength_extension
          );
          return originalItem ? originalItem.id : null;
        }).filter(id => id !== null);

        if (selectedIds.length === 0) {
          this.$message.warning('请至少选择一条记录');
          return;
        }

        const confirmDelete = await this.$confirm(
            '确定要删除选中的记录吗？',
            '提示',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning',
            }
        );

        if (confirmDelete) {
          const response = await axios.delete(
              'http://localhost:8080/PSOHistory/delete',
              {
                data: { ids: selectedIds },
              }
          );

          if (response.data.code === 1) {
            this.$message.success('删除成功');
            this.fetchHistoryData();
            this.filterData();
            this.showCheckbox = false;
          } else {
            this.$message.error(response.data.msg || '删除失败');
          }
        }
      } catch (error) {
        this.$message.info('已取消删除');
      }
    },
    clearFilters() {
      this.filters = {
        aveLengthMin: null,
        aveLengthMax: null,
        lisanValueMin: null,
        lisanValueMax: null,
        hardnessMin: null,
        hardnessMax: null,
        yieldStrengthMin: null,
        yieldStrengthMax: null,
        strengthExtensionMin: null,
        strengthExtensionMax: null,
      };
      this.filterData();
    },
    exportData() {
      const option = {};
      option.fileName = '历史记录';

      // 在导出数据之前过滤掉 ID 和 UID 列
      const filteredForExport = this.filteredTable.map(item => {
        const { id, uid, ...rest } = item; // 使用对象解构来移除 id 和 uid 属性
        return rest;
      });

      let sheetFilter = [
        'aveLength',
        'lisanValue',
        'hardness',
        'yield_strength',
        'strength_extension',
        'createTime',
        'updateTime',
      ];
      let sheetHeader = [
        '平均晶粒尺寸',
        '离散特征值',
        '硬度',
        '屈服强度',
        '抗拉强度',
        '创建时间',
        '更新时间',
      ];

      if (this.showOptionalColumns) {
        sheetFilter = [
          'aveLength',
          'lisanValue',
          'hardness',
          'yield_strength',
          'strength_extension',
          'maxNum',
          'topValue',
          'lowValue',
          'swarmSize',
          'individualFactor',
          'groupFactor',
          'inertiaFactor',
          'createTime',
          'updateTime',
        ];
        sheetHeader = [
          '平均晶粒尺寸',
          '离散特征值',
          '硬度',
          '屈服强度',
          '抗拉强度',
          '最大迭代次数',
          '上限值',
          '下限值',
          '种群大小',
          '个体因子',
          '群体因子',
          '惯性因子',
          '创建时间',
          '更新时间',
        ];
      }

      option.datas = [
        {
          sheetData: filteredForExport, // 使用过滤后的数据进行导出
          sheetName: 'sheet',
          sheetFilter: sheetFilter,
          sheetHeader: sheetHeader,
          columnWidths: [15, 15, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 20, 20],
        },
      ];
      option.type = 'xlsx';

      const toExcel = new ExportJsonExcel(option);
      toExcel.saveExcel();
    },
    toggleOptionalColumns() {
      this.showOptionalColumns = !this.showOptionalColumns;
    },
  },
  async mounted() {
    await this.fetchHistoryData();
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
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2); /* 添加文字阴影 */
  position: relative; /*  为伪元素定位做准备  */
  padding-bottom: 10px; /*  为下划线留出空间  */
  display: inline-block; /* 让标题宽度自适应内容 */
}

/* 标题下方的横线 */
.title::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: -5px; /* 调整横线与文字的距离 */
  transform: translateX(-50%);
  width: 160%; /*  横线比文字更长  */
  height: 2px; /*  横线高度  */
  background-color: @border-color; /*  横线颜色  */
}

/*  筛选条件  */
.filter-container {
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  text-align: center;
}

.filter-form {
  width: 100%;
}

/*  filter-items 样式 */
.filter-items {
  display: flex; /* 使用 Flexbox 布局 */
  flex-wrap: wrap; /* 允许换行 */
  justify-content: flex-start; /* 左对齐 */
}

.el-form-item {
  margin-bottom: 12px;
  text-align: left;
  width: 48%; /* 调整宽度，每行显示两个 */
  display: flex; /* 使用 Flexbox 布局 */
  align-items: center; /* 垂直居中 */
}

/* 自定义 Label 样式 */
.custom-label {
  color: @sub-text-color;
  font-weight: normal;
  margin-right: 6px; /* 与输入框间隔 */
  text-align: left;
  width: auto; /* 宽度自适应 */
  order: -1; /* 将label放在前面 */
  white-space: nowrap; /* 防止文字换行 */
  display: inline-block; /* 设置为内联块元素 */
  font-size: 16px; /* 增大筛选条件文字字号 */
}

/* 范围输入框样式 */
.range-input {
  display: flex; /* 使用Flex布局 */
  /* align-items: center; */ /* 移除垂直居中 */
  justify-content: space-around; /* 均匀分布子元素 */
  width: 100%; /* 占据全部宽度 */
}

.range-input .el-input {
  width: 45%; /* 调整输入框宽度，留出间隔 */
  border-radius: @border-radius;
}

/* Input 之间的分隔符 */
.range-input .separator {
  margin: 0; /* 移除 Separator 的 Margin */
  color: @sub-text-color;
}

/* 自定义输入框样式 */
.custom-input {
  border: none;
  background-color: transparent;
  color: @text-color;
  padding: 6px 12px;
  border-radius: @border-radius;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.06);
}

.custom-input:focus {
  outline: none;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.12);
}

/* 筛选按钮样式 */
.filter-buttons {
  text-align: center;
  margin-top: 20px;
}

.filter-button {
  border-radius: @border-radius;
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

/* 调整表格列宽 */
.el-table-column[prop="aveLength"] {
  min-width: 160px; /* 调整平均晶粒尺寸列宽 */
}

.el-table-column[prop="lisanValue"] {
  min-width: 160px; /* 调整离散特征值列宽 */
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
