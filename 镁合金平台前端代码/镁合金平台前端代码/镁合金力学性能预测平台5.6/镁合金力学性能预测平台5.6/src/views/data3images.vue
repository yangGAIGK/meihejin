<template>
  <div class="crack-visualization-container">
    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 右侧：统计图表 -->
      <div class="right-panel">
        <div class="chart-container">
          <h3 class="section-title">裂纹统计概览 (共 {{ crackCount }} 条裂纹)</h3>
          <div class="chart-wrapper" ref="overviewChart"></div>
        </div>
        <div class="chart-container">
          <h3 class="section-title">裂纹尺寸分布</h3>
          <div class="chart-wrapper" ref="sizeChart"></div>
        </div>
        <div class="chart-container">
          <h3 class="section-title">裂纹位置分布</h3>
          <div class="chart-wrapper" ref="positionChart"></div>
        </div>
        <div class="chart-container">
          <h3 class="section-title">裂纹详细信息</h3>
          <el-table :data="crackRecords" style="width: 100%" height="300">
            <el-table-column prop="CrackStart" label="起始位置" width="120"/>
            <el-table-column prop="CrackLength" label="长度(mm)" width="100"/>
            <el-table-column prop="CrackWidth" label="宽度(mm)" width="100"/>
            <el-table-column prop="CrackHeight" label="高度(mm)" width="100"/>
            <el-table-column prop="CrackArea" label="面积(mm²)" width="100"/>
            <el-table-column prop="CrackPerimeter" label="周长(mm)" width="100"/>
          </el-table>
        </div>
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
  },
  created() {
    // 页面加载时自动获取所有裂纹信息
    this.fetchAllCrackData();
  },
  methods: {
    // 获取所有图像表记录
    async fetchImageRecords() {
      try {
        const response = await axios.post('http://localhost:8080/imageQuery', {
          page: this.imageCurrentPage,
          pageSize: this.imagePageSize,
        });

        if (response.data.code === 1) {
          this.imageRecords = response.data.data.rows;
        } else {
          this.$message.error(response.data.msg || '获取图像记录失败');
        }
      } catch (error) {
        console.error('获取图像记录失败:', error);
        this.$message.error('获取图像记录失败，请稍后重试');
      }
    },
    // 循环获取所有裂纹数据
    async fetchAllCrackData() {
      try {
        // 先获取所有的imageRecords
        await this.fetchImageRecords()
        // 循环调用crackQuery，获取所有裂纹信息
        let allCracks = [];
        for (const imageRecord of this.imageRecords) {
          try {
            const response = await axios.post('http://localhost:8080/crackQuery', {
              ImageUrl: imageRecord.ImageUrl,
            });

            if (response.data.code === 1) {
              allCracks = allCracks.concat(response.data.data.rows);
            } else {
              console.warn(`获取 ${imageRecord.ImageUrl} 的裂纹信息失败: ${response.data.msg}`);
            }
          } catch (error) {
            console.error(`获取 ${imageRecord.ImageUrl} 的裂纹信息失败:`, error);
          }
        }

        this.crackRecords = allCracks; // 更新裂纹信息
        this.initCharts(); // 初始化图表
      } catch (error) {
        console.error('获取所有裂纹信息失败:', error);
        this.$message.error('获取所有裂纹信息失败，请稍后重试');
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

      const option = {
        tooltip: {
          trigger: 'item',
        },
        legend: {
          bottom: '5%',
          left: 'center',
          textStyle: {    // 调整图例文字大小
            fontSize: 12
          },
          itemGap: 10  // 调整图例间距
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
          },
        ],
        yAxis: [
          {
            gridIndex: 1,
            type: 'value',
            name: '数量',
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
              color: function (params) {
                const colorList = ['#c23531', '#2f4554', '#61a0a8'];
                return colorList[params.dataIndex];
              },
            },
          },
        ],
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
  padding: 20px;
  background-color: var(--bg-base);
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.right-panel {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chart-container {
  background: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: 16px;
  height: 400px;
}

.chart-wrapper {
  height: 300px;
  width: 100%;
}

.section-title {
  margin: 0 0 12px 0;
  color: var(--accent-primary);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-subtle);
}

/* 表格覆盖 */
/deep/ .el-table th.el-table__cell {
  background-color: var(--bg-overlay) !important;
  color: var(--accent-primary) !important;
  border-bottom: 1px solid var(--border-subtle) !important;
}

/deep/ .el-table tr,
/deep/ .el-table td.el-table__cell {
  background-color: var(--bg-elevated) !important;
  color: var(--text-primary) !important;
  border-bottom: 1px solid rgba(255,255,255,0.04) !important;
}

/deep/ .el-table__body tr:hover > td.el-table__cell {
  background-color: rgba(0,212,255,0.07) !important;
}
</style>


