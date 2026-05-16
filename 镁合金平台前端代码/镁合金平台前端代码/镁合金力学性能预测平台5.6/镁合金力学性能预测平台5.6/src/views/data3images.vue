<template>
  <div class="crack-visualization-container">
    <!-- 顶部数据概览卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="card-label">裂纹总数</div>
        <div class="card-value">{{ crackCount }} <span class="unit">条</span></div>
        <i class="el-icon-warning-outline card-icon"></i>
      </div>
      <div class="stat-card">
        <div class="card-label">平均长度</div>
        <div class="card-value">{{ avgLength }} <span class="unit">mm</span></div>
        <i class="el-icon-guide card-icon"></i>
      </div>
      <div class="stat-card">
        <div class="card-label">平均宽度</div>
        <div class="card-value">{{ avgWidth }} <span class="unit">mm</span></div>
        <i class="el-icon-rank card-icon"></i>
      </div>
      <div class="stat-card">
        <div class="card-label">最大面积</div>
        <div class="card-value">{{ maxArea }} <span class="unit">mm²</span></div>
        <i class="el-icon-full-screen card-icon"></i>
      </div>
    </div>

    <!-- 主内容区：仪表盘布局 -->
    <div class="dashboard-grid">
      <!-- 第一排：大型概览图 + 数据列表 -->
      <div class="grid-item span-2 chart-card">
        <h3 class="section-title">
          <i class="el-icon-pie-chart"></i> 裂纹统计概览
        </h3>
        <div class="chart-wrapper large" ref="overviewChart"></div>
      </div>

      <div class="grid-item table-card">
        <h3 class="section-title">
          <i class="el-icon-tickets"></i> 裂纹详细数据
        </h3>
        <el-table :data="crackRecords" class="custom-table" height="350">
          <el-table-column prop="CrackLength" label="长度" width="70" align="center"/>
          <el-table-column prop="CrackWidth" label="宽度" width="70" align="center"/>
          <el-table-column prop="CrackArea" label="面积" min-width="80" align="center"/>
          <el-table-column label="位置" min-width="100" show-overflow-tooltip>
            <template slot-scope="scope">{{ scope.row.CrackStart }}</template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 第二排：分布对比图 -->
      <div class="grid-item chart-card">
        <h3 class="section-title">
          <i class="el-icon-sort"></i> 裂纹尺寸分布对比
        </h3>
        <div class="chart-wrapper" ref="sizeChart"></div>
      </div>

      <div class="grid-item span-2 chart-card">
        <h3 class="section-title">
          <i class="el-icon-location-outline"></i> 裂纹空间位置分布
        </h3>
        <div class="chart-wrapper" ref="positionChart"></div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import * as echarts from 'echarts';
import { format } from 'date-fns'; // 引入 date-fns 的 format 方法

