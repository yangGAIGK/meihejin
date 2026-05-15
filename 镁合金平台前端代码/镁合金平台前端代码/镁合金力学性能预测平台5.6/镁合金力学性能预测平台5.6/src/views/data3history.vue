<template>
  <div class="container">
    <div class="main-content">
      <div class="tables-container">
        <!-- 图像表 (左侧) -->
        <div class="table-section">
          <h3 class="section-title">图像表</h3>
          <div class="table-wrapper">
            <el-table
                :data="filteredImageRecords"
                border
                stripe
                style="width: 100%"
                header-row-class-name="header-row"
                @selection-change="handleSelectionChange"
            >
              <el-table-column v-if="showSelection" type="selection" width="55" align="center"></el-table-column>

              <el-table-column label="序号" width="80" align="center">
                <template #default="scope">
                  {{ (imageCurrentPage - 1) * imagePageSize + scope.$index + 1 }}
                </template>
              </el-table-column>

              <el-table-column label="原始图像" width="200" align="center">
                <template #default="scope">
                  <img
                      :src="scope.row.ImageUrl"
                      alt="原始图像"
                      style="max-width: 100%; max-height: 100px;"
                  />
                </template>
              </el-table-column>

              <el-table-column prop="createTime" label="上传时间" width="180" align="center">
                <template #default="scope">
                  {{ formatDate(scope.row.createTime) }}
                </template>
              </el-table-column>

              <el-table-column label="操作" width="120" align="center">
                <template #default="scope">
                  <el-button type="primary" size="mini" @click.stop="handleQuery(scope.row)">
                    查询
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="table-actions">
            <el-button type="danger" @click="handleShowSelection" >
              删除
            </el-button>
            <el-button v-if="showSelection" type="primary" @click="handleDeleteSelected" :disabled="multipleSelection.length === 0">
              确认删除
            </el-button>
            <el-pagination
                background
                layout="prev, pager, next"
                :total="imageTotal"
                :page-size="imagePageSize"
                v-model="imageCurrentPage"
                @current-change="handleImagePageChange"
                class="pagination"
            />
          </div>
        </div>

        <!-- 右侧内容 -->
        <div class="right-content">
          <div class="binary-image-section" >
            <h3 class="section-title">二值图像</h3>
            <img
                v-if="selectedImage"
                :src="selectedImage.BinaryUrl"
                alt="二值图像"
                class="binary-image"
            />
            <div v-else class="no-data-message">请选择图像进行查询</div>
          </div>

          <!-- 裂纹信息表 -->
          <div class="crack-info-section" >
            <h3 class="section-title">裂纹信息表</h3>
            <div v-if="crackRecords.length > 0">
              <el-table
                  :data="crackRecords"
                  border
                  stripe
                  style="width: 100%"
                  header-row-class-name="header-row"
              >
                <el-table-column label="序号" width="80" align="center">
                  <template #default="scope">
                    {{ scope.$index + 1 }}
                  </template>
                </el-table-column>
                <el-table-column prop="CrackWidth" label="裂纹宽度" width="100" align="center"></el-table-column>
                <el-table-column prop="CrackStart" label="裂纹起点" width="120" align="center"></el-table-column>
                <el-table-column prop="CrackPerimeter" label="裂纹周长" width="120" align="center"></el-table-column>
                <el-table-column prop="CrackHeight" label="裂纹高度" width="100" align="center"></el-table-column>
                <el-table-column prop="CrackArea" label="裂纹面积" width="100" align="center"></el-table-column>
                <el-table-column prop="CrackLength" label="裂纹长度" width="100" align="center"></el-table-column>
              </el-table>
            </div>
            <div v-else class="no-crack-message">未识别到裂纹</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { format } from 'date-fns'; // 引入 date-fns 的 format 方法
import Qs from 'qs'; // 引入 qs

