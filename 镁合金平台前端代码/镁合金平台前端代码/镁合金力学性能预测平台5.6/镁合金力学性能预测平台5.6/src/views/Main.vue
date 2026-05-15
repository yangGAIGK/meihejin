<template>
  <el-container class="home-container">
    <!-- 第一行：Logo + 右侧用户信息 -->
    <el-header style="height: 60px;" class="top-bar">
      <div class="header-left">
        <div class="logo-icon">⬡</div>
        <div class="header-title">
          <span class="title-main">镁合金智能分析平台</span>
          <span class="title-sub">MAGNESIUM ALLOY ANALYSIS PLATFORM</span>
        </div>
      </div>
      <div class="header-right">
        <div class="user-info">
          <img :src="displayedAvatar" class="avatar" alt="avatar" @error="e => e.target.src = defaultAvatar" />
          <div class="online-dot"></div>
        </div>
        <el-button class="logout-btn" @click="logout">
          <i class="el-icon-switch-button"></i> 退出
        </el-button>
      </div>
    </el-header>

    <!-- 第二行：横向导航菜单 -->
    <div class="nav-bar">
      <Aside></Aside>
    </div>

    <!-- 主内容区 -->
    <el-main>
      <router-view></router-view>
    </el-main>
  </el-container>
</template>

<script>
import Aside from '@/components/Aside.vue';
import axios from 'axios';
import { mapState, mapActions } from 'vuex';

export default {
  name: 'Main',
  components: {
    Aside
  },
  data() {
    return {
      loading: false,
      defaultAvatar: "https://tyut123.oss-cn-hangzhou.aliyuncs.com/logo.jpeg",
    };
  },
  computed: {
    ...mapState('user', ['avatar']), // 从Vuex获取头像
    displayedAvatar() {
      return this.avatar || this.defaultAvatar;
    }
  },
  created() {
    this.setupInterceptors();
    this.fetchUserInfo();
  },
  methods: {
    ...mapActions('user', ['updateAvatar', 'clearUserData']),

    setupInterceptors() {
      // 请求拦截器
      axios.interceptors.request.use(config => {
        const token = localStorage.getItem('Authorization');
        if (token) config.headers['Authorization'] = token;
        return config;
      });

      // 响应拦截器
      axios.interceptors.response.use(
          response => response,
          error => {
            if (error.response?.status === 401) {
              this.handleLogout();
            }
            return Promise.reject(error);
          }
      );
    },

    async fetchUserInfo() {
      try {
        const token = this.getAuthorization();
        const response = await axios.get("http://localhost:8080/user/info", {
          headers: { Authorization: token },
        });

        if (response.data?.username) {
          let avatarUrl = response.data.avatar || this.defaultAvatar;
          if (avatarUrl && !avatarUrl.startsWith("http")) {
            // 处理后端返回的URL格式，确保是完整URL
            if (avatarUrl.startsWith("//")) {
              avatarUrl = "http:" + avatarUrl;
            } else if (!avatarUrl.includes("://")) {
              avatarUrl = "http://localhost:8080" + avatarUrl;
            }
          }
          // 添加时间戳防止缓存
          const fullAvatarUrl = avatarUrl + "?t=" + new Date().getTime();
          this.updateAvatar(fullAvatarUrl);
        } else {
          throw new Error("获取用户信息失败");
        }
      } catch (error) {
        console.error("获取用户信息失败:", error);
        if (error.response?.status === 401) {
          this.handleLogout();
        } else {
          this.$message.error("获取用户信息失败，请检查网络或后端服务是否正常运行");
        }
      }
    },

    beforeAvatarUpload(file) {
      const isJPG = file.type === "image/jpeg";
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isJPG) {
        this.$message.error("只能上传JPG格式图片!");
        return false;
      }
      if (!isLt2M) {
        this.$message.error("图片大小不能超过2MB!");
        return false;
      }
      return true;
    },

    async uploadAvatar({ file }) {
      try {
        this.loading = true;
        const formData = new FormData();
        formData.append("file", file);

        const response = await axios.post(
            "http://localhost:8080/user/upload",
            formData,
            {
              headers: {
                'Content-Type': 'multipart/form-data',
                Authorization: this.getAuthorization(),
              }
            }
        );

        if (response.data?.code === 1) {
          let avatarUrl = response.data.data;
          if (!avatarUrl.startsWith('http')) {
            // 处理OSS返回的URL格式，确保是完整URL
            if (avatarUrl.startsWith('//')) {
              avatarUrl = "http:" + avatarUrl;
            } else if (!avatarUrl.includes("://")) {
              avatarUrl = "http://localhost:8080" + avatarUrl;
            }
          }
          // 添加时间戳防止缓存
          const fullAvatarUrl = avatarUrl + "?t=" + new Date().getTime();
          this.updateAvatar(fullAvatarUrl);
          this.$message.success("头像上传成功");
        } else {
          const errorMsg = response.data?.msg ? response.data.msg : "头像上传失败";
          this.$message.error(errorMsg);
        }
      } catch (error) {
        console.error("上传失败:", error);
        let errorMsg = "头像上传失败，请重试";
        if (error.response) {
          errorMsg = error.response.data.msg || errorMsg;
        }
        this.$message.error(errorMsg);
        if (error.response?.status === 401) {
          this.handleLogout();
        }
      } finally {
        this.loading = false;
      }
    },

    validateRePwd(rule, value, callback) {
      if (value !== this.passwordForm.new_pwd) {
        callback(new Error("两次输入的新密码不一致"));
      } else {
        callback();
      }
    },

    async submitForm() {
      try {
        this.loading = true;
        const valid = await this.$refs.profileForm.validate();
        if (valid) {
          const token = this.getAuthorization();
          const updateData = {
            username: this.profile.username,
            UserUrl: this.profile.UserUrl.split('?')[0], // 移除时间戳
          };

          await axios.put(
              "http://localhost:8080/user/update",
              updateData,
              {
                headers: {
                  Authorization: token,
                },
              }
          );

          this.$message.success("修改成功");
          // 如果需要，可以在这里刷新用户信息
        } else {
          this.$message.error("表单验证失败");
        }
      } catch (error) {
        console.error("修改失败：", error);
        if (error.response?.status === 401) {
          this.$message.error("登录已过期，请重新登录");
          this.$router.push("/login");
        } else {
          const errorMessage =
              `修改失败，状态码: ${error.response?.status}，错误信息: ${
                  error.response?.data?.msg || "未知错误"
              }`;
          this.$message.error(errorMessage);
        }
      } finally {
        this.loading = false;
      }
    },

    showPasswordDialog() {
      this.passwordDialogVisible = true;
    },

    async submitPasswordForm() {
      try {
        this.loading = true;
        const valid = await this.$refs.passwordForm.validate();
        if (valid) {
          const token = this.getAuthorization();
          const headers = {
            Authorization: token,
          };

          const updateData = {
            username: this.profile.username,
            old_pwd: this.passwordForm.old_pwd,
            new_pwd: this.passwordForm.new_pwd,
          };

          const response = await axios.put(
              "http://localhost:8080/user/updatePwd",
              updateData,
              { headers }
          );

          if (response.data.code === 1) {
            this.$message.success("密码修改成功");
            this.passwordDialogVisible = false;
            // 清空表单
            this.passwordForm = {
              old_pwd: "",
              new_pwd: "",
              re_pwd: "",
            };
          } else {
            this.$message.error(response.data.msg || "密码修改失败");
          }
        } else {
          this.$message.error("表单验证失败");
        }
      } catch (error) {
        console.error("密码修改失败：", error);
        if (error.response?.status === 401) {
          this.$message.error("登录已过期，请重新登录");
          this.$router.push("/login");
        } else {
          const errorMessage =
              `密码修改失败，状态码: ${error.response?.status}，错误信息: ${
                  error.response?.data?.msg || "未知错误"
              }`;
          this.$message.error(errorMessage);
        }
      } finally {
        this.loading = false;
      }
    },

    handleLogout() {
      this.clearUserData();
      localStorage.removeItem('Authorization');
      localStorage.removeItem('uid');
      this.$message.success('已退出登录');
      this.$router.push('/login');
    },

    logout() {
      this.$confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.handleLogout();
      });
    },

    getAuthorization() {
      return localStorage.getItem('Authorization');
    }
  },
};
</script>

