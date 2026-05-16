<template>
  <div class="batch-page">
    <!-- 主操作区 -->
    <el-card class="main-card">
      <div slot="header" class="card-header">
        <span class="card-title"><i class="el-icon-picture-outline-round"></i> 批量温度预测</span>
        <div class="header-actions">
          <el-button
            v-if="hasSuccessResults"
            type="primary"
            size="small"
            icon="el-icon-data-analysis"
            @click="showChartDialog = true"
          >查看图表分析</el-button>
        </div>
      </div>

      <!-- 操作栏 -->
      <div class="action-bar">
        <el-upload
          action=""
          :auto-upload="false"
          :show-file-list="false"
          :multiple="true"
          :on-change="handleChange"
        >
          <el-button type="primary" icon="el-icon-folder-opened">批量选择图片</el-button>
        </el-upload>
        <el-button type="success" icon="el-icon-video-play" @click="startBatchPredict" :disabled="!hasPendingImages" :loading="predicting">
          开始批量预测
        </el-button>
        <el-button type="danger" icon="el-icon-delete" @click="clearList" :disabled="imageList.length === 0">清空列表</el-button>
        <span v-if="hasSuccessResults" class="result-summary">
          共预测 <b>{{ successList.length }}</b> 张 ·
          平均温度 <b style="color: var(--accent-primary);">{{ avgTemp }}℃</b>
        </span>
      </div>

      <!-- 数据表 -->
      <el-table :data="imageList" style="width: 100%" stripe border>
        <el-table-column type="index" label="序号" width="70" align="center"></el-table-column>
        <el-table-column label="图片预览" width="100" align="center">
          <template slot-scope="scope">
            <el-image
              style="width: 56px; height: 56px; border-radius: 6px;"
              :src="scope.row.url"
              :preview-src-list="[scope.row.url]">
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="文件名" show-overflow-tooltip></el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status === '待预测'" type="info" size="small">待预测</el-tag>
            <el-tag v-else-if="scope.row.status === '预测中'" type="warning" size="small">预测中</el-tag>
            <el-tag v-else-if="scope.row.status === '成功'" type="success" size="small">成功</el-tag>
            <el-tag v-else type="danger" size="small">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预测温度 (℃)" width="140" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.temp !== null" class="temp-val">{{ scope.row.temp }}</span>
            <span v-else style="color: var(--text-muted)">-</span>
          </template>
        </el-table-column>
        <el-table-column label="真实温度 (℃，选填)" width="180" align="center">
          <template slot-scope="scope">
            <el-input-number
              v-if="scope.row.status === '成功'"
              v-model="scope.row.realTemp"
              :precision="2"
              :step="1"
              size="mini"
              style="width: 130px;"
              placeholder="选填"
              controls-position="right">
            </el-input-number>
            <span v-else style="color: var(--text-muted)">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template slot-scope="scope">
            <el-button type="text" style="color: #f87171;" @click="removeItem(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 图表分析弹窗 -->
    <el-dialog
      title="本次批量预测 · 图表分析"
      :visible.sync="showChartDialog"
      width="90vw"
      :close-on-click-modal="false"
      @opened="onDialogOpened"
      custom-class="chart-dialog">

      <!-- MAE 统计卡片 -->
      <div class="stats-row">
        <div class="stat-card">
          <div class="stat-label">预测成功</div>
          <div class="stat-value primary">{{ successList.length }} 张</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">平均预测温度</div>
          <div class="stat-value primary">{{ avgTemp }} ℃</div>
        </div>
        <div class="stat-card" v-if="maeValue !== null">
          <div class="stat-label">平均绝对误差 (MAE)</div>
          <div class="stat-value accent">{{ maeValue }} ℃</div>
        </div>
        <div class="stat-card" v-if="maeValue !== null">
          <div class="stat-label">最大绝对误差</div>
          <div class="stat-value warning">{{ maxAbsErr }} ℃</div>
        </div>
        <div class="stat-card" v-if="maeValue === null">
          <div class="stat-label">MAE 提示</div>
          <div class="stat-value muted">填入真实温度后可计算</div>
        </div>
      </div>

      <!-- 图表区 -->
      <el-tabs v-model="activeTab" class="chart-tabs" @tab-click="onTabClick">
        <!-- 温区分布 -->
        <el-tab-pane label="🌡 温区分布" name="zone">
          <div ref="zoneChart" class="chart-box"></div>
        </el-tab-pane>
        <!-- 预测 vs 真实 -->
        <el-tab-pane label="📈 预测 vs 真实温度" name="compare">
          <div v-if="hasRealTemp" ref="compareChart" class="chart-box"></div>
          <div v-else class="empty-hint">
            <i class="el-icon-data-line"></i>
            <p>在预测结果表格中填入<b>真实温度</b>后，此图表将自动生成对比折线图</p>
          </div>
        </el-tab-pane>
        <!-- 误差分布 -->
        <el-tab-pane label="📊 误差分布" name="error">
          <div v-if="hasRealTemp" ref="errorChart" class="chart-box"></div>
          <div v-else class="empty-hint">
            <i class="el-icon-s-data"></i>
            <p>在预测结果表格中填入<b>真实温度</b>后，此图表将显示每张图片的预测误差分布</p>
          </div>
        </el-tab-pane>
      </el-tabs>


      <span slot="footer">
        <el-button @click="showChartDialog = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios'
