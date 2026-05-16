<template>
  <div class="profile-container">
    <el-card class="profile-card" shadow="hover">
      <!-- 顶部标题栏 -->
      <div class="profile-title">
        <h2>个人信息设置</h2>
        <p class="subtitle">管理你的账户资料与安全设置</p>
      </div>

      <div class="profile-content">
        <!-- 左侧：头像区域 -->
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <img 
              :src="displayedAvatar" 
              class="avatar" 
              alt="头像" 
              @error="e => e.target.src = defaultAvatar"
            />
            <div class="avatar-overlay">
              <div class="avatar-upload-btn" @click="triggerUpload">
                <i class="el-icon-camera"></i>
                <span>更换头像</span>
              </div>
            </div>
          </div>
          <p class="avatar-tip">支持 JPG/PNG，大小不超过 2MB</p>
        </div>

        <!-- 右侧：表单区域 -->
        <div class="form-section">
          <el-form :model="profile" label-width="90px" class="profile-form">
            <el-form-item label="用户名">
              <el-input 
                v-model="profile.username" 
                placeholder="请输入用户名"
                class="custom-input"
              >
                <i slot="prefix" class="el-icon-user"></i>
              </el-input>
            </el-form-item>

            <el-form-item label="操作">
              <div class="button-group">
                <el-button 
                  type="primary" 
                  @click="showPasswordDialog"
                  class="btn-primary-gradient"
                >
                  <i class="el-icon-key"></i> 修改密码
                </el-button>
                <el-button 
                  type="success" 
                  @click="submitForm"
                  class="btn-success-gradient"
                  :loading="loading"
                >
                  <i class="el-icon-check"></i> 保存修改
                </el-button>
              </div>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>

    <!-- 修改密码弹窗 -->
    <el-dialog 
      title="修改密码" 
      :visible.sync="passwordDialogVisible" 
      width="420px"
      class="password-dialog"
    >
      <el-form 
        :model="passwordForm" 
        :rules="passwordRules" 
        ref="passwordForm" 
        label-width="80px"
      >
        <el-form-item label="旧密码" prop="old_pwd">
          <el-input 
            v-model="passwordForm.old_pwd" 
            type="password" 
            placeholder="请输入旧密码"
            class="custom-input"
            prefix-icon="el-icon-lock"
          ></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="new_pwd">
          <el-input 
            v-model="passwordForm.new_pwd" 
            type="password" 
            placeholder="请输入新密码（不少于6位）"
            class="custom-input"
            prefix-icon="el-icon-key"
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="re_pwd">
          <el-input 
            v-model="passwordForm.re_pwd" 
            type="password" 
            placeholder="请再次输入新密码"
            class="custom-input"
            prefix-icon="el-icon-key"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="passwordDialogVisible = false" class="btn-cancel">取消</el-button>
        <el-button 
          type="primary" 
          @click="submitPasswordForm"
          class="btn-primary-gradient"
          :loading="loading"
        >
          确定修改
        </el-button>
      </div>
    </el-dialog>

    <!-- 隐藏的上传组件 -->
    <el-upload
      ref="upload"
      class="avatar-uploader"
      action="#"
      :show-file-list="false"
      :before-upload="beforeAvatarUpload"
      :http-request="uploadAvatar"
    >
    </el-upload>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex';
import axios from 'axios';

