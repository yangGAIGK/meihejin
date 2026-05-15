<template>
  <el-container class="visualization-container">
    <el-aside width="200px" class="aside">
      <h2>数据选择</h2>

      <el-button type="primary" @click="showDatabaseChart('bphistory')">
        BP图
      </el-button>
      <el-button type="primary" @click="showDatabaseChart('psohistory')">
        PSO_BP图
      </el-button>
      <el-button type="primary" @click="showDatabaseChart('psobphistory')">
        PSO_BP_GA图
      </el-button>
      <el-button type="success" @click="showTotalChart">
        查看总和
      </el-button>
    </el-aside>

    <el-main>
      <h1 class="main-title">数据可视化</h1>

      <div class="main-content">
        <div class="left-panel">
          <!-- 柱状图：平均晶粒尺寸 -->
          <div class="chart-container" :style="{ height: leftChartHeight + 'px' }">
            <h2>平均晶粒尺寸分布</h2>
            <div ref="grainSizeChart" class="chart"></div>
          </div>

          <!-- 柱状图：离散特征值 -->
          <div class="chart-container" :style="{ height: leftChartHeight + 'px' }">
            <h2>离散特征值分布</h2>
            <div ref="lisanValueChart" class="chart"></div>
          </div>
        </div>

        <div class="right-panel">
          <!-- 饼图：硬度分布 -->
          <div class="chart-container" :style="{ height: rightChartHeight + 'px' }">
            <h2>硬度分布</h2>
            <div ref="hardnessChart" class="chart"></div>
          </div>

          <!-- 饼图：屈服强度分布 -->
          <div class="chart-container" :style="{ height: rightChartHeight + 'px' }">
            <h2>屈服强度分布</h2>
            <div ref="yieldStrengthChart" class="chart"></div>
          </div>

          <!-- 饼图：抗拉强度分布 -->
          <div class="chart-container" :style="{ height: rightChartHeight + 'px' }">
            <h2>抗拉强度分布</h2>
            <div ref="strengthExtensionChart" class="chart"></div>
          </div>
        </div>
      </div>
    </el-main>
  </el-container>
</template>

<script>
import * as echarts from 'echarts';
import axios from 'axios';
import { ElMessage } from 'element-ui';