import * as echarts from 'echarts'

// 温区划分
const ZONES = [
  { label: '低温 (<200℃)',    min: -Infinity, max: 200 },
  { label: '中低温 (200-400℃)', min: 200, max: 400 },
  { label: '中温 (400-600℃)', min: 400, max: 600 },
  { label: '中高温 (600-800℃)', min: 600, max: 800 },
  { label: '高温 (>800℃)',    min: 800, max: Infinity },
]

export default {
  name: 'TempPredictBatch',
  data() {
    return {
      imageList: [],
      uploadUrl: 'http://localhost:8080/predictTemperature',
      predicting: false,
      showChartDialog: false,
      activeTab: 'zone',
      charts: {},
    }
  },
  computed: {
    hasPendingImages() {
      return this.imageList.some(img => img.status === '待预测' || img.status === '失败')
    },
    hasSuccessResults() {
      return this.imageList.some(img => img.status === '成功')
    },
    successList() {
      return this.imageList.filter(img => img.status === '成功' && img.temp !== null)
    },
    avgTemp() {
      if (!this.successList.length) return '-'
      const sum = this.successList.reduce((a, b) => a + b.temp, 0)
      return (sum / this.successList.length).toFixed(2)
    },
    hasRealTemp() {
      return this.successList.some(img => img.realTemp !== undefined && img.realTemp !== null)
    },
    maeValue() {
      const withReal = this.successList.filter(img => img.realTemp !== undefined && img.realTemp !== null)
      if (!withReal.length) return null
      const mae = withReal.reduce((sum, img) => sum + Math.abs(img.temp - img.realTemp), 0) / withReal.length
      return mae.toFixed(3)
    },
    maxAbsErr() {
      const withReal = this.successList.filter(img => img.realTemp !== undefined && img.realTemp !== null)
      if (!withReal.length) return null
      return Math.max(...withReal.map(img => Math.abs(img.temp - img.realTemp))).toFixed(3)
    },
  },
  methods: {
    // 从文件名提取真实温度，格式：G1_250_1.jpg → 250
    extractRealTemp(filename) {
      // 匹配 _数字_ 模式，取第一个匹配到的中间数字段
      const match = filename.match(/[_-](\d+)[_-]/)
      return match ? parseFloat(match[1]) : undefined
    },
    handleChange(file) {
      const isImage = file.raw.type === 'image/jpeg' || file.raw.type === 'image/png'
      if (!isImage) { this.$message.error(`${file.name} 不是 JPG/PNG 格式!`); return }
      const exists = this.imageList.some(img => img.name === file.name && img.size === file.size)
      if (!exists) {
        const realTemp = this.extractRealTemp(file.name)
        this.imageList.push({
          raw: file.raw, url: URL.createObjectURL(file.raw),
          name: file.name, size: file.size,
          status: '待预测', temp: null, realTemp,
        })
      }
    },
    removeItem(index) { this.imageList.splice(index, 1) },
    clearList() { this.imageList = [] },
    async startBatchPredict() {
      const token = localStorage.getItem('Authorization')
      this.predicting = true
      let successCount = 0
      for (let i = 0; i < this.imageList.length; i++) {
        let img = this.imageList[i]
        if (img.status === '成功' || img.status === '预测中') continue
        this.$set(this.imageList, i, { ...img, status: '预测中' })
        img = this.imageList[i]
        const formData = new FormData()
        formData.append('file', img.raw)
        try {
          const res = await axios.post(this.uploadUrl, formData, {
            headers: { 'Content-Type': 'multipart/form-data', 'Authorization': token || '' }
          })
          if (res.data && res.data.code === 1) {
            const realTemp = this.extractRealTemp(img.name)
            this.$set(this.imageList, i, { ...img, status: '成功', temp: res.data.data.temperature, realTemp })
            successCount++
          } else {
            this.$set(this.imageList, i, { ...img, status: '失败' })
          }
        } catch (e) {
          this.$set(this.imageList, i, { ...img, status: '失败' })
        }
      }
      this.predicting = false
      if (successCount > 0) {
        this.$message.success(`批量预测完成，成功 ${successCount} 张`)
      }
    },

    // ===== 图表渲染 =====
    onDialogOpened() {
      // 等待 dialog 动画完全结束后再初始化图表，确保拿到正确容器宽度
      setTimeout(() => {
        this.renderZoneChart()
        if (this.hasRealTemp) {
          this.renderCompareChart()
          this.renderErrorChart()
        }
      }, 120)
    },
    onTabClick(tab) {
      this.$nextTick(() => {
        setTimeout(() => {
          const key = tab.name + 'Chart'
          if (this.charts[key]) this.charts[key].resize()
        }, 60)
      })
    },

    getOrCreate(ref) {
      if (this.charts[ref]) { this.charts[ref].dispose() }
      this.charts[ref] = echarts.init(this.$refs[ref], 'dark')
      return this.charts[ref]
    },

    // 温区饼图
    renderZoneChart() {
      const chart = this.getOrCreate('zoneChart')
      const zoneData = ZONES.map(z => ({
        name: z.label,
        value: this.successList.filter(img => img.temp >= z.min && img.temp < z.max).length,
      })).filter(d => d.value > 0)

      chart.setOption({
        backgroundColor: 'transparent',
        tooltip: { trigger: 'item', formatter: '{b}: {c} 张 ({d}%)' },
        legend: { bottom: 10, textStyle: { color: '#ccc' } },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['50%', '45%'],
          data: zoneData,
          label: { color: '#ddd', formatter: '{b}\n{c}张' },
          itemStyle: {
            borderRadius: 6,
            borderColor: 'rgba(0,0,0,0.3)',
            borderWidth: 2,
          },
          emphasis: { itemStyle: { shadowBlur: 16, shadowOffsetX: 0, shadowColor: 'rgba(0,212,255,0.5)' } },
        }],
      })
    },

    // 预测 vs 真实折线图
    renderCompareChart() {
      const chart = this.getOrCreate('compareChart')
      const withReal = this.successList.filter(img => img.realTemp !== undefined && img.realTemp !== null)
      const names = withReal.map((img, i) => `图${i + 1}`)
      const count = withReal.length
      // 最多显 12 个标签，多了自动隱藏
      const labelInterval = count > 12 ? Math.ceil(count / 12) - 1 : 0
      chart.setOption({
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          formatter: params => {
            const idx = params[0].dataIndex
            return `图${idx + 1}：${withReal[idx].name}<br/>` +
              params.map(p => `${p.seriesName}: <b>${p.value} ℃</b>`).join('<br/>')
          }
        },
        legend: { data: ['预测温度', '真实温度'], top: 8, textStyle: { color: '#ccc' } },
        grid: { left: 60, right: 30, bottom: count > 30 ? 40 : 70, top: 50, containLabel: false },
        xAxis: {
          type: 'category', data: names,
          axisLabel: {
            interval: labelInterval,
            rotate: count > 30 ? 0 : 35,
            color: '#aaa', fontSize: 11,
            formatter: count > 30 ? val => val : val => val,
          },
          axisLine: { lineStyle: { color: '#444' } },
        },
        yAxis: { type: 'value', name: '温度 (℃)', nameTextStyle: { color: '#aaa' }, axisLabel: { color: '#aaa' }, axisLine: { lineStyle: { color: '#444' } }, splitLine: { lineStyle: { color: '#333' } } },
        series: [
          {
            name: '预测温度', type: 'line', smooth: true,
            data: withReal.map(img => img.temp),
            lineStyle: { color: '#00d4ff' }, itemStyle: { color: '#00d4ff' },
            areaStyle: { color: 'rgba(0,212,255,0.08)' },
            symbol: count > 50 ? 'none' : 'circle', symbolSize: 5,
          },
          {
            name: '真实温度', type: 'line', smooth: true,
            data: withReal.map(img => img.realTemp),
            lineStyle: { color: '#a855f7', type: 'dashed' }, itemStyle: { color: '#a855f7' },
            symbol: count > 50 ? 'none' : 'diamond', symbolSize: 5,
          },
        ],
      })
    },


    // 误差柱状图
    renderErrorChart() {
      const chart = this.getOrCreate('errorChart')
      const withReal = this.successList.filter(img => img.realTemp !== undefined && img.realTemp !== null)
      const names = withReal.map((img, i) => `图${i + 1}`)
      const errors = withReal.map(img => parseFloat((img.temp - img.realTemp).toFixed(3)))
      const count = withReal.length
      const labelInterval = count > 12 ? Math.ceil(count / 12) - 1 : 0
      chart.setOption({
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          formatter: params => {
            const idx = params[0].dataIndex
            return `图${idx + 1}：${withReal[idx].name}<br/>误差: <b>${params[0].value} ℃</b>`
          }
        },
        grid: { left: 60, right: 30, bottom: count > 30 ? 40 : 70, top: 30, containLabel: false },
        xAxis: {
          type: 'category', data: names,
          axisLabel: { interval: labelInterval, rotate: count > 30 ? 0 : 35, color: '#aaa', fontSize: 11 },
          axisLine: { lineStyle: { color: '#444' } },
        },
        yAxis: {
          type: 'value', name: '误差 (℃)', nameTextStyle: { color: '#aaa' }, axisLabel: { color: '#aaa' },
          axisLine: { lineStyle: { color: '#444' } }, splitLine: { lineStyle: { color: '#333' } }
        },
        series: [{
          type: 'bar',
          data: errors.map(v => ({ value: v, itemStyle: { color: v >= 0 ? 'rgba(0,212,255,0.7)' : 'rgba(248,113,113,0.7)' } })),
          barMaxWidth: count > 60 ? 4 : 20,
          markLine: {
            silent: true,
            lineStyle: { color: '#f59e0b', type: 'dashed' },
            data: [{ yAxis: 0, name: '零误差' }],
            label: { color: '#f59e0b' },
          },
        }],
      })
    },

  },
  beforeDestroy() {
    Object.values(this.charts).forEach(c => c && c.dispose())
  },
}
</script>

