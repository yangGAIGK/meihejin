<template>
  <div class="temp-predict-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>图片温度预测</span>
      </div>
      
      <div class="upload-area">
        <el-upload
          class="avatar-uploader"
          :action="uploadUrl"
          :show-file-list="false"
          :on-success="handleSuccess"
          :before-upload="beforeUpload"
          :http-request="customUpload">
          <img v-if="imageUrl" :src="imageUrl" class="avatar">
          <i v-else class="el-icon-plus avatar-uploader-icon"></i>
        </el-upload>
        <div class="el-upload__tip">点击上传镁合金图片，只能上传jpg/png文件，且不超过2MB</div>
      </div>

      <div class="result-area" v-if="predictedTemp !== null">
        <el-alert
          :title="'预测温度: ' + predictedTemp + ' ℃'"
          type="success"
          :closable="false"
          show-icon>
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      imageUrl: '',
      predictedTemp: null,
      uploadUrl: 'http://localhost:8080/predictTemperature'
    };
  },
  methods: {
    handleSuccess(res, file) {
      this.imageUrl = URL.createObjectURL(file.raw);
    },
    beforeUpload(file) {
      const isImage = file.type === 'image/jpeg' || file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isImage) {
        this.$message.error('上传图片只能是 JPG/PNG 格式!');
      }
      if (!isLt2M) {
        this.$message.error('上传图片大小不能超过 2MB!');
      }
      return isImage && isLt2M;
    },
    async customUpload(params) {
      const formData = new FormData();
      formData.append('file', params.file);
      
      try {
        const token = localStorage.getItem('Authorization');
        const res = await axios.post(this.uploadUrl, formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization': token || ''
          }
        });
        
        if (res.data && res.data.code === 1) {
          this.predictedTemp = res.data.data.temperature;
          this.$message.success('预测成功');
          params.onSuccess(res.data, params.file);
        } else {
          this.$message.error(res.data.msg || '预测失败');
          params.onError(new Error(res.data.msg));
        }
      } catch (error) {
        console.error(error);
        this.$message.error('预测请求失败，请检查网络或后端');
        params.onError(error);
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
  max-width: 600px;
  margin: 0 auto;
}
.upload-area {
  text-align: center;
  margin-top: 30px;
  margin-bottom: 30px;
}
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  line-height: 178px;
  text-align: center;
  border: 1px dashed #d9d9d9;
}
.avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}
.result-area {
  margin-top: 20px;
}
</style>