export default {
  data() {
    return {
      imageRecords: [],
      imageTotal: 5, // 直接赋值，因为 total 只有 5 个
      imageCurrentPage: 1,
      imagePageSize: 10,
      crackRecords: [],

      overviewChart: null,
      sizeChart: null,
      positionChart: null,
    };
  },
  computed: {
    crackCount() {
      return this.crackRecords.length;
    },
    avgLength() {
      if (!this.crackCount) return 0;
      return (this.crackRecords.reduce((acc, c) => acc + (c.CrackLength || 0), 0) / this.crackCount).toFixed(2);
    },
    avgWidth() {
      if (!this.crackCount) return 0;
      return (this.crackRecords.reduce((acc, c) => acc + (c.CrackWidth || 0), 0) / this.crackCount).toFixed(2);
    },
    maxArea() {
      if (!this.crackCount) return 0;
      return Math.max(...this.crackRecords.map(c => c.CrackArea || 0)).toFixed(2);
    }
  },
  created() {
    // 页面加载时自动获取所有裂纹信息
    this.fetchAllCrackData();
  },
  methods: {
    // 获取所有图像表记录
    async fetchImageRecords() {
      try {
        const token = localStorage.getItem('Authorization');
        const response = await axios.post('http://localhost:8080/imageQuery', {
          page: this.imageCurrentPage,
          pageSize: this.imagePageSize,
        }, {
          headers: token ? { 'Authorization': `Bearer ${token}` } : {}
        });

        if (response.data.code === 1 && response.data.data) {
          this.imageRecords = response.data.data.rows || [];
          this.imageTotal = response.data.data.total || 0;
          return true;
        } else {
          console.warn('图像记录返回失败:', response.data.msg);
          return false;
        }
      } catch (error) {
        console.error('获取图像记录失败:', error);
        return false;
      }
    },
    // 循环获取所有裂纹数据
    async fetchAllCrackData() {
      this.loading = true;
      try {
        // 先获取所有的imageRecords
        const ok = await this.fetchImageRecords();
        if (!ok || this.imageRecords.length === 0) {
          this.$message.info('暂无裂纹图像数据，请先上传图像进行裂纹识别');
          this.loading = false;
          return;
        }

        const token = localStorage.getItem('Authorization');
        // 循环调用crackQuery，获取所有裂纹信息
        let allCracks = [];
        for (const imageRecord of this.imageRecords) {
          try {
            const imageUrl = imageRecord.ImageUrl || imageRecord.imageUrl;
            if (!imageUrl) continue;
            const response = await axios.post('http://localhost:8080/crackQuery', {
              ImageUrl: imageUrl,
            }, {
              headers: token ? { 'Authorization': `Bearer ${token}` } : {}
            });

            if (response.data.code === 1 && response.data.data && response.data.data.rows) {
              allCracks = allCracks.concat(response.data.data.rows);
            } else {
              console.warn(`获取 ${imageUrl} 的裂纹信息失败: ${response.data.msg}`);
            }
          } catch (error) {
            console.error(`单张裂纹查询失败，跳过:`, error);
          }
        }

        this.crackRecords = allCracks;
        if (allCracks.length === 0) {
          this.$message.info('当前图像暂无裂纹识别结果');
        } else {
          this.initCharts();
        }
      } catch (error) {
        console.error('获取裂纹数据异常:', error);
        this.$message.error('请求异常，请检查后端连接');
      } finally {
        this.loading = false;
      }
    },
    // 初始化所有图表
    initCharts() {
      if (!this.crackRecords || this.crackRecords.length === 0) return;

      this.initOverviewChart();
      this.initSizeChart();
      this.initPositionChart();
    },

    // 概览图表 (饼图+柱状图组合)
    initOverviewChart() {
      const chartDom = this.$refs.overviewChart;
      this.overviewChart = echarts.init(chartDom);

      // 按尺寸分类统计
      const sizeCategories = {
        small: this.crackRecords.filter(d => d.CrackLength < 10).length,
        medium: this.crackRecords.filter(
            d => d.CrackLength >= 10 && d.CrackLength < 30
        ).length,
        large: this.crackRecords.filter(d => d.CrackLength >= 30).length,
      };

      // 按宽度分类统计
      const widthCategories = {
        narrow: this.crackRecords.filter(d => d.CrackWidth < 0.5).length,
        normal: this.crackRecords.filter(
            d => d.CrackWidth >= 0.5 && d.CrackWidth < 1.0
        ).length,
        wide: this.crackRecords.filter(d => d.CrackWidth >= 1.0).length,
      };

      const chartColor = ['#00d4ff', '#7c3aed', '#f43f5e', '#fbbf24'];

      const option = {
        tooltip: {
          trigger: 'item',
        },
        legend: {
          bottom: '5%',
          left: 'center',
          textStyle: { color: '#8b949e', fontSize: 12 },
          itemGap: 20
        },
        grid: [
          { right: '70%', left: '10%', top: '15%', bottom: '60%' }, // 进一步调整right值
          { left: '70%', right: '10%', top: '50%', bottom: '25%' },  // 进一步调整left值
        ],
        xAxis: [
          {
            gridIndex: 1,
            type: 'category',
            data: ['小型(<10mm)', '中型(10-30mm)', '大型(≥30mm)'],
            axisLabel: { color: '#8b949e' }
          },
        ],
        yAxis: [
          {
            gridIndex: 1,
            type: 'value',
            name: '数量',
            nameTextStyle: { color: '#8b949e' },
            axisLabel: { color: '#8b949e' },
            splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } }
          },
        ],
        series: [
          {
            name: '裂纹长度分布',
            type: 'pie',
            radius: ['30%', '60%'],  // 进一步减小饼图半径
            center: ['25%', '40%'],
            data: [
              { value: sizeCategories.small, name: '小型(<10mm)' },
              { value: sizeCategories.medium, name: '中型(10-30mm)' },
              { value: sizeCategories.large, name: '大型(≥30mm)' },
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)',
              },
            },
          },
          {
            name: '裂纹宽度分布',
            type: 'bar',
            xAxisIndex: 0,
            yAxisIndex: 0,
            barWidth: '30%',   // 调整柱状图宽度
            data: [
              { value: widthCategories.narrow, name: '狭窄(<0.5mm)' },
              { value: widthCategories.normal, name: '正常(0.5-1.0mm)' },
              { value: widthCategories.wide, name: '宽(≥1.0mm)' },
            ],
            itemStyle: {
              borderRadius: [4, 4, 0, 0],
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#00d4ff' },
                { offset: 1, color: '#7c3aed' }
              ])
            },
          },
        ],
        backgroundColor: 'transparent'
      };

      this.overviewChart.setOption(option);
    },

    // 尺寸分布图表
    initSizeChart() {
      const chartDom = this.$refs.sizeChart;
      this.sizeChart = echarts.init(chartDom);

      // 取前10条数据展示
      const displayData = this.crackRecords.slice(0, 10);
      const crackIds = displayData.map((_, index) => `裂纹${index + 1}`);

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow',
          },
        },
        legend: {
          data: ['长度', '宽度', '高度'],
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true,
        },
        xAxis: {
          type: 'category',
          data: crackIds,
        },
        yAxis: {
          type: 'value',
          name: '尺寸(mm)',
        },
        series: [
          {
            name: '长度',
            type: 'bar',
            data: displayData.map(d => d.CrackLength),
          },
          {
            name: '宽度',
            type: 'bar',
            data: displayData.map(d => d.CrackWidth),
          },
          {
            name: '高度',
            type: 'bar',
            data: displayData.map(d => d.CrackHeight),
          },
        ],
      };

      this.sizeChart.setOption(option);
    },

    // 位置分布图表
    initPositionChart() {
      const chartDom = this.$refs.positionChart;
      this.positionChart = echarts.init(chartDom);

      // 解析坐标数据 (假设CrackStart格式为"(x,y)")
      const positionData = this.crackRecords.map(item => {
        const coords = item.CrackStart.replace(/[()]/g, '').split(',');
        return {
          value: [parseFloat(coords[0]), parseFloat(coords[1]), item.CrackArea],
          name: `面积:${item.CrackArea}mm²`,
        };
      });

      const option = {
        tooltip: {
          formatter: function (params) {
            return `位置: (${params.value[0]}, ${params.value[1]})<br>面积: ${params.value[2]}mm²`;
          },
        },
        xAxis: {
          type: 'value',
          name: 'X坐标',
          scale: true,
        },
        yAxis: {
          type: 'value',
          name: 'Y坐标',
          scale: true,
        },
        series: [
          {
            name: '裂纹位置',
            type: 'scatter',
            symbolSize: function (data) {
              return Math.sqrt(data[2]) * 0.5; // 根据面积调整点大小
            },
            data: positionData,
            itemStyle: {
              color: function (params) {
                // 根据面积大小设置不同颜色
                const area = params.value[2];
                if (area > 200) return '#c23531';
                if (area > 100) return '#2f4554';
                return '#61a0a8';
              },
              opacity: 0.8,
            },
            emphasis: {
              label: {
                show: true,
                formatter: function (params) {
                  return `面积:${params.value[2]}mm²`;
                },
                position: 'top',
              },
            },
          },
        ],
      };

      this.positionChart.setOption(option);
    },

    // 窗口大小变化时重绘图表
    handleResize() {
      if (this.overviewChart) this.overviewChart.resize();
      if (this.sizeChart) this.sizeChart.resize();
      if (this.positionChart) this.positionChart.resize();
    },
  },
  mounted() {
    window.addEventListener('resize', this.handleResize);
  },
  beforeDestroy() {
    if (this.overviewChart) this.overviewChart.dispose();
    if (this.sizeChart) this.sizeChart.dispose();
    if (this.positionChart) this.positionChart.dispose();
    window.removeEventListener('resize', this.handleResize);
  }
};
</script>