<style lang="less" scoped>
.batch-page {
  padding: 20px;
}

.main-card {
  background: var(--bg-elevated) !important;
  border: 1px solid var(--border-subtle) !important;
  border-radius: var(--radius-md) !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .card-title {
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);
    i { margin-right: 6px; color: var(--accent-primary); }
  }
}

.action-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.result-summary {
  margin-left: auto;
  font-size: 13px;
  color: var(--text-secondary);
}

.temp-val {
  color: #34d399;
  font-weight: 700;
  font-size: 15px;
}

// ===== 弹窗 =====
/deep/ .chart-dialog {
  background: var(--bg-surface) !important;
  border: 1px solid var(--border-subtle) !important;
  border-radius: var(--radius-lg) !important;

  .el-dialog__header {
    border-bottom: 1px solid var(--border-subtle);
    .el-dialog__title {
      background: var(--accent-gradient);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      font-weight: 700;
    }
  }

  .el-dialog__body {
    padding: 16px 24px;
  }
}

// 统计卡片行
.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}
.stat-card {
  background: linear-gradient(135deg, rgba(0,212,255,0.04), rgba(124,58,237,0.04));
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  padding: 16px 20px;
  text-align: center;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 2px;
    background: linear-gradient(90deg, transparent, var(--accent-primary), transparent);
    opacity: 0.5;
  }

  .stat-label {
    font-size: 11px;
    color: var(--text-muted);
    margin-bottom: 10px;
    letter-spacing: 0.5px;
    text-transform: uppercase;
  }
  .stat-value {
    font-size: 22px;
    font-weight: 700;
    line-height: 1;
    &.primary { color: var(--accent-primary); text-shadow: 0 0 12px rgba(0,212,255,0.3); }
    &.accent   { color: #a855f7; text-shadow: 0 0 12px rgba(168,85,247,0.3); }
    &.warning  { color: #f59e0b; text-shadow: 0 0 12px rgba(245,158,11,0.3); }
    &.muted    { font-size: 13px; color: var(--text-muted); font-weight: 400; }
  }
}

// 图表
.chart-tabs {
  /deep/ .el-tabs__item { color: var(--text-secondary); &.is-active { color: var(--accent-primary); } }
  /deep/ .el-tabs__active-bar { background-color: var(--accent-primary); }
  /deep/ .el-tabs__nav-wrap::after { background-color: var(--border-subtle); }
}

.chart-box {
  width: 100%;
  height: 380px;
  margin-top: 4px;
  border-radius: var(--radius-md);
  overflow: hidden;
  background: rgba(0,0,0,0.2);
  border: 1px solid var(--border-subtle);
}

.empty-hint {
  height: 380px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 16px;
  color: var(--text-muted);
  border: 1px dashed var(--border-subtle);
  border-radius: var(--radius-md);
  margin-top: 4px;
  background: rgba(0,0,0,0.1);

  i {
    font-size: 48px;
    opacity: 0.4;
    color: var(--accent-primary);
  }

  p {
    font-size: 14px;
    text-align: center;
    max-width: 360px;
    line-height: 1.8;
    b { color: var(--accent-primary); }
  }
}
</style>

