<template>
  <div class="nav-menu-wrapper">
    <el-menu
      mode="horizontal"
      :default-active="activeIndex"
      class="top-nav-menu"
      background-color="transparent"
      text-color="var(--text-secondary)"
      active-text-color="var(--accent-primary)"
    >
      <!-- 1. 性能预测 -->
      <el-submenu index="1">
        <template #title>
          <i class="el-icon-s-data"></i>
          <span>性能预测</span>
        </template>
        <el-menu-item index="1-1" @click="goData1">
          <i class="el-icon-download"></i>文件导入
        </el-menu-item>
        <el-menu-item index="1-2" @click="goInput">
          <i class="el-icon-edit"></i>参数输入
        </el-menu-item>
        <el-menu-item index="1-3" @click="goEcharts">
          <i class="el-icon-pie-chart"></i>图表展示
        </el-menu-item>
      </el-submenu>

      <!-- 2. 裂纹识别 -->
      <el-submenu index="2">
        <template #title>
          <i class="el-icon-camera"></i>
          <span>裂纹识别</span>
        </template>
        <el-menu-item index="2-1" @click="goData3">
          <i class="el-icon-search"></i>图像识别
        </el-menu-item>
        <el-menu-item index="2-2" @click="goInputFrame">
          <i class="el-icon-edit"></i>参数输入
        </el-menu-item>
        <el-menu-item index="2-3" @click="goImages">
          <i class="el-icon-picture-outline"></i>图表展示
        </el-menu-item>
      </el-submenu>

      <!-- 3. 温度预测 -->
      <el-submenu index="3">
        <template #title>
          <i class="el-icon-picture"></i>
          <span>温度预测</span>
        </template>
        <el-menu-item index="3-1" @click="goTempPredictSingle">
          <i class="el-icon-crop"></i>单图/局部框选
        </el-menu-item>
        <el-menu-item index="3-2" @click="goTempPredictBatch">
          <i class="el-icon-document-copy"></i>批量预测
        </el-menu-item>
      </el-submenu>

      <!-- 4. 历史中心 (独立顶级入口) -->
      <el-menu-item index="5" @click="goHistory">
        <i class="el-icon-time"></i>
        <span>历史中心</span>
      </el-menu-item>

      <!-- 5. 个人信息 / 管理中心 -->
      <el-menu-item index="4" @click="goProfile">
        <i class="el-icon-user"></i>
        <span>{{ isAdmin ? '管理中心' : '个人信息' }}</span>
        <el-badge v-if="isAdmin" value="ADMIN" class="admin-badge" type="danger"></el-badge>
      </el-menu-item>
    </el-menu>
  </div>
</template>

<script>
export default {
  data() {
    return {
      activeIndex: '1',
      isAdmin: window.localStorage.getItem('role') === '1'
    };
  },
  methods: {
    goData1()             { this.$router.push('data1') },
    goData3()             { this.$router.push('data3') },
    goInput()             { this.$router.push('input') },
    goEcharts()           { this.$router.push('echarts') },
    goProfile()           { this.$router.push('profile') },
    goInputFrame()        { this.$router.push('inputFrame') },
    goImages()            { this.$router.push('data3images') },
    goTempPredictSingle() { this.$router.push('tempPredictSingle') },
    goTempPredictBatch()  { this.$router.push('tempPredictBatch') },
    goHistory()           { this.$router.push('/history') },
  }
}
</script>

<style lang="less" scoped>
.nav-menu-wrapper {
  width: 100%;
  background: transparent;
}

.top-nav-menu {
  border-bottom: none !important;
  background: transparent !important;
  height: 48px;
  display: flex;
  align-items: center;
  padding: 0 16px;

  /deep/ .el-menu-item,
  /deep/ .el-submenu__title {
    height: 48px !important;
    line-height: 48px !important;
    font-size: 13px !important;
    font-weight: 500 !important;
    color: var(--text-secondary) !important;
    border-bottom: 2px solid transparent !important;
    padding: 0 16px !important;
    transition: all 0.25s ease !important;
    background: transparent !important;

    i {
      font-size: 14px !important;
      margin-right: 6px !important;
      color: inherit !important;
    }

    &:hover {
      color: var(--accent-primary) !important;
      background: rgba(0, 212, 255, 0.06) !important;
      border-bottom-color: var(--accent-primary) !important;
    }
  }

  /deep/ .el-menu-item.is-active,
  /deep/ .el-submenu.is-active > .el-submenu__title {
    color: var(--accent-primary) !important;
    border-bottom-color: var(--accent-primary) !important;
    background: rgba(0, 212, 255, 0.08) !important;
  }

  /deep/ .el-menu--popup {
    background: rgba(16, 20, 28, 0.97) !important;
    border: 1px solid var(--border-subtle) !important;
    border-radius: var(--radius-md) !important;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5) !important;
    padding: 6px 0 !important;
    min-width: 160px !important;
    backdrop-filter: blur(12px);
  }

  /deep/ .el-menu--popup .el-menu-item {
    height: 40px !important;
    line-height: 40px !important;
    font-size: 13px !important;
    color: var(--text-secondary) !important;
    padding: 0 20px !important;
    border-bottom: none !important;
    background: transparent !important;

    &:hover {
      color: var(--accent-primary) !important;
      background: rgba(0, 212, 255, 0.08) !important;
    }

    &.is-active {
      color: var(--accent-primary) !important;
      background: rgba(0, 212, 255, 0.1) !important;
    }
  }

  /deep/ .el-menu--popup .el-submenu__title {
    height: 40px !important;
    line-height: 40px !important;
    font-size: 13px !important;
    color: var(--text-secondary) !important;
    padding: 0 20px !important;
    border-bottom: none !important;
    background: transparent !important;

    &:hover {
      color: var(--accent-primary) !important;
      background: rgba(0, 212, 255, 0.08) !important;
    }
  }

  /deep/ .el-submenu__icon-arrow {
    color: var(--text-muted) !important;
    font-size: 11px !important;
  }
}

.admin-badge {
  margin-left: 8px;
  /deep/ .el-badge__content {
    line-height: 14px;
    height: 14px;
    padding: 0 4px;
    border: none;
    font-size: 10px;
    font-weight: 700;
  }
}
</style>
