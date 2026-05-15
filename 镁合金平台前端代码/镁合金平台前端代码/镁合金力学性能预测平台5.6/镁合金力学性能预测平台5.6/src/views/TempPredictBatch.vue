<template>
  <div class="temp-predict-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>图片温度预测 - 批量预测</span>
      </div>

      <div class="action-bar">
        <el-upload
          action=""
          :auto-upload="false"
          :show-file-list="false"
          :multiple="true"
          :on-change="handleChange"
          style="display: inline-block; margin-right: 10px;">
          <el-button type="primary" icon="el-icon-folder-opened">批量选择图片</el-button>
        </el-upload>
        <el-button type="success" icon="el-icon-video-play" @click="startBatchPredict" :disabled="!hasPendingImages">开始批量预测</el-button>
        <el-button type="danger" icon="el-icon-delete" @click="clearList" :disabled="imageList.length === 0">清空列表</el-button>
      </div>

      <el-table :data="imageList" style="width: 100%" stripe border>
        <el-table-column type="index" label="序号" width="80" align="center"></el-table-column>
        <el-table-column label="图片预览" width="120" align="center">
          <template slot-scope="scope">
            <el-image
              style="width: 60px; height: 60px; border-radius: 4px;"
              :src="scope.row.url"
              :preview-src-list="[scope.row.url]">
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="文件名"></el-table-column>
        <el-table-column label="状态" width="120" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.status === '待预测'" type="info">{{ scope.row.status }}</el-tag>
            <el-tag v-else-if="scope.row.status === '预测中'" type="warning">{{ scope.row.status }}</el-tag>
            <el-tag v-else-if="scope.row.status === '成功'" type="success">{{ scope.row.status }}</el-tag>
            <el-tag v-else type="danger">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="temp" label="预测温度 (℃)" width="150" align="center">
          <template slot-scope="scope">
            <span v-if="scope.row.temp !== null" style="color: #67C23A; font-weight: bold;">{{ scope.row.temp }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-button type="text" style="color: #F56C6C;" @click="removeItem(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  data() {
    return {
      imageList: [],
      uploadUrl: 'http://localhost:8080/predictTemperature',
    };
  },
  computed: {
    hasPendingImages() {
      return this.imageList.some(img => img.status === '待预测' || img.status === '失败');
    }
  },
  methods: {
    handleChange(file, fileList) {
      const isImage = file.raw.type === 'image/jpeg' || file.raw.type === 'image/png';
      if (!isImage) {
        this.$message.error(`${file.name} 不是 JPG/PNG 格式!`);
        return;
      }
      // 防止重复添加
      const exists = this.imageList.some(img => img.name === file.name && img.size === file.size);
      if (!exists) {
        this.imageList.push({
          raw: file.raw,
          url: URL.createObjectURL(file.raw),
          name: file.name,
          size: file.size,
          status: '待预测',
          temp: null
        });
      }
    },
    removeItem(index) {
      this.imageList.splice(index, 1);
    },
    clearList() {
      this.imageList = [];
    },
    async startBatchPredict() {
      const token = localStorage.getItem('Authorization');
      let successCount = 0;
      
      for (let i = 0; i < this.imageList.length; i++) {
        let img = this.imageList[i];
        if (img.status === '成功' || img.status === '预测中') continue;
        
        this.$set(this.imageList, i, { ...img, status: '预测中' });
        img = this.imageList[i];
        const formData = new FormData();
        formData.append('file', img.raw);
        
        try {
          const res = await axios.post(this.uploadUrl, formData, {
            headers: {
              'Content-Type': 'multipart/form-data',
              'Authorization': token || ''
            }
          });
          
          if (res.data && res.data.code === 1) {
            this.$set(this.imageList, i, { ...img, status: '成功', temp: res.data.data.temperature });
            successCount++;
          } else {
            this.$set(this.imageList, i, { ...img, status: '失败' });
          }
        } catch (error) {
          console.error(error);
          this.$set(this.imageList, i, { ...img, status: '失败' });
        }
      }
      
      if (successCount > 0) {
        this.$message.success(`批量预测完成，成功 ${successCount} 张图片`);
      }
    }
  }
}
</script>

<style scoped>
.temp-predict-container {
  padding: 20px;
}
.box-card {
  margin: 0 auto;
}
.action-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}
</style>