<style lang="less" scoped>
.crack-visualization-container {
  min-height: 100%;
  padding: 24px;
  background-color: var(--bg-base);
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 统计卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  padding: 20px;
  position: relative;
  overflow: hidden;
  box-shadow: var(--shadow-card);
  transition: transform 0.3s ease;

  &:hover { transform: translateY(-4px); }

  .card-label {
    font-size: 13px;
    color: var(--text-secondary);
    margin-bottom: 8px;
  }

  .card-value {
    font-size: 24px;
    font-weight: 700;
    color: var(--text-primary);
    
    .unit {
      font-size: 12px;
      color: var(--text-muted);
      margin-left: 4px;
    }
  }

  .card-icon {
    position: absolute;
    right: -10px;
    bottom: -10px;
    font-size: 60px;
    color: var(--accent-primary);
    opacity: 0.05;
  }
}

/* 仪表盘网格 */
.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-auto-rows: minmax(400px, auto);
  gap: 20px;
}

.grid-item {
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: 20px;
  box-shadow: var(--shadow-card);
  display: flex;
  flex-direction: column;
}

.span-2 { grid-column: span 2; }

.section-title {
  margin: 0 0 20px 0;
  color: var(--text-primary);
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;

  i { color: var(--accent-primary); }
}

.chart-wrapper {
  flex: 1;
  width: 100%;
  min-height: 300px;
  
  &.large { height: 350px; }
}

.custom-table {
  background: transparent !important;
  /deep/ .el-table__body tr:hover > td.el-table__cell {
    background: rgba(0,212,255,0.06) !important;
  }
}

/* 表格样式适配 */
/deep/ .el-table {
  background: transparent !important;
  color: var(--text-secondary) !important;
  
  &::before { display: none; }
  
  th.el-table__cell {
    background: rgba(0,212,255,0.05) !important;
    color: var(--accent-primary) !important;
    border-bottom: 1px solid rgba(0,212,255,0.1) !important;
  }
  
  td.el-table__cell {
    background: transparent !important;
    border-bottom: 1px solid rgba(255,255,255,0.03) !important;
  }
}
</style>


