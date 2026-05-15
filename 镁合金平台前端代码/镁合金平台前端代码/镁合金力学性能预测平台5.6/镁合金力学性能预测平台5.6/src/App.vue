<template>
  <div id="app">
    <router-view />
  </div>
</template>

<style lang="less">
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

// ========== 设计 Token ==========
:root {
  --bg-base: #0d1117;
  --bg-surface: #161b22;
  --bg-elevated: #1c2128;
  --bg-overlay: #21262d;
  --border-subtle: rgba(0, 212, 255, 0.12);
  --border-default: rgba(0, 212, 255, 0.25);
  --accent-primary: #00d4ff;
  --accent-secondary: #7c3aed;
  --accent-gradient: linear-gradient(135deg, #00d4ff 0%, #7c3aed 100%);
  --text-primary: #e6edf3;
  --text-secondary: #8b949e;
  --text-muted: #484f58;
  --glow-primary: 0 0 20px rgba(0, 212, 255, 0.3);
  --shadow-card: 0 4px 30px rgba(0, 0, 0, 0.5);
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  background-color: var(--bg-base);
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

#app {
  height: 100vh;
  color: var(--text-primary);
  background-color: var(--bg-base);
}

// ========== Element UI 全局覆盖 ==========

// 表格
.el-table {
  background-color: var(--bg-elevated) !important;
  color: var(--text-primary) !important;

  &::before {
    background-color: var(--border-subtle) !important;
  }

  th.el-table__cell {
    background-color: var(--bg-overlay) !important;
    color: var(--accent-primary) !important;
    border-bottom: 1px solid var(--border-subtle) !important;
    font-weight: 600;
    font-size: 13px;
  }

  td.el-table__cell {
    background-color: var(--bg-elevated) !important;
    border-bottom: 1px solid rgba(255, 255, 255, 0.04) !important;
    color: var(--text-primary) !important;
  }

  tr {
    background-color: var(--bg-elevated) !important;
    &:hover > td {
      background-color: rgba(0, 212, 255, 0.07) !important;
    }
  }

  // stripe 斑马纹深色
  &.el-table--striped {
    .el-table__body tr.el-table__row--striped td.el-table__cell {
      background-color: rgba(0, 0, 0, 0.15) !important;
    }
  }

  .el-table__empty-block {
    background-color: var(--bg-elevated) !important;
    color: var(--text-secondary) !important;
  }

  // 展开行
  .el-table__expand-column, .el-table__expanded-cell {
    background-color: var(--bg-elevated) !important;
    color: var(--text-primary) !important;
  }
}

// 分页
.el-pagination {
  color: var(--text-secondary) !important;

  button, .el-pager li {
    background-color: var(--bg-overlay) !important;
    color: var(--text-secondary) !important;
    border: 1px solid var(--border-subtle) !important;
    border-radius: var(--radius-sm) !important;
    &:hover, &.active {
      color: var(--accent-primary) !important;
      border-color: var(--accent-primary) !important;
      background-color: rgba(0, 212, 255, 0.1) !important;
    }
  }
}

// 按钮
.el-button--primary {
  background: var(--accent-gradient) !important;
  border: none !important;
  border-radius: var(--radius-sm) !important;
  font-weight: 500 !important;
  letter-spacing: 0.5px !important;
  transition: all 0.3s ease !important;
  box-shadow: 0 0 15px rgba(0, 212, 255, 0.3) !important;
  &:hover {
    transform: translateY(-1px) !important;
    box-shadow: 0 0 25px rgba(0, 212, 255, 0.5) !important;
  }
}

.el-button--default {
  background: transparent !important;
  border: 1px solid var(--border-default) !important;
  color: var(--text-primary) !important;
  border-radius: var(--radius-sm) !important;
  transition: all 0.3s ease !important;
  &:hover {
    background: rgba(0, 212, 255, 0.1) !important;
    border-color: var(--accent-primary) !important;
    color: var(--accent-primary) !important;
  }
}

.el-button--danger {
  background: rgba(239, 68, 68, 0.15) !important;
  border: 1px solid rgba(239, 68, 68, 0.4) !important;
  color: #f87171 !important;
  border-radius: var(--radius-sm) !important;
  &:hover {
    background: rgba(239, 68, 68, 0.3) !important;
  }
}

.el-button--info {
  background: rgba(139, 148, 158, 0.15) !important;
  border: 1px solid rgba(139, 148, 158, 0.3) !important;
  color: var(--text-secondary) !important;
  border-radius: var(--radius-sm) !important;
  &:hover {
    background: rgba(139, 148, 158, 0.3) !important;
    color: var(--text-primary) !important;
  }
}

// 输入框
.el-input__inner {
  background-color: var(--bg-overlay) !important;
  border: 1px solid var(--border-subtle) !important;
  color: var(--text-primary) !important;
  border-radius: var(--radius-sm) !important;
  transition: all 0.3s ease !important;

  &::placeholder {
    color: var(--text-muted) !important;
  }

  &:focus {
    border-color: var(--accent-primary) !important;
    box-shadow: 0 0 0 2px rgba(0, 212, 255, 0.15) !important;
  }
}

// 卡片
.el-card {
  background-color: var(--bg-elevated) !important;
  border: 1px solid var(--border-subtle) !important;
  border-radius: var(--radius-md) !important;
  box-shadow: var(--shadow-card) !important;
  color: var(--text-primary) !important;

  .el-card__header {
    border-bottom: 1px solid var(--border-subtle) !important;
    color: var(--text-primary) !important;
    font-weight: 600;
  }
}

// 表单标签
.el-form-item__label {
  color: var(--text-secondary) !important;
  font-weight: 500;
}

// Dialog 弹窗
.el-dialog {
  background-color: var(--bg-surface) !important;
  border: 1px solid var(--border-subtle) !important;
  border-radius: var(--radius-lg) !important;
  box-shadow: var(--shadow-card), var(--glow-primary) !important;

  .el-dialog__title {
    color: var(--text-primary) !important;
    font-weight: 600;
  }

  .el-dialog__header {
    border-bottom: 1px solid var(--border-subtle);
  }

  .el-dialog__body {
    color: var(--text-primary) !important;
  }
}

// Select 下拉
.el-select-dropdown {
  background-color: var(--bg-overlay) !important;
  border: 1px solid var(--border-subtle) !important;

  .el-select-dropdown__item {
    color: var(--text-primary) !important;
    &:hover, &.hover {
      background-color: rgba(0, 212, 255, 0.1) !important;
    }
    &.selected {
      color: var(--accent-primary) !important;
    }
  }
}

// 遮罩层
.el-overlay, .v-modal {
  background: rgba(0, 0, 0, 0.7) !important;
}

// 消息提示
.el-message {
  background-color: var(--bg-elevated) !important;
  border-color: var(--border-subtle) !important;
  .el-message__content {
    color: var(--text-primary) !important;
  }
}

// ========== 兜底全局覆盖（对抗各页面 scoped 白色硬编码）==========

// 通用容器类：白色/浅色背景全部替换
.container,
.el-main .container,
.el-main > div {
  background-color: var(--bg-elevated) !important;
  color: var(--text-primary) !important;
  border-color: var(--border-subtle) !important;
}

// chart-container 图表卡片
.chart-container {
  background-color: var(--bg-surface) !important;
  border: 1px solid var(--border-subtle) !important;
  box-shadow: var(--shadow-card) !important;
  border-radius: var(--radius-md) !important;

  h2 {
    color: var(--text-primary) !important;
    border-bottom-color: var(--border-subtle) !important;
  }
}

// aside 侧边面板（Echarts 页面内部的）
.visualization-container .aside,
.el-container > .el-aside.aside {
  background-color: var(--bg-surface) !important;
  border-right: 1px solid var(--border-subtle) !important;
  box-shadow: none !important;

  h2 {
    color: var(--text-primary) !important;
    border-bottom-color: var(--border-subtle) !important;
  }
}

// 页面内的 h1/h2/h3 标题
.el-main h1,
.el-main h2,
.el-main h3 {
  color: var(--text-primary) !important;
}

// el-empty 暗色
.el-empty {
  background-color: var(--bg-elevated) !important;
  .el-empty__description p {
    color: var(--text-muted) !important;
  }
}

// el-upload 上传区域
.el-upload-dragger {
  background-color: var(--bg-overlay) !important;
  border-color: var(--border-default) !important;
  color: var(--text-secondary) !important;
}
.el-upload-list__item {
  color: var(--text-primary) !important;
}

// warning/success 按钮统一走科技色系
.el-button--warning {
  background: rgba(234, 179, 8, 0.15) !important;
  border: 1px solid rgba(234, 179, 8, 0.4) !important;
  color: #fbbf24 !important;
  border-radius: var(--radius-sm) !important;
  &:hover {
    background: rgba(234, 179, 8, 0.3) !important;
  }
}

.el-button--success {
  background: rgba(34, 197, 94, 0.15) !important;
  border: 1px solid rgba(34, 197, 94, 0.4) !important;
  color: #4ade80 !important;
  border-radius: var(--radius-sm) !important;
  &:hover {
    background: rgba(34, 197, 94, 0.3) !important;
  }
}

// 表单行内布局
.demo-form-inline {
  .el-form-item__label {
    color: var(--text-secondary) !important;
  }
}

// data-table 白色覆盖
.data-table {
  background-color: var(--bg-elevated) !important;
  border-color: var(--border-subtle) !important;
}

// el-form 背景
.el-form {
  color: var(--text-primary) !important;
}

// 通用 border 颜色修正
.el-table--border,
.el-table--group {
  border-color: var(--border-subtle) !important;
  &::after, &::before {
    background-color: var(--border-subtle) !important;
  }
}

// 对话框内的表单
.el-dialog .el-form-item__label {
  color: var(--text-secondary) !important;
}

// ECharts 图表容器透明背景（让 chart-container 的深色透出来）
.chart {
  background: transparent !important;
}

// Loading 遮罩
.el-loading-mask {
  background-color: rgba(13, 17, 23, 0.85) !important;
  .el-loading-text {
    color: var(--accent-primary) !important;
  }
  .el-loading-spinner .path {
    stroke: var(--accent-primary) !important;
  }
}

// tag 标签
.el-tag {
  background-color: rgba(0, 212, 255, 0.1) !important;
  border-color: var(--border-default) !important;
  color: var(--accent-primary) !important;
}

// input number
.el-input-number .el-input-number__decrease,
.el-input-number .el-input-number__increase {
  background: var(--bg-overlay) !important;
  border-color: var(--border-subtle) !important;
  color: var(--text-secondary) !important;
  &:hover {
    color: var(--accent-primary) !important;
  }
}
</style>
