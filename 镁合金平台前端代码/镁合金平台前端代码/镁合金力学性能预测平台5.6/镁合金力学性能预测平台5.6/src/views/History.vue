<template>
  <div class="history-hub">
    <!-- 顶部标题栏 -->
    <div class="hub-header">
      <div class="hub-title">
        <i class="el-icon-time"></i>
        <span>历史中心</span>
        <span class="hub-subtitle">HISTORY HUB</span>
      </div>
      <div class="hub-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          size="small"
          style="margin-right:10px;"
          @change="onDateChange"
        />
        <el-button size="small" type="primary" icon="el-icon-download" @click="exportExcel">导出 Excel</el-button>
      </div>
    </div>

    <!-- Tab 切换 -->
    <div class="tab-bar">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="switchTab(tab.key)"
      >
        <i :class="tab.icon"></i>
        <span>{{ tab.label }}</span>
        <span class="tab-count">{{ tabCounts[tab.key] }}</span>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="hub-content" v-loading="loading">
      <!-- 管理员 UID 过滤提示 -->
      <div class="admin-badge-bar" v-if="isAdmin">
        <el-tag type="danger" effect="dark" size="mini">管理员模式 · 显示所有用户数据</el-tag>
      </div>

      <!-- 空状态 -->
      <div class="empty-block" v-if="!loading && currentRows.length === 0">
        <el-empty description="暂无历史记录" />
      </div>

      <!-- ===== 性能预测 Tab ===== -->
      <template v-if="activeTab === 'performance' && currentRows.length > 0">
        <div style="margin-bottom:12px;">
          <el-button size="mini" @click="showPerfExtra = !showPerfExtra">
            {{ showPerfExtra ? '隐藏参数详情' : '显示参数详情（BP专属）' }}
          </el-button>
        </div>
        <el-table
          ref="perfTable"
          :data="currentRows"
          @selection-change="onSelectionChange"
          class="hub-table"
          :header-cell-style="tableHeaderStyle"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column type="index" label="#" width="55" align="center" />
          <el-table-column prop="uid" label="用户UID" width="110" v-if="isAdmin" show-overflow-tooltip />
          <el-table-column label="模型" width="90" align="center">
            <template slot-scope="scope">
              <el-tag size="mini" :type="scope.row._type === 'BP' ? 'primary' : scope.row._type === 'PSO' ? 'success' : 'warning'">
                {{ scope.row._type }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="aveLength" label="平均晶粒尺寸" align="center" min-width="120" />
          <el-table-column prop="lisanValue" label="离散特征值" align="center" min-width="110" />
          <el-table-column prop="hardness" label="硬度" align="center" />
          <el-table-column prop="yieldStrength" label="屈服强度" align="center" min-width="100" />
          <el-table-column prop="strengthExtension" label="抗拉强度" align="center" min-width="100" />
          <!-- BP专属参数列 -->
          <el-table-column v-if="showPerfExtra" prop="inputLayer" label="输入层" align="center" min-width="80" />
          <el-table-column v-if="showPerfExtra" prop="outputLayer" label="输出层" align="center" min-width="80" />
          <el-table-column v-if="showPerfExtra" prop="intermediateLayer" label="中间层" align="center" min-width="80" />
          <el-table-column v-if="showPerfExtra" prop="numberOfCycles" label="循环次数" align="center" min-width="90" />
          <el-table-column v-if="showPerfExtra" prop="learningRate" label="学习率" align="center" min-width="80" />
          <el-table-column v-if="showPerfExtra" prop="errorTargetValue" label="误差目标" align="center" min-width="90" />
          <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
          <el-table-column label="操作" width="80" align="center">
            <template slot-scope="scope">
              <el-button type="text" style="color:#F56C6C" @click="deleteSingle('performance', scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <!-- ===== 裂纹识别 Tab ===== -->
      <template v-if="activeTab === 'crack' && currentRows.length > 0">
        <el-table
          ref="crackTable"
          :data="currentRows"
          @selection-change="onSelectionChange"
          class="hub-table"
          :header-cell-style="tableHeaderStyle"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column type="index" label="#" width="55" align="center" />
          <el-table-column prop="uid" label="用户UID" width="110" v-if="isAdmin" show-overflow-tooltip />
          <el-table-column label="图片预览" width="90" align="center">
            <template slot-scope="scope">
              <el-image
                :src="scope.row.ImageUrl"
                style="width:60px;height:40px;border-radius:4px;cursor:pointer;"
                :preview-src-list="[scope.row.ImageUrl]"
                fit="cover"
              />
            </template>
          </el-table-column>
          <el-table-column prop="ImageUrl" label="图片路径" show-overflow-tooltip min-width="200" />
          <el-table-column label="裂纹数" width="80" align="center">
            <template slot-scope="scope">
              <el-tag size="mini" :type="scope.row._crackCount > 0 ? 'danger' : 'info'">
                {{ scope.row._crackCount !== undefined ? scope.row._crackCount : '-' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="识别时间" width="170" align="center" />
          <el-table-column label="操作" width="80" align="center">
            <template slot-scope="scope">
              <el-button type="text" style="color:#F56C6C" @click="deleteSingle('crack', scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <!-- ===== 温度预测 Tab ===== -->
      <template v-if="activeTab === 'temperature' && currentRows.length > 0">
        <el-table
          ref="tempTable"
          :data="currentRows"
          @selection-change="onSelectionChange"
          class="hub-table"
          :header-cell-style="tableHeaderStyle"
        >
          <el-table-column type="selection" width="50" />
          <el-table-column type="index" label="#" width="55" align="center" />
          <el-table-column prop="uid" label="用户UID" width="120" v-if="isAdmin" show-overflow-tooltip />
          <el-table-column prop="fileName" label="文件名" align="center" />
          <el-table-column prop="temp" label="预测温度 (℃)" width="150" align="center">
            <template slot-scope="scope">
              <span style="color:#67C23A;font-weight:bold;">{{ scope.row.temp }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="time" label="预测时间" width="180" align="center" />
          <el-table-column label="操作" width="80" align="center">
            <template slot-scope="scope">
              <el-button type="text" style="color:#F56C6C" @click="deleteSingle('temperature', scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

      <!-- 分页 -->
      <el-pagination
        v-if="totalRows > 0"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalRows"
        class="hub-pagination"
      />
    </div>

    <!-- 底部操作栏 -->
    <div class="hub-footer" v-if="selectedRows.length > 0">
      <span class="selected-tip">已选择 <b>{{ selectedRows.length }}</b> 条记录</span>
      <el-button type="danger" size="small" icon="el-icon-delete" @click="batchDelete">批量删除</el-button>
      <el-button size="small" @click="clearSelection">取消选择</el-button>
    </div>

    <!-- 密码确认删除对话框 -->
    <el-dialog
      title="操作确认"
      :visible.sync="pwdDialogVisible"
      width="360px"
      :close-on-click-modal="false"
    >
      <div class="pwd-dialog-body">
        <i class="el-icon-warning" style="color:#E6A23C;font-size:28px;margin-bottom:10px;"></i>
        <p style="color:var(--text-secondary);margin-bottom:16px;">首次执行删除操作，请输入您的登录密码以确认</p>
        <el-input
          v-model="confirmPassword"
          type="password"
          show-password
          placeholder="请输入登录密码"
          @keyup.enter.native="submitDelete"
        />
      </div>
      <span slot="footer">
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitDelete" :loading="deleting">确认删除</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios';
import ExportJsonExcel from 'js-export-excel';

const BASE = 'http://localhost:8080';

export default {
  name: 'HistoryHub',
  data() {
    return {
      activeTab: 'performance',
      showPerfExtra: false,
      tabs: [
        { key: 'performance', label: '性能预测', icon: 'el-icon-s-data' },
        { key: 'crack',       label: '裂纹识别', icon: 'el-icon-camera' },
        { key: 'temperature', label: '温度预测', icon: 'el-icon-picture' },
      ],
      // 各模块原始数据
      perfData: [],   // 合并 BP+PSO+PSOBP
      crackData: [],
      tempData: [],
      // 日期筛选后的数据
      perfFiltered: [],
      crackFiltered: [],
      tempFiltered: [],

      dateRange: null,
      loading: false,
      currentPage: 1,
      pageSize: 10,
      selectedRows: [],

      isAdmin: localStorage.getItem('role') === '1',

      // 删除相关
      pwdDialogVisible: false,
      confirmPassword: '',
      deleting: false,
      deleteVerified: false, // 本次会话是否已经验证过密码
      pendingDeleteTask: null, // { type, ids }

      tableHeaderStyle: {
        background: '#21262d',
        color: '#00d4ff',
        borderBottom: '1px solid rgba(0,212,255,0.15)'
      }
    };
  },
  computed: {
    currentData() {
      const map = { performance: this.perfFiltered, crack: this.crackFiltered, temperature: this.tempFiltered };
      return map[this.activeTab] || [];
    },
    currentRows() {
      const start = (this.currentPage - 1) * this.pageSize;
      return this.currentData.slice(start, start + this.pageSize);
    },
    totalRows() {
      return this.currentData.length;
    },
    tabCounts() {
      return {
        performance: this.perfFiltered.length,
        crack: this.crackFiltered.length,
        temperature: this.tempFiltered.length,
      };
    }
  },
  async mounted() {
    await this.loadAll();
  },
  methods: {
    getHeaders() {
      const token = localStorage.getItem('Authorization');
      return { Authorization: token ? `Bearer ${token}` : '' };
    },

    // =========== 数据加载 ===========
    async loadAll() {
      this.loading = true;
      await Promise.all([this.loadPerf(), this.loadCrack(), this.loadTemp()]);
      this.applyDateFilter();
      this.loading = false;
    },

    async loadPerf() {
      try {
        const headers = this.getHeaders();
        const payload  = { page: 1, pageSize: 10000 };
        const [bpRes, psoRes, psobpRes] = await Promise.allSettled([
          axios.post(`${BASE}/bphistory`,    payload, { headers }),
          axios.post(`${BASE}/psohistory`,   payload, { headers }),
          axios.post(`${BASE}/psobphistory`, payload, { headers }),
        ]);
        const tag = (res, type) =>
          res.status === 'fulfilled' && res.value.data.code === 1
            ? (res.value.data.data.records || []).map(r => ({ ...r, _type: type }))
            : [];
        this.perfData = [
          ...tag(bpRes,    'BP'),
          ...tag(psoRes,   'PSO'),
          ...tag(psobpRes, 'PSO-BP'),
        ].sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
      } catch (e) { console.error('性能预测历史加载失败', e); }
    },

    async loadCrack() {
      try {
        const res = await axios.post(`${BASE}/imageQuery`, { page: 1, pageSize: 10000 }, { headers: this.getHeaders() });
        if (res.data.code === 1) {
          const images = res.data.data.rows || [];
          // 批量查询每张图对应的裂纹数
          const crackCounts = await Promise.allSettled(
            images.map(img =>
              axios.post(`${BASE}/crackQuery`, { ImageUrl: img.ImageUrl }, { headers: this.getHeaders() })
            )
          );
          this.crackData = images.map((img, i) => ({
            ...img,
            _crackCount: crackCounts[i].status === 'fulfilled' && crackCounts[i].value.data.code === 1
              ? (crackCounts[i].value.data.data.rows || []).length
              : 0
          }));
        }
      } catch (e) { console.error('裂纹识别历史加载失败', e); }
    },

    async loadTemp() {
      try {
        const res = await axios.get(`${BASE}/tempHistory/list`, { headers: this.getHeaders() });
        if (res.data && res.data.code === 1) {
          this.tempData = res.data.data.map(item => ({
            ...item,
            time: item.time ? item.time.replace('T', ' ').substring(0, 19) : ''
          }));
        }
      } catch (e) { console.error('温度预测历史加载失败', e); }
    },

    // =========== 日期筛选 ===========
    applyDateFilter() {
      const filter = (list, timeKey) => {
        if (!this.dateRange || this.dateRange.length !== 2) return [...list];
        const start = new Date(this.dateRange[0] + ' 00:00:00').getTime();
        const end   = new Date(this.dateRange[1] + ' 23:59:59').getTime();
        return list.filter(item => {
          const t = new Date((item[timeKey] || '').replace('T', ' ')).getTime();
          return t >= start && t <= end;
        });
      };
      this.perfFiltered  = filter(this.perfData, 'createTime');
      this.crackFiltered = filter(this.crackData, 'createTime');
      this.tempFiltered  = filter(this.tempData, 'time');
      this.currentPage = 1;
    },

    onDateChange() {
      this.applyDateFilter();
    },

    // =========== Tab 切换 ===========
    switchTab(key) {
      this.activeTab = key;
      this.currentPage = 1;
      this.selectedRows = [];
    },

    // =========== 选择 ===========
    onSelectionChange(rows) {
      this.selectedRows = rows;
    },

    clearSelection() {
      const refMap = { performance: 'perfTable', crack: 'crackTable', temperature: 'tempTable' };
      const ref = this.$refs[refMap[this.activeTab]];
      if (ref) ref.clearSelection();
      this.selectedRows = [];
    },

    // =========== 删除逻辑 ===========
    deleteSingle(type, row) {
      const ids = type === 'performance' ? [row.id]
                : type === 'crack'       ? [row.ImageUrl]
                : [row.id];
      this.triggerDelete({ type, ids, row });
    },

    batchDelete() {
      if (this.selectedRows.length === 0) {
        this.$message.warning('请先选择要删除的记录');
        return;
      }
      const ids = this.activeTab === 'performance' ? this.selectedRows.map(r => r.id)
                : this.activeTab === 'crack'       ? this.selectedRows.map(r => r.ImageUrl)
                : this.selectedRows.map(r => r.id);
      this.triggerDelete({ type: this.activeTab, ids });
    },

    triggerDelete(task) {
      this.pendingDeleteTask = task;
      if (!this.deleteVerified) {
        // 首次需要密码验证
        this.confirmPassword = '';
        this.pwdDialogVisible = true;
      } else {
        this.executeDelete(task);
      }
    },

    async submitDelete() {
      if (!this.confirmPassword) {
        this.$message.warning('请输入密码');
        return;
      }
      this.deleting = true;
      try {
        const username = localStorage.getItem('username');
        // 用登录接口验证密码
        const res = await axios.post(`${BASE}/user/login`, {
          username,
          password: this.confirmPassword
        });
        if (res.data.code === 1) {
          this.deleteVerified = true;
          this.pwdDialogVisible = false;
          this.confirmPassword = '';
          await this.executeDelete(this.pendingDeleteTask);
        } else {
          this.$message.error('密码错误，请重新输入');
        }
      } catch(e) {
        this.$message.error('验证失败：' + e.message);
      } finally {
        this.deleting = false;
      }
    },

    async executeDelete({ type, ids, row }) {
      try {
        let res;
        if (type === 'performance') {
          // 按模型类型路由到不同删除接口
          const endpointMap = { BP: '/BPHistory/delete', PSO: '/PSOHistory/delete', 'PSO-BP': '/PSOBPHistory/delete' };
          const modelType = row ? row._type : 'BP';
          const endpoint = endpointMap[modelType] || '/BPHistory/delete';
          res = await axios.delete(`${BASE}${endpoint}`, { data: { ids }, headers: this.getHeaders() });
          if (res.data.code === 1) { this.$message.success('删除成功'); await this.loadPerf(); }
          else this.$message.error(res.data.msg || '删除失败');
        } else if (type === 'crack') {
          res = await axios.delete(`${BASE}/deleteCrack`, {
            params: { ImageUrls: ids },
            headers: this.getHeaders()
          });
          if (res.data.code === 1) { this.$message.success('删除成功'); await this.loadCrack(); }
          else this.$message.error(res.data.msg || '删除失败');
        } else if (type === 'temperature') {
          if (ids.length === 1) {
            res = await axios.delete(`${BASE}/tempHistory/delete/${ids[0]}`, { headers: this.getHeaders() });
          } else {
            res = await axios.delete(`${BASE}/tempHistory/batchDelete`, { data: { ids }, headers: this.getHeaders() });
          }
          if (res.data.code === 1) { this.$message.success('删除成功'); await this.loadTemp(); }
          else this.$message.error(res.data.msg || '删除失败');
        }
        this.applyDateFilter();
        this.clearSelection();
      } catch(e) {
        console.error('删除失败', e);
        this.$message.error('删除请求失败');
      }
    },

    // =========== 导出 Excel ===========
    exportExcel() {
      const tab = this.activeTab;
      const data = this.currentData;
      if (data.length === 0) { this.$message.warning('当前没有可导出的数据'); return; }

      let sheetFilter, sheetHeader, fileName;
      if (tab === 'performance') {
        fileName = '性能预测历史记录';
        sheetFilter = ['_type','aveLength','lisanValue','hardness','yieldStrength','strengthExtension','createTime'];
        sheetHeader = ['模型类型','平均晶粒尺寸','离散特征值','硬度','屈服强度','抗拉强度','创建时间'];
      } else if (tab === 'crack') {
        fileName = '裂纹识别历史记录';
        sheetFilter = ['ImageUrl','_crackCount','createTime'];
        sheetHeader = ['图片路径','裂纹数','识别时间'];
      } else {
        fileName = '温度预测历史记录';
        sheetFilter = ['fileName','temp','time'];
        sheetHeader = ['文件名','预测温度(℃)','预测时间'];
      }

      if (this.isAdmin) {
        sheetFilter.unshift('uid');
        sheetHeader.unshift('用户UID');
      }

      const option = {
        fileName,
        datas: [{
          sheetData: data,
          sheetName: '历史记录',
          sheetFilter,
          sheetHeader,
        }],
        type: 'xlsx'
      };
      const toExcel = new ExportJsonExcel(option);
      toExcel.saveExcel();
    },

    // =========== 分页 ===========
    handleSizeChange(val)    { this.pageSize = val; this.currentPage = 1; },
    handleCurrentChange(val) { this.currentPage = val; },
  }
};
</script>

<style lang="less" scoped>
.history-hub {
  padding: 24px;
  min-height: 100%;
  background: var(--bg-base);
  display: flex;
  flex-direction: column;
  gap: 20px;
  font-family: 'Inter', 'PingFang SC', sans-serif;
}

/* 顶部标题栏 */
.hub-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 28px;
  background: linear-gradient(135deg, rgba(0,212,255,0.08), rgba(124,58,237,0.08));
  border: 1px solid rgba(0,212,255,0.2);
  border-radius: var(--radius-lg);
}

.hub-title {
  display: flex;
  align-items: center;
  gap: 12px;

  i { font-size: 24px; color: var(--accent-primary); }

  span:first-of-type {
    font-size: 22px;
    font-weight: 700;
    background: var(--accent-gradient);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.hub-subtitle {
  font-size: 11px !important;
  letter-spacing: 2px;
  color: var(--text-muted) !important;
  -webkit-text-fill-color: var(--text-muted) !important;
  background: none !important;
}

.hub-actions {
  display: flex;
  align-items: center;
}

/* Tab 栏 */
.tab-bar {
  display: flex;
  gap: 12px;
}

.tab-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 24px;
  background: rgba(22,27,34,0.6);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.25s ease;
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 14px;

  i { font-size: 16px; }

  &:hover {
    border-color: rgba(0,212,255,0.4);
    color: var(--accent-primary);
    background: rgba(0,212,255,0.06);
  }

  &.active {
    border-color: var(--accent-primary);
    color: var(--accent-primary);
    background: rgba(0,212,255,0.1);
    box-shadow: 0 0 16px rgba(0,212,255,0.15);
  }
}

.tab-count {
  background: rgba(0,212,255,0.2);
  color: var(--accent-primary);
  border-radius: 20px;
  padding: 0 8px;
  font-size: 12px;
  font-weight: 700;
  min-width: 24px;
  text-align: center;
}

/* 内容区 */
.hub-content {
  flex: 1;
  background: rgba(22,27,34,0.6);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
  padding: 20px;
  backdrop-filter: blur(12px);
}

.admin-badge-bar {
  margin-bottom: 14px;
}

.empty-block {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 280px;
}

/* 表格 */
.hub-table {
  width: 100%;
  background: transparent !important;

  /deep/ .el-table__body tr:hover > td.el-table__cell {
    background: rgba(0,212,255,0.06) !important;
  }
  /deep/ .el-table tr,
  /deep/ .el-table td.el-table__cell {
    background: transparent !important;
    color: var(--text-primary) !important;
    border-bottom: 1px solid rgba(255,255,255,0.04) !important;
  }
  /deep/ .el-table::before { display: none; }
}

/* 分页 */
.hub-pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 底部操作浮动栏 */
.hub-footer {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 24px;
  background: rgba(22,27,34,0.9);
  border: 1px solid rgba(246,75,75,0.3);
  border-radius: var(--radius-md);
  backdrop-filter: blur(12px);

  .selected-tip {
    flex: 1;
    color: var(--text-secondary);
    font-size: 13px;
    b { color: #F56C6C; }
  }
}

/* 密码确认弹窗 */
.pwd-dialog-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 10px 0;
}

/deep/ .el-dialog {
  background: rgba(22,27,34,0.97) !important;
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-lg);
}
/deep/ .el-dialog__title { color: var(--text-primary); }
/deep/ .el-dialog__header { border-bottom: 1px solid var(--border-subtle); }
</style>