export default {
  name: 'Profile',
  data() {
    return {
      profile: {
        username: '',
        UserUrl: ''
      },
      passwordForm: {
        old_pwd: '',
        new_pwd: '',
        re_pwd: ''
      },
      passwordDialogVisible: false,
      loading: false,
      defaultAvatar: "https://tyut123.oss-cn-hangzhou.aliyuncs.com/logo.jpeg",
      passwordRules: {
        old_pwd: [
          { required: true, message: '请输入旧密码', trigger: 'blur' }
        ],
        new_pwd: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        re_pwd: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: this.validateRePwd, trigger: 'blur' }
        ]
      }
    };
  },
  computed: {
    ...mapState('user', ['avatar']),
    displayedAvatar() {
      return this.avatar || this.defaultAvatar;
    }
  },
  created() {
    this.fetchUserProfile();
  },
  methods: {
    ...mapActions('user', ['updateAvatar', 'clearUserData']),
    validateRePwd(rule, value, callback) {
      if (value !== this.passwordForm.new_pwd) {
        callback(new Error('两次输入的新密码不一致'));
      } else {
        callback();
      }
    },
    async fetchUserProfile() {
      try {
        const token = localStorage.getItem('Authorization');
        const response = await axios.get("http://localhost:8080/user/info", {
          headers: { Authorization: token }
        });
        if (response.data) {
          this.profile.username = response.data.username || '';
          this.profile.UserUrl = response.data.avatar || '';
        }
      } catch (error) {
        console.error("获取用户信息失败:", error);
        this.$message.error("获取用户信息失败");
      }
    },
    triggerUpload() {
      this.$refs.upload.$refs['upload-inner'].handleClick();
    },
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isJPG) {
        this.$message.error('只能上传JPG/PNG格式图片!');
        return false;
      }
      if (!isLt2M) {
        this.$message.error('图片大小不能超过2MB!');
        return false;
      }
      return true;
    },
    async uploadAvatar({ file }) {
      try {
        this.loading = true;
        const formData = new FormData();
        formData.append("file", file);
        const token = localStorage.getItem('Authorization');
        const response = await axios.post("http://localhost:8080/user/upload", formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
            Authorization: token
          }
        });
        if (response.data?.code === 1) {
          let avatarUrl = response.data.data;
          if (!avatarUrl.startsWith('http')) {
            if (avatarUrl.startsWith('//')) {
              avatarUrl = "http:" + avatarUrl;
            } else if (!avatarUrl.includes("://")) {
              avatarUrl = "http://localhost:8080" + avatarUrl;
            }
          }
          const fullAvatarUrl = avatarUrl + "?t=" + new Date().getTime();
          this.updateAvatar(fullAvatarUrl);
          this.profile.UserUrl = avatarUrl;
          this.$message.success("头像上传成功");
        } else {
          this.$message.error(response.data?.msg || "头像上传失败");
        }
      } catch (error) {
        console.error("上传失败:", error);
        this.$message.error("头像上传失败，请重试");
      } finally {
        this.loading = false;
      }
    },
    showPasswordDialog() {
      this.passwordDialogVisible = true;
    },
    async submitForm() {
      try {
        this.loading = true;
        const token = localStorage.getItem('Authorization');
        await axios.put("http://localhost:8080/user/update", {
          username: this.profile.username,
          UserUrl: this.profile.UserUrl.split('?')[0]
        }, {
          headers: { Authorization: token }
        });
        this.$message.success("修改成功");
      } catch (error) {
        console.error("修改失败:", error);
        this.$message.error("修改失败，请重试");
      } finally {
        this.loading = false;
      }
    },
    async submitPasswordForm() {
      try {
        const valid = await this.$refs.passwordForm.validate();
        if (valid) {
          this.loading = true;
          const token = localStorage.getItem('Authorization');
          await axios.put("http://localhost:8080/user/updatePwd", {
            username: this.profile.username,
            old_pwd: this.passwordForm.old_pwd,
            new_pwd: this.passwordForm.new_pwd
          }, { headers: { Authorization: token } });
          this.$message.success("密码修改成功");
          this.passwordDialogVisible = false;
          this.passwordForm = { old_pwd: "", new_pwd: "", re_pwd: "" };
        }
      } catch (error) {
        console.error("密码修改失败:", error);
        this.$message.error("密码修改失败，请重试");
      } finally {
        this.loading = false;
      }
    }
  }
}
</script>

<style lang="less" scoped>
.profile-container {
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start; /* 关键修改：改为顶部对齐 */
  min-height: calc(100vh - 120px);
  background: linear-gradient(135deg, #12171f 0%, #1a202c 100%);
  padding-top: 60px; /* 增加顶部内边距，让卡片整体上移 */
}

.profile-card {
  width: 100%;
  max-width: 700px; /* 稍微收窄卡片，更贴合你的截图 */
  background: linear-gradient(145deg, rgba(22, 27, 34, 0.95), rgba(16, 20, 28, 0.98));
  border: 1px solid var(--border-subtle);
  border-radius: 12px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.4),
    0 0 0 1px rgba(0, 212, 255, 0.1);
  padding: 30px 40px; /* 调整上下内边距 */
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 
      0 12px 40px rgba(0, 0, 0, 0.5),
      0 0 0 1px rgba(0, 212, 255, 0.2);
  }
}