export default {
  data() {
    return {
      historyData: [],
      loading: false,
      error: false,
      errorMessage: '',
      grainSizeChartInstance: null,
      lisanValueChartInstance: null,
      hardnessChartInstance: null,
      yieldStrengthChartInstance: null,
      strengthExtensionChartInstance: null,
      leftChartHeight: 475, // 调整后的左侧图表高度
      rightChartHeight: 300, // 调整后的右侧图表高度
      selectedDatabase: 'bphistory', // 默认选中 BP History
      baseURL: 'http://localhost:8080',
    };
  },
  mounted() {
    this.fetchHistoryData(this.selectedDatabase); // 默认加载 BP History
  },
  beforeDestroy() {
    this.disposeCharts();
  },
  methods: {
    disposeCharts() {
      if (this.grainSizeChartInstance) {
        this.grainSizeChartInstance.dispose();
        this.grainSizeChartInstance = null;
      }
      if (this.lisanValueChartInstance) {
        this.lisanValueChartInstance.dispose();
        this.lisanValueChartInstance = null;
      }
      if (this.hardnessChartInstance) {
        this.hardnessChartInstance.dispose();
        this.hardnessChartInstance = null;
      }
      if (this.yieldStrengthChartInstance) {
        this.yieldStrengthChartInstance.dispose();
        this.yieldStrengthChartInstance = null;
      }
      if (this.strengthExtensionChartInstance) {
        this.strengthExtensionChartInstance.dispose();
        this.strengthExtensionChartInstance = null;
      }
    },
    async fetchHistoryData(database = '') {
      this.loading = true;
      this.error = false;
      try {
        const token = localStorage.getItem('Authorization');
        if (!token) {
          this.error = true;
          this.errorMessage = '请先登录';
          return;
        }

        let apiUrl = this.baseURL;
        if (database) {
          apiUrl += `/${database}`;
        } else {
          apiUrl = null;
        }

        if (apiUrl) {
          const response = await axios.post(
              apiUrl,
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

          console.log('API Response for ' + database + ':', response);

          if (response.data.code === 1) {
            this.historyData = response.data.data.records || [];
            console.log('History Data for ' + database + ':', this.historyData);
            this.$nextTick(() => {
              this.initCharts();
            });
          } else {
            this.error = true;
            this.errorMessage = response.data.msg || '获取历史记录失败';
            ElMessage.error(this.errorMessage);
          }
        } else {
          this.historyData = [];
          this.$nextTick(() => {
            this.initCharts();
          });
        }
      } catch (error) {
        console.error('获取历史记录失败：', error);
        this.error = true;
        this.errorMessage = '网络错误，请稍后再试';
        ElMessage.error(this.errorMessage);
      } finally {
        this.loading = false;
      }
    },
    initCharts() {
      if (this.historyData && this.historyData.length > 0) {
        this.disposeCharts();
        this.$nextTick(() => {
          this.initGrainSizeChart();
          this.initLisanValueChart();
          this.initHardnessChart();
          this.initYieldStrengthChart();
          this.initStrengthExtensionChart();
        });
      } else {
        console.warn('historyData is empty or undefined. Charts will not be initialized.');
        ElMessage.info('数据加载中或数据为空');
      }
    },
    initGrainSizeChart() {
      const chartDom = this.$refs.grainSizeChart;
      if (chartDom) {
        this.grainSizeChartInstance = echarts.init(chartDom);

        const grainSizeData = this.historyData.map(item => item.aveLength);
        const grainSizeIntervalData = this.calculateIntervalData(grainSizeData, 4, 7, 0.5, true);

        const option = {
          backgroundColor: 'transparent',
          color: ['#00d4ff', '#7c3aed', '#22c55e', '#f59e0b', '#ef4444'],
          textStyle: { color: '#8b949e' },
          tooltip: {
            trigger: 'axis',
            backgroundColor: 'rgba(22,27,34,0.95)',
            borderColor: 'rgba(0,212,255,0.2)',
            textStyle: { color: '#e6edf3' },
            axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(0,212,255,0.05)' } }
          },
          legend: { data: ['平均晶粒尺寸'], textStyle: { color: '#8b949e' } },
          grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
          xAxis: {
            type: 'category',
            name: '平均晶粒尺寸范围',
            nameTextStyle: { color: '#8b949e' },
            axisLabel: { color: '#8b949e' },
            axisLine: { lineStyle: { color: 'rgba(0,212,255,0.2)' } },
            splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } },
            data: grainSizeIntervalData.map(item => item.interval),
          },
          yAxis: {
            type: 'value',
            name: '数据量',
            nameTextStyle: { color: '#8b949e' },
            axisLabel: { color: '#8b949e' },
            axisLine: { lineStyle: { color: 'rgba(0,212,255,0.2)' } },
            splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } }
          },
          series: [{ name: '平均晶粒尺寸', type: 'bar', data: grainSizeIntervalData.map(item => item.count), itemStyle: { borderRadius: [3, 3, 0, 0] } }]
        };

        this.grainSizeChartInstance.setOption(option);
      } else {
        console.warn('chartDom is null for grainSizeChart. Chart will not be initialized.');
      }
    },

    initLisanValueChart() {
      const chartDom = this.$refs.lisanValueChart;
      if (chartDom) {
        this.lisanValueChartInstance = echarts.init(chartDom);

        const lisanValueData = this.historyData.map(item => item.lisanValue);
        const lisanValueIntervalData = this.calculateIntervalData(lisanValueData, 0.2, 0.7, 0.1, true);

        const option = {
          backgroundColor: 'transparent',
          color: ['#7c3aed', '#00d4ff', '#22c55e', '#f59e0b'],
          textStyle: { color: '#8b949e' },
          tooltip: { trigger: 'axis', backgroundColor: 'rgba(22,27,34,0.95)', borderColor: 'rgba(0,212,255,0.2)', textStyle: { color: '#e6edf3' }, axisPointer: { type: 'shadow' } },
          legend: { data: ['离散特征值'], textStyle: { color: '#8b949e' } },
          grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true },
          xAxis: { type: 'category', name: '离散特征值范围', nameTextStyle: { color: '#8b949e' }, axisLabel: { color: '#8b949e' }, axisLine: { lineStyle: { color: 'rgba(0,212,255,0.2)' } }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } }, data: lisanValueIntervalData.map(item => item.interval) },
          yAxis: { type: 'value', name: '数据量', nameTextStyle: { color: '#8b949e' }, axisLabel: { color: '#8b949e' }, axisLine: { lineStyle: { color: 'rgba(0,212,255,0.2)' } }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.04)' } } },
          series: [{ name: '离散特征值', type: 'bar', data: lisanValueIntervalData.map(item => item.count), itemStyle: { borderRadius: [3, 3, 0, 0] } }]
        };

        this.lisanValueChartInstance.setOption(option);
      } else {
        console.warn('chartDom is null for lisanValueChart. Chart will not be initialized.');
      }
    },

    initHardnessChart() {
      const chartDom = this.$refs.hardnessChart;
      if (chartDom) {
        this.hardnessChartInstance = echarts.init(chartDom);

        const hardnessData = this.generatePieChartDataByRange(this.historyData, 'hardness', 55, 60, 1);

        const option = {
          backgroundColor: 'transparent',
          color: ['#00d4ff','#7c3aed','#22c55e','#f59e0b','#ef4444','#06b6d4','#a855f7'],
          textStyle: { color: '#8b949e' },
          tooltip: { trigger: 'item', backgroundColor: 'rgba(22,27,34,0.95)', borderColor: 'rgba(0,212,255,0.2)', textStyle: { color: '#e6edf3' }, formatter: '{a} <br/>{b} : {c} ({d}%)' },
          legend: { orient: 'vertical', left: 'left', textStyle: { color: '#8b949e' }, data: hardnessData.map(item => item.name) },
          series: [{ name: '硬度', type: 'pie', radius: '45%', center: ['50%', '55%'], data: hardnessData, emphasis: { itemStyle: { shadowBlur: 15, shadowColor: 'rgba(0,212,255,0.4)' } }, label: { color: '#8b949e' } }]
        };

        this.hardnessChartInstance.setOption(option);
      } else {
        console.warn('chartDom is null for hardnessChart. Chart will not be initialized.');
      }
    },

    initYieldStrengthChart() {
      const chartDom = this.$refs.yieldStrengthChart;
      if (chartDom) {
        this.yieldStrengthChartInstance = echarts.init(chartDom);

        const yieldStrengthData = this.generatePieChartDataByRange(this.historyData, 'yieldStrength', 150, 200, 10);

        const option = {
          backgroundColor: 'transparent',
          color: ['#7c3aed','#00d4ff','#22c55e','#f59e0b','#ef4444','#06b6d4','#a855f7'],
          textStyle: { color: '#8b949e' },
          tooltip: { trigger: 'item', backgroundColor: 'rgba(22,27,34,0.95)', borderColor: 'rgba(0,212,255,0.2)', textStyle: { color: '#e6edf3' }, formatter: '{a} <br/>{b} : {c} ({d}%)' },
          legend: { orient: 'vertical', left: 'left', textStyle: { color: '#8b949e' }, data: yieldStrengthData.map(item => item.name) },
          series: [{ name: '屈服强度', type: 'pie', radius: '45%', center: ['50%', '55%'], data: yieldStrengthData, emphasis: { itemStyle: { shadowBlur: 15, shadowColor: 'rgba(124,58,237,0.4)' } }, label: { color: '#8b949e' } }]
        };

        this.yieldStrengthChartInstance.setOption(option);
      } else {
        console.warn('chartDom is null for yieldStrengthChart. Chart will not be initialized.');
      }
    },

    initStrengthExtensionChart() {
      const chartDom = this.$refs.strengthExtensionChart;
      if (chartDom) {
        this.strengthExtensionChartInstance = echarts.init(chartDom);

        const strengthExtensionData = this.generatePieChartDataByRange(this.historyData, 'strengthExtension', 200, 260, 10);

        const option = {
          backgroundColor: 'transparent',
          color: ['#22c55e','#00d4ff','#7c3aed','#f59e0b','#ef4444','#06b6d4','#a855f7'],
          textStyle: { color: '#8b949e' },
          tooltip: { trigger: 'item', backgroundColor: 'rgba(22,27,34,0.95)', borderColor: 'rgba(0,212,255,0.2)', textStyle: { color: '#e6edf3' }, formatter: '{a} <br/>{b} : {c} ({d}%)' },
          legend: { orient: 'vertical', left: 'left', textStyle: { color: '#8b949e' }, data: strengthExtensionData.map(item => item.name) },
          series: [{ name: '抗拉强度', type: 'pie', radius: '45%', center: ['50%', '55%'], data: strengthExtensionData, emphasis: { itemStyle: { shadowBlur: 15, shadowColor: 'rgba(34,197,94,0.4)' } }, label: { color: '#8b949e' } }]
        };

        this.strengthExtensionChartInstance.setOption(option);
      } else {
        console.warn('chartDom is null for strengthExtensionChart. Chart will not be initialized.');
      }
    },

    generatePieChartDataByRange(data, key, rangeStart, rangeEnd, step) {
      const pieData = [];

      const lessThanStart = data.filter(item => item[key] < rangeStart).length;
      pieData.push({ value: lessThanStart, name: `<${rangeStart}` });

      for (let i = rangeStart; i < rangeEnd; i += step) {
        const rangeName = `${i}-${i + step}`;
        const rangeCount = data.filter(item => item[key] >= i && item[key] < i + step).length;
        pieData.push({ value: rangeCount, name: rangeName });
      }

      const greaterThanEnd = data.filter(item => item[key] >= rangeEnd).length;
      pieData.push({ value: greaterThanEnd, name: `>${rangeEnd}` });

      return pieData;
    },

    calculateIntervalData(data, min, max, step, includeOverMax = false) {
      const intervals = [];
      for (let i = min; i < max; i += step) {
        const intervalStart = i.toFixed(1);
        const intervalEnd = (i + step).toFixed(1);
        const interval = `${intervalStart}-${intervalEnd}`;
        const count = data.filter(value => value >= i && value < i + step).length;
        intervals.push({ interval, count });
      }

      if (includeOverMax) {
        const overMaxCount = data.filter(value => value >= max).length;
        intervals.push({ interval: `>${max.toFixed(1)}`, count: overMaxCount });
      }

      return intervals;
    },

    showDatabaseChart(database) {
      this.selectedDatabase = database;
      this.fetchHistoryData(database);
    },

    async showTotalChart() {
      this.selectedDatabase = '';
      this.loading = true;
      this.error = false;

      try {
        const token = localStorage.getItem('Authorization');
        if (!token) {
          this.error = true;
          this.errorMessage = '请先登录';
          return;
        }

        const [bpResponse, psoResponse, psobpResponse] = await Promise.all([
          axios.post(
              `${this.baseURL}/bphistory`,
              { page: 1, pageSize: 10000 },
              { headers: { Authorization: `Bearer ${token}` } }
          ),
          axios.post(
              `${this.baseURL}/psohistory`,
              { page: 1, pageSize: 10000 },
              { headers: { Authorization: `Bearer ${token}` } }
          ),
          axios.post(
              `${this.baseURL}/psobphistory`,
              { page: 1, pageSize: 10000 },
              { headers: { Authorization: `Bearer ${token}` } }
          ),
        ]);

        console.log('API Responses for Total:', { bpResponse, psoResponse, psobpResponse });

        if (
            bpResponse.data.code === 1 &&
            psoResponse.data.code === 1 &&
            psobpResponse.data.code === 1
        ) {
          const bpData = bpResponse.data.data.records || [];
          const psoData = psoResponse.data.data.records || [];
          const psobpData = psobpResponse.data.data.records || [];

          this.historyData = [...bpData, ...psoData, ...psobpData];
          console.log('Combined History Data for Total:', this.historyData);

          this.$nextTick(() => {
            this.initCharts();
          });
        } else {
          this.error = true;
          this.errorMessage = '获取历史记录失败';
          ElMessage.error(this.errorMessage);
        }
      } catch (error) {
        console.error('获取历史记录失败：', error);
        this.error = true;
        this.errorMessage = '网络错误，请稍后再试';
        ElMessage.error(this.errorMessage);
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
.visualization-container {
  height: 100vh;
  display: flex;
  background-color: var(--bg-base);
}

.aside {
  background-color: var(--bg-surface) !important;
  padding: 20px;
  border-right: 1px solid var(--border-subtle);
  display: flex;
  flex-direction: column;
}

.aside h2 {
  font-size: 1.2em;
  font-weight: 600;
  margin-bottom: 20px;
  text-align: center;
  color: var(--accent-primary);
  border-bottom: 1px solid var(--border-subtle);
  padding-bottom: 12px;
  letter-spacing: 1px;
}

.aside .el-button {
  margin: 8px 0 0 0;
  width: 100%;
  border-radius: var(--radius-sm) !important;
  transition: all 0.3s ease;
}

.aside .el-button:hover {
  transform: translateY(-2px);
}

.el-main {
  padding: 20px;
  background-color: var(--bg-base);
}

.main-title {
  font-size: 1.8em;
  font-weight: 700;
  margin-bottom: 20px;
  text-align: center;
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.main-content {
  display: flex;
  min-height: 600px;
  gap: 16px;
}

.left-panel {
  width: 40%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.right-panel {
  width: 60%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.chart-container {
  padding: 16px;
  background-color: var(--bg-surface) !important;
  border: 1px solid var(--border-subtle) !important;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
}

.chart {
  width: 100%;
  height: 100%;
}

.chart-container h2 {
  font-size: 1em;
  font-weight: 600;
  margin-bottom: 10px;
  color: var(--text-secondary) !important;
  border-bottom: 1px solid var(--border-subtle);
  padding-bottom: 8px;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  font-size: 12px;
}
</style>