<style lang="less" scoped>
@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

.home-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-base);
}

/* 顶部 Logo 栏 */
.top-bar {
  background: rgba(13, 17, 23, 0.96);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border-subtle);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 200;
  flex-shrink: 0;
}

/* 导航栏 */
.nav-bar {
  background: rgba(16, 20, 28, 0.98);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--border-subtle);
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 150;
  flex-shrink: 0;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 5%;
    width: 90%;
    height: 1px;
    background: linear-gradient(90deg, transparent, var(--accent-primary), transparent);
    opacity: 0.35;
  }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo-icon {
  font-size: 26px;
  color: var(--accent-primary);
  filter: drop-shadow(0 0 8px rgba(0, 212, 255, 0.6));
  line-height: 1;
}

.header-title {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.title-main {
  font-size: 17px;
  font-weight: 700;
  background: var(--accent-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
  line-height: 1.2;
}

.title-sub {
  font-size: 10px;
  color: var(--text-muted);
  letter-spacing: 2px;
  font-weight: 400;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  position: relative;
  cursor: pointer;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--accent-primary);
  box-shadow: 0 0 10px rgba(0, 212, 255, 0.35);
  display: block;
}

.online-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 9px;
  height: 9px;
  background: #22c55e;
  border-radius: 50%;
  border: 2px solid var(--bg-base);
  box-shadow: 0 0 6px rgba(34, 197, 94, 0.6);
  animation: pulse-dot 2s ease-in-out infinite;
}

.logout-btn {
  background: transparent !important;
  border: 1px solid var(--border-default) !important;
  color: var(--text-secondary) !important;
  border-radius: var(--radius-sm) !important;
  font-size: 13px !important;
  height: 32px !important;
  padding: 0 12px !important;
  transition: all 0.3s ease !important;

  &:hover {
    background: rgba(239, 68, 68, 0.1) !important;
    border-color: rgba(239, 68, 68, 0.4) !important;
    color: #f87171 !important;
  }
}

.el-main {
  background-color: var(--bg-base);
  padding: 20px 24px;
  overflow-y: auto;
  flex: 1;
  display: flex;
  flex-direction: column;

  > .container,
  > div {
    flex: 1;
    width: 100%;
  }
}
</style>