.profile-title {
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);

  h2 {
    color: #60a5fa; /* 改为你截图里的蓝色 */
    font-size: 22px;
    font-weight: 600;
    margin: 0 0 6px 0;
  }

  .subtitle {
    color: var(--text-muted);
    font-size: 13px;
    margin: 0;
  }
}

.profile-content {
  display: flex;
  align-items: center; /* 垂直居中对齐 */
  gap: 60px;
  justify-content: center;
}

/* 头像区域优化 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-wrapper {
  position: relative;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  border: 3px solid transparent;
  background: linear-gradient(#1a202c, #1a202c) padding-box,
              linear-gradient(135deg, #60a5fa, #a855f7) border-box;
  box-shadow: 
    0 0 20px rgba(96, 165, 250, 0.3),
    inset 0 0 20px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  transition: all 0.3s ease;

  &:hover {
    transform: scale(1.05);
    box-shadow: 
      0 0 30px rgba(96, 165, 250, 0.5),
      inset 0 0 20px rgba(0, 0, 0, 0.3);
  }
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  cursor: pointer;

  .avatar-upload-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: #fff;
    font-size: 14px;

    i {
      font-size: 24px;
      margin-bottom: 4px;
      color: #60a5fa;
    }
  }
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.avatar-tip {
  margin-top: 16px;
  color: var(--text-muted);
  font-size: 12px;
  text-align: center;
}

/* 表单区域优化 */
.form-section {
  flex: 1;
  max-width: 350px;
}

.profile-form {
  .el-form-item {
    margin-bottom: 24px;
  }

  .custom-input {
    .el-input__inner {
      background: rgba(16, 20, 28, 0.9) !important;
      border: 1px solid rgba(255, 255, 255, 0.1) !important;
      color: var(--text-secondary) !important;
      border-radius: 6px !important;
      height: 44px !important;
      line-height: 44px !important;
      font-size: 14px !important;
      padding-left: 40px !important;
      transition: all 0.3s ease;

      &:focus {
        border-color: #60a5fa !important;
        box-shadow: 0 0 12px rgba(96, 165, 250, 0.3) !important;
        background: rgba(16, 20, 28, 1) !important;
      }
    }

    .el-input__prefix {
      left: 12px !important;
      color: var(--text-muted);
    }
  }

  .button-group {
    display: flex;
    gap: 16px;
  }

  .el-button {
    border-radius: 6px;
    padding: 10px 24px;
    font-size: 14px;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 6px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-2px);
    }
  }

  .btn-primary-gradient {
    background: linear-gradient(90deg, #60a5fa, #a855f7);
    border: none;
    box-shadow: 0 4px 15px rgba(96, 165, 250, 0.3);

    &:hover {
      box-shadow: 0 6px 20px rgba(96, 165, 250, 0.5);
    }
  }

  .btn-success-gradient {
    background: linear-gradient(90deg, #10b981, #06b6d4);
    border: none;
    box-shadow: 0 4px 15px rgba(16, 185, 129, 0.3);

    &:hover {
      box-shadow: 0 6px 20px rgba(16, 185, 129, 0.5);
    }
  }
}

/* 弹窗样式优化 */
.password-dialog {
  :deep(.el-dialog__header) {
    background: rgba(96, 165, 250, 0.05);
    border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    padding: 20px;

    .el-dialog__title {
      color: #fff;
      font-size: 18px;
      font-weight: 600;
    }
  }

  :deep(.el-dialog__body) {
    padding: 24px;
    background: #1a202c;
  }

  :deep(.el-dialog__footer) {
    background: rgba(0, 0, 0, 0.2);
    padding: 16px 20px;
    border-top: 1px solid rgba(255, 255, 255, 0.08);
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;

    .btn-cancel {
      background: transparent;
      border: 1px solid rgba(255, 255, 255, 0.2);
      color: #fff;

      &:hover {
        border-color: #60a5fa;
        color: #60a5fa;
      }
    }
  }
}

.avatar-uploader {
  display: none;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .profile-content {
    flex-direction: column;
    align-items: center;
    gap: 30px;
  }

  .form-section {
    width: 100%;
    max-width: 100%;
  }

  .profile-card {
    padding: 24px;
  }
}
</style>