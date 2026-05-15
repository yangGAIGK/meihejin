<template>
  <div class="temp-predict-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>图片温度预测 - 单图/局部框选</span>
      </div>

      <div class="upload-area" v-if="!imageUrl">
        <el-upload
          class="avatar-uploader"
          action=""
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handleChange">
          <el-button type="primary" icon="el-icon-picture">选择图片</el-button>
        </el-upload>
        <div class="el-upload__tip" style="margin-top: 10px;">选择图片后可框选需要预测的区域，仅支持jpg/png</div>
      </div>

      <div class="cropper-area" v-if="imageUrl">
        <div class="cropper-box">
          <vueCropper
            ref="cropper"
            :img="imageUrl"
            :outputSize="option.size"
            :outputType="option.outputType"
            :info="true"
            :full="option.full"
            :canMove="option.canMove"
            :canMoveBox="option.canMoveBox"
            :original="option.original"
            :autoCrop="option.autoCrop"
            :fixed="option.fixed"
            :fixedNumber="option.fixedNumber"
            :centerBox="option.centerBox"
            :infoTrue="option.infoTrue"
            :fixedBox="option.fixedBox"
          ></vueCropper>
        </div>
        <div class="action-btn">
          <el-button type="success" @click="startPredict">预测框选区域温度</el-button>
          <el-button @click="resetImage">重新选择</el-button>
        </div>
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
import { VueCropper } from 'vue-cropper'
import axios from 'axios'

export default {
  components: {
    VueCropper
  },
  data() {
    return {
      imageUrl: '',
      originalFileName: '',
      predictedTemp: null,
      uploadUrl: 'http://localhost:8080/predictTemperature',
      option: {
        size: 1,
        full: false,
        outputType: 'png',
        canMove: true,
        fixedBox: false,
        original: false,
        canMoveBox: true,
        autoCrop: true,
        centerBox: false,
        infoTrue: true
      }
    };
  },
  methods: {
    handleChange(file) {
      const isImage = file.raw.type === 'image/jpeg' || file.raw.type === 'image/png';
      if (!isImage) {
        this.$message.error('只能选择 JPG/PNG 格式图片!');
        return;
      }
      this.imageUrl = URL.createObjectURL(file.raw);
      this.originalFileName = file.name;
      this.predictedTemp = null;
    },
    resetImage() {
      this.imageUrl = '';
      this.originalFileName = '';
      this.predictedTemp = null;
    },
    startPredict() {
      this.$refs.cropper.getCropBlob((data) => {
        // 使用原始文件名，这样历史记录里显示的文件名有意义
        const fileName = this.originalFileName || 'crop.png';
        const formData = new FormData();
        formData.append('file', data, fileName);
        
        const token = localStorage.getItem('Authorization');
        axios.post(this.uploadUrl, formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization': token || ''
          }
        }).then(res => {
          if (res.data && res.data.code === 1) {
            this.predictedTemp = res.data.data.temperature;
            this.$message.success('预测成功');
          } else {
            this.$message.error(res.data.msg || '预测失败');
          }
        }).catch(err => {
          console.error(err);
          this.$message.error('预测请求失败，请检查网络或后端');
        });
      });
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
.upload-area {
  text-align: center;
  margin: 40px 0;
}
.cropper-area {
  margin-top: 20px;
}
.cropper-box {
  height: 400px;
  width: 100%;
  margin-bottom: 20px;
}
.action-btn {
  text-align: center;
  margin-bottom: 20px;
}
.result-area {
  margin-top: 20px;
}
</style>
