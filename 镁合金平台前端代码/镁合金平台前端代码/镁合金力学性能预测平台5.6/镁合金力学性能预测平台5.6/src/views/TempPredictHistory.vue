<template>
  <div class="temp-predict-container">
    <el-card class="box-card" style="margin-bottom: 20px;">
      <div slot="header" class="clearfix">
        <span>温度预测 - 历史记录</span>
      </div>
      
      <div class="chart-header" style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
        <span style="font-weight: bold; font-size: 16px;">温度预测趋势分析</span>
        <div>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            style="margin-right: 10px;">
          </el-date-picker>
          <el-button type="primary" icon="el-icon-search" @click="filterData">查询</el-button>
        </div>
      </div>
      
      <div id="trendChart" style="height: 300px; width: 100%;"></div>
    </el-card>

    <el-card class="box-card">
      <div class="action-bar" style="margin-bottom: 20px;">
        <el-button type="danger" icon="el-icon-delete" @click="batchDelete" :disabled="selectedRows.length === 0">批量删除</el-button>
        <el-button type="primary" icon="el-icon-refresh" @click="loadHistory">刷新列表</el-button>
      </div>

      <el-table 
        :data="displayList" 
        style="width: 100%" 
        stripe 
        border
        @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center"></el-table-column>
        <el-table-column type="index" label="序号" width="80" align="center"></el-table-column>
        <el-table-column prop="fileName" label="文件名" align="center"></el-table-column>
        <el-table-column prop="temp" label="预测温度 (℃)" width="150" align="center">
          <template slot-scope="scope">
            <span style="color: #67C23A; font-weight: bold;">{{ scope.row.temp }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="预测时间" width="200" align="center"></el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button type="text" style="color: #F56C6C;" @click="removeItem(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div style="text-align: right; margin-top: 20px;">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredList.length">
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import axios from 'axios';

export default {
  data() {
    return {
      historyList: [],
      filteredList: [],
      selectedRows: [],
      dateRange: null,
      chartInstance: null,
      currentPage: 1,
      pageSize: 10,
      baseUrl: 'http://localhost:8080'
    };
  },
  computed: {
    displayList() {
      const start = (this.currentPage - 1) * this.pageSize;
      const end = start + this.pageSize;
      return this.filteredList.slice(start, end);
    }
  },
  mounted() {
    this.loadHistory();
    window.addEventListener('resize', this.resizeChart);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeChart);
    if (this.chartInstance) {
      this.chartInstance.dispose();
    }
  },
  methods: {
    getHeaders() {
      const token = localStorage.getItem('Authorization');
      return { Authorization: token || '' };
    },
    async loadHistory() {
      try {
        const res = await axios.get(`${this.baseUrl}/tempHistory/list`, { headers: this.getHeaders() });
        if (res.data && res.data.code === 1) {
          // 格式化时间
          this.historyList = res.data.data.map(item => {
            return {
              ...item,
              time: item.time ? item.time.replace('T', ' ').substring(0, 19) : ''
            }
          });
          this.filterData();
        } else {
          this.$message.error(res.data.msg || '获取历史记录失败');
        }
      } catch (error) {
        console.error(error);
        this.$message.error('获取历史记录请求失败');
      }
    },
    filterData() {
      if (this.dateRange && this.dateRange.length === 2) {
        const start = new Date(this.dateRange[0] + ' 00:00:00').getTime();
        const end = new Date(this.dateRange[1] + ' 23:59:59').getTime();
        this.filteredList = this.historyList.filter(item => {
          const itemTime = new Date(item.time.replace(' ', 'T')).getTime();
          return itemTime >= start && itemTime <= end;
        });
      } else {
        this.filteredList = [...this.historyList];
      }
      this.currentPage = 1;
      this.updateChart();
    },
    handleSelectionChange(val) {
      this.selectedRows = val;
    },
    removeItem(id) {
      this.$confirm('确定删除该条记录吗?', '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          const res = await axios.delete(`${this.baseUrl}/tempHistory/delete/${id}`, { headers: this.getHeaders() });
          if (res.data && res.data.code === 1) {
            this.$message.success('删除成功');
            this.loadHistory();
          } else {
            this.$message.error(res.data.msg || '删除失败');
          }
        } catch (error) {
          console.error(error);
          this.$message.error('删除请求失败');
        }
      }).catch(() => {});
    },
    batchDelete() {
      this.$confirm(`确定删除选中的 ${this.selectedRows.length} 条记录吗?`, '提示', {
        type: 'warning'
      }).then(async () => {
        try {
          const ids = this.selectedRows.map(row => row.id);
          const res = await axios.post(`${this.baseUrl}/tempHistory/batchDelete`, ids, { headers: this.getHeaders() });
          if (res.data && res.data.code === 1) {
            this.$message.success('批量删除成功');
            this.loadHistory();
          } else {
            this.$message.error(res.data.msg || '批量删除失败');
          }
        } catch (error) {
          console.error(error);
          this.$message.error('批量删除请求失败');
        }
      }).catch(() => {});
    },
    updateChart() {
      if (!this.chartInstance) {
        this.chartInstance = echarts.init(document.getElementById('trendChart'));
      }
      
      // Sort data chronologically for the chart
      const chartData = [...this.filteredList].sort((a, b) => new Date(a.time) - new Date(b.time));
      
      const xAxisData = chartData.map(item => item.time);
      const seriesData = chartData.map(item => item.temp);
      
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: xAxisData,
          name: '时间'
        },
        yAxis: {
          type: 'value',
          name: '温度 (℃)'
        },
        series: [
          {
            name: '预测温度',
            type: 'line',
            data: seriesData,
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(103, 194, 58, 0.5)' },
                { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
              ])
            },
            itemStyle: {
              color: '#67C23A'
            },
            smooth: true
          }
        ]
      };
      
      this.chartInstance.setOption(option);
    },
    resizeChart() {
      if (this.chartInstance) {
        this.chartInstance.resize();
      }
    },
    handleSizeChange(val) {
      this.pageSize = val;
    },
    handleCurrentChange(val) {
      this.currentPage = val;
    }
  }
}
</script>

<style scoped>
.temp-predict-container {
  padding: 20px;
}
</style>
