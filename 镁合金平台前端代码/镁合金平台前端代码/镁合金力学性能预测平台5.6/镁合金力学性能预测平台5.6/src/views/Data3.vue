<template>
  <div
      class="container"
      v-loading="loading"
      :element-loading-text="loadingText"
      element-loading-spinner="el-icon-loading"
      element-loading-background="rgba(0, 0, 0, 0.7)"
  >
    <div class="upload-buttons">
      <input
          type="file"
          id="file-input"
          style="position: absolute; top: -9999px; left: -9999px;"
          @change="handleFileSelect"
          accept="image/*"
          multiple
      />
      <div class="button-group">
        <el-button
            size="small"
            type="primary"
            class="upload-button"
            @click="triggerFileInput"
        >
          选取图像
        </el-button>
        <el-button
            size="small"
            type="success"
            @click="submitBatchUpload"
            :disabled="fileList.length === 0"
            class="upload-button"
        >
          批量处理分析
        </el-button>
      </div>
    </div>

    <div class="content">
      <div class="file-list" v-if="fileList.length > 0">
        <div
            v-for="(file, index) in fileList"
            :key="index"
            class="file-item"
        >
          <span class="file-name">{{ file.name }}</span>
          <el-button
              type="text"
              @click="removeFile(index)"
              class="remove-button"
          >
            ×
          </el-button>
        </div>
      </div>

      <div class="image-display-container scrollable" v-show="fileList.length > 0">
        <div
            v-for="(fileItem, index) in fileList"
            :key="index"
            class="image-box"
        >
          <h3>原始图像 {{ index + 1 }}</h3>
          <img
              :src="fileItem.previewUrl"
              alt="原始图像"
              class="uploaded-image"
          />
        </div>
      </div>

      <el-empty
          v-if="fileList.length === 0 && !loading"
          description="请选取并上传图像进行处理与分析"
          style="background-color: #fff; margin-top: 20px;"
      ></el-empty>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      loading: false,
      loadingText: "正在处理图像，请稍候...",
      fileList: [],
      uid: null,
      processingQueue: [],
      currentProcessing: 0,
    };
  },
  mounted() {
    this.checkLoginStatus();
  },
  methods: {
    checkLoginStatus() {
      this.uid = localStorage.getItem("uid");
    },
    triggerFileInput() {
      document.getElementById("file-input").click();
    },
    handleFileSelect(event) {
      const files = Array.from(event.target.files);
      event.target.value = null;

      if (files.length > 0) {
        this.fileList = files.map(file => ({
          file,
          name: file.name,
          previewUrl: URL.createObjectURL(file),
          processing: false,
          result: null
        }));
        this.clearResults();
      }
    },
    removeFile(index) {
      const fileItem = this.fileList[index];
      if (fileItem.previewUrl && fileItem.previewUrl.startsWith("blob:")) {
        URL.revokeObjectURL(fileItem.previewUrl);
      }
      this.fileList.splice(index, 1);
      if (this.fileList.length === 0) this.clearResults();
    },

    clearResults() {
    },

    async submitBatchUpload() {
      if (this.fileList.length === 0) {
        this.$message.warning("请先选择要处理的图像文件！");
        return;
      }

      this.loading = true;
      this.currentProcessing = 0;
      this.loadingText = `正在处理 ${this.currentProcessing + 1}/${this.fileList.length} 张图像...`;

      this.processingQueue = this.fileList.map((fileItem, index) =>
          this.processSingleFile(fileItem.file, index)
      );

      try {
        await Promise.all(this.processingQueue);
        this.$message.success(`成功处理 ${this.fileList.length} 张图像！`);
      } catch (error) {
        console.error("Batch processing failed:", error);
        this.$message.error("批量处理过程中发生错误，请检查控制台！");
      } finally {
        this.loading = false;
      }
    },

    async processSingleFile(file, index) {
      try {
        this.currentProcessing = index;
        this.loadingText = `正在处理 ${index + 1}/${this.fileList.length} 张图像...`;

        const formData = new FormData();
        formData.append("image", file);

        const response = await axios.post(
            "http://127.0.0.1:5000/imagepredict",
            formData,
            { headers: { "Content-Type": "multipart/form-data" } }
        );

        // 处理响应
        const analysisResult = response.data.analysis;
        const binaryImageB64 = response.data.binary_image_b64;

        // 上传原始图像
        const originalImageUrl = await this.uploadImageToOSS(file);

        // 上传二值图像
        const binaryImageUrl = await this.uploadBase64ImageToOSS(binaryImageB64, file.name);

        // 准备上传到后端的数据
        const uploadData = {
          ImageUrl: originalImageUrl, // 使用原始图像的 OSS URL
          BinaryUrl: binaryImageUrl, // 使用二值图像的 OSS URL
          CrackCount: analysisResult.crack_count,
          rows: analysisResult.crack_info.map((crack) => ({
            CrackStart: `(${crack.position[0]},${crack.position[1]})`,
            CrackLength: crack.length,
            CrackWidth: crack.width,
            CrackHeight: crack.height,
            CrackArea: crack.area,
            CrackPerimeter: crack.perimeter,
          })),
        };

        // *******************  调试信息  *******************
        console.log("发送给后端的数据 (uploadData):", uploadData); // 打印 uploadData 对象
        // *******************  调试信息  *******************

        // 上传数据到后端
        try {
          const uploadResponse = await axios.post("http://localhost:8080/insertCrack", uploadData, {
            headers: {
              Authorization: this.getAuthorization(),
            },
          });
          console.log(`File ${file.name} processed and data uploaded successfully!`, uploadResponse); // 打印 axios 的响应
          this.$router.push('/data3history'); // 上传成功后跳转
        } catch (uploadError) {
          console.error(`Error uploading data for file ${file.name}:`, uploadError);
          this.$message.error(`上传文件 ${file.name} 数据失败！ ${uploadError.message} (请查看控制台)`);
          console.error("Axios 错误响应:", uploadError.response); // 打印 axios 的错误响应
          console.error("后端返回的错误信息:", uploadError.response.data); // 打印后端返回的错误信息
        }
      } catch (err) {
        console.error(`Error processing file ${file.name}:`, err);
        this.$message.error(`处理文件 ${file.name} 失败！`);
      }
    },

    async uploadImageToOSS(file) {
      try {
        const formData = new FormData();
        formData.append("file", file);

        const response = await axios.post(
            "http://localhost:8080/user/upload", // 假设 /user/upload 接口也支持上传其他图片
            formData,
            {
              headers: {
                "Content-Type": "multipart/form-data",
                Authorization: this.getAuthorization(),
              },
            }
        );

        if (response.data && response.data.code === 1) {
          console.log("原始图像上传成功", response.data.data);
          return response.data.data; // 返回 OSS 文件的 URL
        } else {
          this.$message.error(response.data.msg || "上传原始图像失败");
          return null;
        }
      } catch (error) {
        console.error("上传原始图像失败:", error);
        this.$message.error("上传原始图像失败，请重试");
        return null;
      }
    },
    async uploadBase64ImageToOSS(base64Data, filename) {
      try {
        // 将 Base64 数据转换为 Blob 对象
        const blob = this.dataURLtoBlob(base64Data);

        // 创建 FormData 对象
        const formData = new FormData();
        formData.append("file", blob, filename); // 将 Blob 对象添加到 FormData 中，并指定文件名

        const response = await axios.post(
            "http://localhost:8080/user/upload", // 假设 /user/upload 接口也支持上传其他图片
            formData,
            {
              headers: {
                "Content-Type": "multipart/form-data",
                Authorization: this.getAuthorization(),
              },
            }
        );

        if (response.data && response.data.code === 1) {
          console.log("二值图像上传成功", response.data.data);
          return response.data.data; // 返回 OSS 文件的 URL
        } else {
          this.$message.error(response.data.msg || "上传二值图像失败");
          return null;
        }
      } catch (error) {
        console.error("上传二值图像失败:", error);
        this.$message.error("上传二值图像失败，请重试");
        return null;
      }
    },
    getAuthorization() {
      // 尝试从 localStorage 获取 token
      let token = localStorage.getItem("token");
      // 如果 localStorage 中没有 token，尝试从 sessionStorage 获取
      if (!token) {
        token = sessionStorage.getItem("token");
      }
      return token ? `Bearer ${token}` : null;
    },
    // 将 Data URL 转换为 Blob 对象
    dataURLtoBlob(dataurl) {
      let arr = dataurl.split(','),
          mime = arr[0].match(/:(.*?);/)[1],
          bstr = atob(arr[1]),
          n = bstr.length,
          u8arr = new Uint8Array(n);
      while (n--) {
        u8arr[n] = bstr.charCodeAt(n);
      }
      return new Blob([u8arr], { type: mime });
    },
  },
  beforeDestroy() {
    this.fileList.forEach(fileItem => {
      if (fileItem.previewUrl && fileItem.previewUrl.startsWith("blob:")) {
        URL.revokeObjectURL(fileItem.previewUrl);
      }
    });
  },
};
</script>

