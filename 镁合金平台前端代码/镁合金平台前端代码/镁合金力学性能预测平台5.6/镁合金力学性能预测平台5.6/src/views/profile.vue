<template>
  <div class="profile-container">
    <!-- 显示加载遮罩层 -->
    <div v-if="loading" class="loading-mask">
      <el-spinner></el-spinner>
    </div>

    <el-row>
      <el-col :span="24">
        <el-menu mode="horizontal" router>
          <el-menu-item index="/main">返回主页面</el-menu-item>
        </el-menu>
      </el-col>
    </el-row>

    <el-row type="flex" justify="center">
      <el-col :span="20">
        <el-form
            ref="profileForm"
            :model="profile"
            :rules="rules"
            label-width="120px"
            class="profile-form"
        >
          <!-- 头像上传 -->
          <el-form-item label="头像">
            <el-upload
                class="avatar-uploader"
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="uploadAvatar"
                :headers="{ Authorization: getAuthorization() }"
            >
              <img :src="displayedAvatar" class="avatar" />
            </el-upload>
          </el-form-item>

          <!-- 用户名 -->
          <el-form-item label="用户名" prop="username" class="profile-username-item">
            <el-input
                v-model="profile.username"
                placeholder="请输入用户名"
            ></el-input>
          </el-form-item>

          <!-- 修改密码按钮 -->
          <el-form-item class="profile-password-item">
            <el-button type="primary" @click="showPasswordDialog">修改密码</el-button>
          </el-form-item>

          <!-- 提交按钮 -->
          <el-form-item class="profile-submit-item">
            <el-button type="primary" @click="submitForm">保存修改</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <!-- 修改密码对话框 -->
    <el-dialog
        title="修改密码"
        :visible.sync="passwordDialogVisible"
        width="30%"
    >
      <el-form
          :model="passwordForm"
          :rules="passwordRules"
          ref="passwordForm"
          label-width="80px"
      >
        <el-form-item label="绑定邮箱" prop="email">
          <el-input
              v-model="passwordForm.email"
              placeholder="请输入绑定的邮箱"
          ></el-input>
        </el-form-item>
        <el-form-item label="绑定手机" prop="phone">
          <el-input
              v-model="passwordForm.phone"
              placeholder="请输入绑定的手机号"
          ></el-input>
        </el-form-item>
        <el-form-item label="旧密码" prop="old_pwd">
          <el-input
              v-model="passwordForm.old_pwd"
              type="password"
              placeholder="请输入旧密码"
              show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="new_pwd">
          <el-input
              v-model="passwordForm.new_pwd"
              type="password"
              placeholder="请输入新密码"
              show-password
          ></el-input>
        </el-form-item>
        <el-form-item label="确认新密码" prop="re_pwd">
          <el-input
              v-model="passwordForm.re_pwd"
              type="password"
              placeholder="请确认新密码"
              show-password
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="passwordDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitPasswordForm">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 管理员模块：用户管理 -->
    <div v-if="isAdmin" class="admin-management-section">
      <div class="section-header">
        <i class="el-icon-setting"></i>
        <span>平台用户管理</span>
        <el-tag type="danger" size="mini" effect="dark" style="margin-left: 10px;">管理员权限</el-tag>
      </div>

      <el-table
          :data="userList"
          v-loading="userLoading"
          class="user-table"
          :header-cell-style="{ background: '#21262d', color: '#00d4ff' }"
      >
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column label="头像" width="80" align="center">
          <template slot-scope="scope">
            <el-avatar :size="32" :src="scope.row.userUrl || defaultAvatar"></el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="role" label="角色" width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.role === 1 ? 'danger' : 'info'" size="mini">
              {{ scope.row.role === 1 ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template slot-scope="scope">
            <el-popconfirm
                title="确定要删除此用户吗？此操作不可恢复！"
                @confirm="deleteUser(scope.row.id)"
            >
              <el-button slot="reference" type="text" class="delete-btn" :disabled="scope.row.id === currentUid">删除</el-button>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import { mapState, mapActions } from "vuex";

export default {
  name: "Profile",
  components: {
    // 如果有需要引入的其他组件，可以在这里添加
  },
  data() {
    return {
      profile: {
        username: "",
        email: "",
        phone: "",
        UserUrl: null, // 使用 UserUrl 而不是 avatar
      },
      loading: false,
      passwordDialogVisible: false,
      passwordForm: {
        email: "",
        phone: "",
        old_pwd: "",
        new_pwd: "",
        re_pwd: "",
      },
      rules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
        ],
      },
      passwordRules: {
        email: [
          { required: true, message: "请输入绑定的邮箱", trigger: "blur" },
          { type: "email", message: "请输入正确的邮箱格式", trigger: "blur" },
        ],
        phone: [
          { required: true, message: "请输入绑定的手机号", trigger: "blur" },
          { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确", trigger: "blur" },
        ],
        old_pwd: [
          { required: true, message: "请输入旧密码", trigger: "blur" },
        ],
        new_pwd: [
          { required: true, message: "请输入新密码", trigger: "blur" },
          { min: 6, message: "新密码长度至少为6位", trigger: "blur" },
        ],
        re_pwd: [
          { required: true, message: "请确认新密码", trigger: "blur" },
          {
            validator: this.validateRePwd,
            trigger: "blur",
          },
        ],
      },
      isAdmin: localStorage.getItem('role') === '1',
      currentUid: localStorage.getItem('uid'),
      userList: [],
      userLoading: false,
      defaultAvatar:
          "https://tyut123.oss-cn-hangzhou.aliyuncs.com/logo.jpeg?Expires=1745502590&OSSAccessKeyId=TMP.3Kp6KhxjnA6YZqhy4qB7dd5dnGS7TPLmsnRGdTRgi48nSDpXfmqoxYCwuxsMPkxNwsqnzRLHR8qMVBC9HVEAzynQ6CMxGt&Signature=aNSxVy7%2Bobw2qVa5qHy221GnKaI%3D",
    };
  },
  computed: {
    ...mapState("user", ["avatar"]), // 从Vuex获取头像
    displayedAvatar() {
      // 优先显示本地上传的头像，其次Vuex中的头像，最后默认头像
      return this.profile.UserUrl || this.avatar || this.defaultAvatar;
    },
  },
  created() {
    this.initializeProfile();
  },
  methods: {
    ...mapActions("user", ["updateAvatar"]),


    async initializeProfile() {
      this.loading = true;
      try {
        await this.checkLoginStatus();
        await this.fetchUserInfo();
        if (this.isAdmin) {
          await this.fetchUserList();
        }
      } catch (error) {
        console.error("初始化失败:", error);
      } finally {
        this.loading = false;
      }
    },

    getAuthorization() {
      const token = localStorage.getItem("Authorization");
      console.log("当前token:", token);
      return token;
    },

    checkLoginStatus() {
      return new Promise((resolve, reject) => {
        const token = localStorage.getItem("Authorization");
        if (!token) {
          this.$message.error("未登录，请先登录！");
          this.$router.push("/login");
          reject(new Error("未登录"));
        } else {
          resolve();
        }
      });
    },

    async fetchUserInfo() {
      try {
        const token = this.getAuthorization();
        const response = await axios.get("http://localhost:8080/user/info", {
          headers: { Authorization: token },
        });

        if (response.data && response.data.username) {
          let avatarUrl = response.data.UserUrl || this.defaultAvatar; // 使用 UserUrl
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

          // 更新组件中的头像
          this.profile.UserUrl = fullAvatarUrl;

          // 更新Vuex中的头像
          this.updateAvatar(fullAvatarUrl);

          this.profile.username = response.data.username;
          this.profile.email = response.data.email || "";
          this.profile.phone = response.data.phone || "";
          // 补存 uid 和 role，确保管理员功能和登录态检测正常
          if (response.data.uid) localStorage.setItem('uid', response.data.uid);
          if (response.data.role !== undefined) localStorage.setItem('role', response.data.role);
          this.$message.success("更新成功");
        } else {
          throw new Error("获取用户信息失败");
        }
      } catch (error) {
        console.error("获取用户信息失败:", error);
        this.profile.UserUrl = this.defaultAvatar;
        // 不强制跳转登录，仅提示用户，避免 token 短暂失效导致意外退出
        this.$message.warning("获取用户信息失败，部分数据可能无法显示");
      }
    },

    beforeAvatarUpload(file) {
      const isJPG = file.type === "image/jpeg";
      const isLt2M = file.size / 1024 / 1024 < 2;

      if (!isJPG) {
        this.$message.error("上传头像图片只能是 JPG 格式!");
        return false;
      }
      if (!isLt2M) {
        this.$message.error("上传头像图片大小不能超过 2MB!");
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
                "Content-Type": "multipart/form-data",
                Authorization: this.getAuthorization(),
              },
            }
        );

        if (response.data && response.data.code === 1) {
          let avatarUrl = response.data.data;
          if (!avatarUrl.startsWith("http")) {
            // 处理后端返回的URL格式，确保是完整URL
            if (avatarUrl.startsWith("//")) {
              avatarUrl = "http:" + avatarUrl;
            } else if (!avatarUrl.includes("://")) {
              avatarUrl = "http://localhost:8080" + avatarUrl;
            }
          }

          // 添加时间戳防止缓存
          const fullAvatarUrl = avatarUrl + "?t=" + new Date().getTime();

          // 更新组件中的头像
          this.profile.UserUrl = fullAvatarUrl;

          // 更新Vuex中的头像
          this.updateAvatar(fullAvatarUrl);

          this.$message.success("头像上传成功");
        } else {
          const errorMsg = response.data && response.data.msg ? response.data.msg : "头像上传失败";
          this.$message.error(errorMsg);
        }
      } catch (error) {
        console.error("头像上传失败：", error);
        let errorMsg = "头像上传失败，请重试";
        if (error.response) {
          errorMsg = error.response.data.msg || errorMsg;
        }
        this.$message.error(errorMsg);

        if (error.response?.status === 401) {
          this.$router.push("/login");
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
            UserUrl: this.profile.UserUrl.split("?")[0], // 移除时间戳
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

          this.$message.success("更新成功");
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

          // 验证绑定的邮箱和手机号
          if (this.passwordForm.email !== this.profile.email || this.passwordForm.phone !== this.profile.phone) {
            this.$message.error("邮箱或手机号验证不通过，请核对后重试");
            return;
          }

          // 检查新密码是否与旧密码相同
          if (this.passwordForm.new_pwd === this.passwordForm.old_pwd) {
            this.$message.error("密码不能和旧密码相同，请重新输入");
            return;
          }

          const response = await axios.put(
              "http://localhost:8080/user/updatePwd",
              updateData,
              { headers }
          );

          if (response.data.code === 1) {
            this.$message.success("更新成功");
            this.passwordDialogVisible = false;
            // 清空表单
            this.passwordForm = {
              email: "",
              phone: "",
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
      localStorage.removeItem("uid");
      localStorage.removeItem("Authorization");
      this.$message.success("已退出登录");
      this.$router.push("/login");
    },

    // 管理员方法
    async fetchUserList() {
      this.userLoading = true;
      try {
        const response = await axios.get("http://localhost:8080/admin/users", {
          headers: { Authorization: this.getAuthorization() }
        });
        if (response.data.code === 1) {
          this.userList = response.data.data;
        } else {
          this.$message.error(response.data.msg || "获取用户列表失败");
        }
      } catch (error) {
        console.error("获取用户列表失败:", error);
      } finally {
        this.userLoading = false;
      }
    },

    async deleteUser(uid) {
      try {
        const response = await axios.delete(`http://localhost:8080/admin/user/${uid}`, {
          headers: { Authorization: this.getAuthorization() }
        });
        if (response.data.code === 1) {
          this.$message.success("更新成功");
          this.fetchUserList(); // 刷新列表
        } else {
          this.$message.error(response.data.msg || "删除失败");
        }
      } catch (error) {
        this.$message.error("删除用户请求失败");
      }
    },

    getAuthorization() {
      return localStorage.getItem("Authorization");
    },
  },
};
</script>

<style lang="less" scoped>
.profile-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: var(--bg-base);
  padding: 24px;
}

/* 顶部导航栏 */
.el-menu {
  background-color: var(--bg-surface) !important;
  border-bottom: 1px solid var(--border-subtle) !important;

  /deep/ .el-menu-item {
    color: var(--text-secondary) !important;
    &:hover, &.is-active {
      color: var(--accent-primary) !important;
      background-color: rgba(0, 212, 255, 0.08) !important;
    }
  }
}

/* 表单卡片 */
.profile-form {
  background-color: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  padding: 32px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  max-width: 700px;
  margin: 24px auto 0 auto;
  position: relative;

  /* 顶部高光线 */
  &::before {
    content: '';
    position: absolute;
    top: 0; left: 10%; width: 80%; height: 1px;
    background: linear-gradient(90deg, transparent, var(--accent-primary), transparent);
    opacity: 0.4;
    border-radius: var(--radius-md);
  }
}

/* 头像上传区域 */
.avatar-uploader /deep/ .el-upload {
  border: 2px dashed var(--border-default);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;

  &:hover {
    border-color: var(--accent-primary);
    box-shadow: 0 0 15px rgba(0, 212, 255, 0.3);
  }
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--accent-primary);
  box-shadow: 0 0 16px rgba(0, 212, 255, 0.35);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 0 24px rgba(0, 212, 255, 0.55);
    transform: scale(1.03);
  }
}