export default {
  data() {
    return {
      // 图像表数据
      imageRecords: [],
      imageTotal: 5, // 直接赋值，因为 total 只有 5 个
      imageCurrentPage: 1,
      imagePageSize: 10,
      selectedImage: null, // 用于存储选中的图像

      // 裂纹信息表数据
      crackRecords: [],

      // 批量删除相关
      multipleSelection: [], // 用于存储选中的图像记录
      showSelection: false, // 控制是否显示选择列
    };
  },

  created() {
    // 页面加载时自动获取图像记录
    this.fetchImageRecords();
  },

  computed: {
    // 过滤掉包含空值的图像记录
    filteredImageRecords() {
      return this.imageRecords.filter(record => {
        // 检查记录中的所有值是否都不为 null
        return Object.values(record).every(value => value !== null && value !== undefined);
      });
    }
  },

  methods: {
    // 获取图像表记录
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

    // 处理图像分页变化
    handleImagePageChange(page) {
      this.imageCurrentPage = page;
      this.fetchImageRecords();
    },

    // 处理查询按钮点击事件
    async handleQuery(row) {
      this.selectedImage = row; // 将选中的图像赋值给 selectedImage

      // 获取裂纹信息
      try {
        const response = await axios.post('http://localhost:8080/crackQuery', {
          ImageUrl: row.ImageUrl,
        });

        if (response.data.code === 1) {
          this.crackRecords = response.data.data.rows;
        } else {
          this.$message.error(response.data.msg || '获取裂纹信息失败');
          this.crackRecords = []; // 清空裂纹信息
        }
      } catch (error) {
        console.error('获取裂纹信息失败:', error);
        this.$message.error('获取裂纹信息失败，请稍后重试');
        this.crackRecords = []; // 清空裂纹信息
      }
    },

    // 格式化日期时间
    formatDate(isoString) {
      if (!isoString) {
        return ''; // 或者返回其他默认值，例如 'N/A'
      }
      try {
        return format(new Date(isoString), 'yyyy-MM-dd HH:mm:ss'); // 使用 date-fns 格式化
      } catch (error) {
        console.error('日期格式化失败:', error);
        return 'Invalid Date'; // 或者返回其他错误提示
      }
    },

    // 处理表格选择变化
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },

    // 处理批量删除
    async handleDeleteSelected() {
      if (this.multipleSelection.length === 0) {
        this.$message.warning('请选择要删除的图像');
        return;
      }

      this.$confirm('确定要删除选中的图像吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
          .then(async () => {
            const imageUrls = this.multipleSelection.map(item => item.ImageUrl);
            try {
              // 使用 DELETE 请求，并将 ImageUrls 作为查询参数传递
              const response = await axios.delete(`http://localhost:8080/deleteCrack?${Qs.stringify({ ImageUrls: imageUrls }, { arrayFormat: 'repeat' })}`);

              if (response.data.code === 1) {
                this.$message.success('删除成功');
                // 重新获取图像记录
                await this.fetchImageRecords();
                this.showSelection = false; //隐藏多选框
              } else {
                this.$message.error(response.data.msg || '删除失败');
              }
            } catch (error) {
              console.error('删除失败:', error);
              this.$message.error('删除失败，请稍后重试');
            }
          })
          .catch(() => {
            this.$message.info('已取消删除');
          });
    },
    //显示多选框
    handleShowSelection(){
      this.showSelection = true;
    }
  },
};
</script>

<style lang="less" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px;
  background-color: var(--bg-base);
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.tables-container {
  display: flex;
  flex: 1;
  gap: 16px;
  overflow: hidden;
}

.table-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
}

.section-title {
  padding: 14px 16px;
  font-size: 14px;
  font-weight: 600;
  color: var(--accent-primary);
  border-bottom: 1px solid var(--border-subtle);
  letter-spacing: 0.5px;
  margin: 0;
}

.table-actions {
  padding: 12px 16px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  border-top: 1px solid var(--border-subtle);
}

.pagination {
  margin-top: 0;
}

.el-table {
  margin-top: 0;
}

/* scoped 内强制覆盖表头硬编码颜色 */
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

/deep/ .el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background-color: rgba(0,0,0,0.12) !important;
}

/deep/ .el-table__body tr:hover > td.el-table__cell {
  background-color: rgba(0, 212, 255, 0.07) !important;
}

.right-content {
  flex: 1;
  margin-left: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.binary-image-section {
  background-color: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.binary-image {
  object-fit: contain;
  width: 100%;
  height: 100%;
  border-radius: var(--radius-sm);
}

.crack-info-section {
  background-color: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: 16px;
  flex: 1;
  overflow: auto;
}

.table-wrapper {
  overflow-y: auto;
  max-height: calc(100vh - 200px);
}

.no-crack-message,
.no-data-message {
  text-align: center;
  font-size: 14px;
  color: var(--text-muted);
  padding: 24px;
}
</style>