<style scoped lang="less">
.container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
  background-color: #f9f9f9;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.content {
  padding: 0px 20px 20px;
  overflow-y: auto;
}

.image-display-container {
  &.scrollable {
    max-height: 50vh;
    overflow-y: auto;
    padding: 10px 0;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    background: #fff;
    margin-bottom: 20px;
  }

  .image-box {
    min-width: 200px;
    margin: 10px;

    h3 {
      font-size: 14px;
      margin-bottom: 10px;
    }
  }
}

.uploaded-image {
  max-width: 100%;
  height: auto;
  max-height: 400px;
  object-fit: contain;
  border-radius: 4px;
  border: 1px solid #ddd;
  margin-top: auto;
}

.button-group {
  display: flex;
  gap: 12px;
}

.upload-buttons {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 20px;
}

.upload-button {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 20px;
  font-family: Arial, sans-serif;
  line-height: 1.5;
  margin: 0 !important;
  border: none;
  box-shadow: none;
  text-align: center;
}

.upload-button.el-button--primary {
  background-color: #409EFF;
  border-color: #409EFF;
  color: #fff;
}

.upload-button.el-button--primary:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.upload-button.el-button--success {
  background-color: #67C23A;
  border-color: #67C23A;
  color: #fff;
}

.upload-button.el-button--success:hover {
  background-color: #85ce61;
  border-color: #85ce61;
}

.upload-button.el-button--success.is-disabled {
  background-color: #b3d8b3;
  border-color: #b3d8b3;
  color: #fff;
}

.el-empty {
  background-color: #fff;
  padding: 40px 0;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.file-list {
  margin-bottom: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fff;

  .file-item {
    display: flex;
    align-items: center;
    padding: 8px 15px;
    border-bottom: 1px solid #ebeef5;

    &:last-child {
      border-bottom: none;
    }

    .file-name {
      flex: 1;
      font-size: 14px;
      color: #606266;
    }

    .remove-button {
      font-size: 20px;
      color: #909399;
      padding: 0 5px;

      &:hover {
        color: #303133;
      }
    }
  }
}
</style>