/* 表单项 */
.el-form-item {
  margin-bottom: 20px;
}

/* 收窄用户名输入框 */
.profile-username-item /deep/ .el-input {
  max-width: 300px;
}

/* 按钮不要撑满 */
.el-button {
  margin-top: 4px;
  width: auto !important;
}

/* 修改密码/保存按钮 */
.profile-password-item .el-button,
.profile-submit-item .el-button {
  width: 96px !important;
  padding: 8px 16px;
  font-size: 13px;
}

/* dialog 覆盖 */
.el-dialog {
  border-radius: var(--radius-lg) !important;
}

/deep/ .el-dialog__header {
  background-color: transparent !important;
  border-bottom: 1px solid var(--border-subtle) !important;
}

/* 列布局 */
.el-col-20 {
  padding: 0;
  background-color: transparent !important;
  border-left: none !important;
}

/* 管理员板块样式 */
.admin-management-section {
  margin: 40px auto 0 auto;
  background-color: var(--bg-elevated);
  border: 1px solid var(--border-subtle);
  padding: 32px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  max-width: 1000px;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  gap: 10px;

  i {
    font-size: 20px;
    color: var(--accent-primary);
  }

  span {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-primary);
  }
}

.user-table {
  width: 100%;
  background: transparent !important;

  /deep/ .el-table__body tr:hover > td.el-table__cell {
    background: rgba(0, 212, 255, 0.04) !important;
  }
  /deep/ .el-table tr,
  /deep/ .el-table td.el-table__cell {
    background: transparent !important;
    color: var(--text-primary) !important;
    border-bottom: 1px solid rgba(255, 255, 255, 0.04) !important;
  }
}

.delete-btn {
  color: #F56C6C !important;
  &:hover {
    color: #ff4d4d !important;
    text-shadow: 0 0 8px rgba(245, 108, 108, 0.4);
  }
  &:disabled {
    color: var(--text-muted) !important;
    cursor: not-allowed;
  }
}
</style>

