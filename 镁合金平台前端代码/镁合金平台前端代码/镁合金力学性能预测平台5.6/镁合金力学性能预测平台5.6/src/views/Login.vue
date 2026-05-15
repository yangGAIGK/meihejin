<template>
  <div class="login">
    <!-- 登录页面 -->
    <div class="login-wrap">
      <div class="login-title">
        <div class="title-icon">⬡</div>
        <h3>镁合金智能分析平台</h3>
        <p>MAGNESIUM ALLOY ANALYSIS PLATFORM</p>
      </div>
      <el-form ref="loginForm" :model="user" :rules="rules" status-icon label-width="70px">
        <el-form-item prop="username" label="用户名">
          <el-input v-model="user.username" placeholder="请输入用户名" prefix-icon="el-icon-user"></el-input>
        </el-form-item>
        <el-form-item id="password" prop="password" label="密码">
          <el-input v-model="user.password" show-password placeholder="请输入密码" prefix-icon="el-icon-lock"></el-input>
        </el-form-item>
        <div class="button-group">
          <el-button type="text" @click="dialogVisible = true">注册账号</el-button>
          <el-button type="text" @click="handleForgetPassword = true">忘记密码</el-button>
        </div>
        <el-button class="login-btn" @click="doLogin">登 录</el-button>
      </el-form>
    </div>

    <!-- 注册窗口 -->
    <el-dialog title="注册账号" :visible.sync="dialogVisible" width="40%">
      <el-form :model="resForm" status-icon :rules="rules" ref="resForm" label-width="100px" class="register-form">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="resForm.username"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="resForm.email"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input type="password" v-model="resForm.password" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="checkPass">
          <el-input type="password" v-model="resForm.checkPass" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="resetForm()" style="width: 80px">取消</el-button>
        <el-button type="primary" @click="register" style="width: 80px">确定</el-button>
      </span>
    </el-dialog>

    <!-- 忘记密码 -->
    <el-dialog title="密码修改" :visible.sync="handleForgetPassword" width="40%">
      <div v-if="step === 1">
        <el-form :model="verifyForm" status-icon :rules="verifyRules" ref="verifyForm" label-width="100px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="verifyForm.username"></el-input>
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="verifyForm.email"></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="handleForgetPassword = false" style="width: 80px">取消</el-button>
          <el-button type="primary" @click="verifyUser" style="width: 80px">下一步</el-button>
        </span>
      </div>

      <div v-if="step === 2">
        <el-form :model="updateForm" status-icon :rules="updateRules" ref="updateForm" label-width="100px">
          <el-form-item label="新密码" prop="new_pwd">
            <el-input type="password" v-model="updateForm.new_pwd" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item label="确认新密码" prop="re_pwd">
            <el-input type="password" v-model="updateForm.re_pwd" autocomplete="off"></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="step = 1" style="width: 80px">返回</el-button>
          <el-button type="primary" @click="submitUpdate" style="width: 80px">确定</el-button>
        </span>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from "axios";
const adminCredentials = { username: 'admin', password: '123456' };
const localAccount = { username: 'tyut', password: '123456', uid: 'tyut_user_001' };

export default {
  name: "login",
  data() {
    var validateEmail = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请输入邮箱"));
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
        callback(new Error("邮箱格式不正确"));
      } else {
        callback();
      }
    };
    const validateUsername = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请输入用户名"));
      } else if (!/^[a-zA-Z0-9_]{3,20}$/.test(value)) {
        callback(new Error("用户名格式不正确（3-20位，字母、数字或下划线）"));
      } else {
        callback();
      }
    };
    const validateNewPwd = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请输入新密码"));
      } else if (value.length < 6) {
        callback(new Error("密码不少于6位"));
      } else {
        callback();
      }
    };
    const validateRePwd = (rule, value, callback) => {
      if (!value) {
        callback(new Error("请再次输入新密码"));
      } else if (value !== this.updateForm.new_pwd) {
        callback(new Error("两次输入密码不一致"));
      } else {
        callback();
      }
    };
    var validatePass = (rule, value, callback) => {
      if (value === "") {
        callback(new Error("请输入密码"));
      } else if (value.length < 6) {
        callback(new Error("密码不少于6位"));
      } else if (this.resForm.checkPass !== "") {
        this.$refs.resForm.validateField("checkPass");
        callback();
      }
    };
    var validatePass2 = (rule, value, callback) => {
      if (value === "") {
        callback(new Error("请再次输入密码"));
      } else if (value !== this.resForm.password) {
        callback(new Error("两次输入密码不一致!"));
      } else {
        this.isSame = true;
        callback();
      }
    };

    return {
      user: { username: "", password: "" },
      dialogVisible: false,
      handleForgetPassword: false,
      step: 1,
      verifyForm: {
        username: "",
        email: ""
      },
      verifyRules: {
        username: [{ validator: validateUsername, trigger: "blur" }],
        email: [{ validator: validateEmail, trigger: "blur" }]
      },
      updateForm: {
        new_pwd: "",
        re_pwd: ""
      },
      resForm: { username: "", password: "", checkPass: "", email: "" },
      isSame: false,
      rules: {
        username: [{ validator: validateUsername, trigger: "blur" }],
        email: [{ validator: validateEmail, trigger: "blur" }],
        new_pwd: [{ validator: validateNewPwd, trigger: "blur" }],
        re_pwd: [{ validator: validateRePwd, trigger: "blur" }],
        password: [{ validator: validatePass, trigger: "blur" }],
        checkPass: [{ validator: validatePass2, trigger: "blur" }]
      }
    };
  },
  methods: {
    doLogin() {
      if (this.user.username === "" && this.user.password === "") {
        this.$message.warning("请先输入用户名以及密码");
      } else if (this.user.username === "") {
        this.$message.warning("请输入用户名");
      } else if (this.user.password === "") {
        this.$message.warning("请输入密码");
      } else if (
          this.user.username === adminCredentials.username &&
          this.user.password === adminCredentials.password
      ) {
        this.$message.success("登录成功");
        localStorage.setItem("uid", "admin_user_001");
        setTimeout(() => {
          this.$router.push({ name: 'main' });
        }, 1000);
      } else if (
          this.user.username === localAccount.username &&
          this.user.password === localAccount.password
      ) {
        this.$message.success("登录成功");
        localStorage.setItem("uid", localAccount.uid);
        setTimeout(() => {
          this.$router.push({ name: 'main' });
        }, 1000);
      } else {
        axios({
          method: "POST",
          url: "http://localhost:8080/user/login",
          data: {
            username: this.user.username,
            password: this.user.password
          }
        })
            .then((res) => {
              if (res.data.code == 1) {
                const token = res.data.data;
                if (token) {
                  window.localStorage.setItem("Authorization", token);
                  localStorage.setItem("uid", res.data.uid);
                  const loading = this.$loading({
                    lock: true,
                    text: "正在登录...",
                    spinner: "el-icon-loading",
                    background: "rgba(0, 0, 0, 0.7)"
                  });

                  setTimeout(() => {
                    this.$router.push("/data1");
                    loading.close();
                  }, 500);
                } else {
                  this.$message.error("没有返回 token");
                }
              } else {
                this.$message.error("用户名或密码错误");
              }
            })
            .catch((error) => {
              this.$message.error("登录失败：" + error.message);
            });
      }
    },
    verifyUser() {
      this.$refs.verifyForm.validate((valid) => {
        if (valid) {
          this.$loading({
            lock: true,
            text: "验证中...",
            spinner: "el-icon-loading",
            background: "rgba(0, 0, 0, 0.7)"
          });

          axios({
            method: "POST",
            url: "http://localhost:8080/user/verifyUser",
            data: {
              username: this.verifyForm.username,
              email: this.verifyForm.email
            }
          })
              .then((res) => {
                if (res.data.code === 1) {
                  this.$message.success("验证成功，请修改密码");
                  this.step = 2;
                } else {
                  this.$message.error("用户名或邮箱错误");
                }
                this.$loading().close();
              })
              .catch((err) => {
                this.$message.error("验证失败：" + err.message);
                this.$loading().close();
              });
        } else {
          this.$message.error("请填写正确的用户名和邮箱");
        }
      });
    },
    submitUpdate() {
      this.$refs.updateForm.validate((valid) => {
        if (valid) {
          this.$loading({
            lock: true,
            text: "修改中...",
            spinner: "el-icon-loading",
            background: "rgba(0, 0, 0, 0.7)"
          });

          axios({
            method: "PUT",
            url: "http://localhost:8080/user/forgetPwd",
            data: {
              username: this.verifyForm.username,
              password: this.updateForm.new_pwd
            }
          })
              .then((res) => {
                if (res.data.code === 1) {
                  this.$message.success("密码修改成功");
                  this.handleForgetPassword = false;
                } else {
                  this.$message.error("密码修改失败");
                }
                this.$loading().close();
              })
              .catch((err) => {
                this.$message.error("修改失败：" + err.message);
                this.$loading().close();
              });
        } else {
          this.$message.error("请确认新密码和确认密码一致");
        }
      });
    },
    register() {
      // 固定的 userUrl
      const fixedUserUrl = "https://tyut123.oss-cn-hangzhou.aliyuncs.com/logo.jpeg?Expires=1743339550&OSSAccessKeyId=TMP.3KsNfcJsbkX3AWtXxteYraZK5Eo82LqqfUjfeVSpLPzdsxFWgKWVkkpMMwG3q3ftCK8pWPkesojCQZQMoSAfGKP4pnZnTq&Signature=FIdlHqFDXC%2BAxhwz2s2mM1AAz%2F0%3D";

      this.$refs.resForm.validate((valid) => {
        if (valid) {
          axios({
            method: "POST",
            url: "http://localhost:8080/user/registerservlet",  // 这里是注册接口
            data: {
              username: this.resForm.username,
              password: this.resForm.password,
              email: this.resForm.email,
              userUrl: fixedUserUrl  // 加入固定的 userUrl
            }
          })
              .then((res) => {
                if (res.data.code == 1) {
                  this.$message.success("注册成功");
                  this.resetForm();
                  this.dialogVisible = false;
                } else {
                  this.$message.warning("用户已注册");
                }
              })
              .catch((err) => {
                this.$message.error("注册失败：" + err.message);
              });
        } else {
          this.$message.error("表单填写有误");
        }
      });
    },
    resetForm() {
      this.dialogVisible = false;
      this.handleForgetPassword = false;
      this.$nextTick(() => {
        this.$refs.resForm.resetFields();
        this.$refs.verifyForm.resetFields();
        this.$refs.updateForm.resetFields();
      });
    }
  }
};
</script>

<style lang="less" scoped>
@keyframes float-orb {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

@keyframes scan-line {
  0% { transform: translateY(-100%); opacity: 0; }
  50% { opacity: 0.6; }
  100% { transform: translateY(100vh); opacity: 0; }
}

@keyframes fadeSlideUp {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}

.login {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100vh;
  background: var(--bg-base);
  position: relative;
  overflow: hidden;

  // 动态背景光球
  &::before {
    content: '';
    position: absolute;
    width: 600px;
    height: 600px;
    background: radial-gradient(circle, rgba(0, 212, 255, 0.12) 0%, transparent 70%);
    top: -150px;
    left: -150px;
    border-radius: 50%;
    animation: float-orb 8s ease-in-out infinite;
    pointer-events: none;
  }

  &::after {
    content: '';
    position: absolute;
    width: 500px;
    height: 500px;
    background: radial-gradient(circle, rgba(124, 58, 237, 0.1) 0%, transparent 70%);
    bottom: -100px;
    right: -100px;
    border-radius: 50%;
    animation: float-orb 10s ease-in-out infinite reverse;
    pointer-events: none;
  }
}

// 网格背景
.login::before {
  background-image:
    radial-gradient(circle, rgba(0, 212, 255, 0.12) 0%, transparent 70%),
    linear-gradient(rgba(0,212,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,212,255,0.03) 1px, transparent 1px);
  background-size: 600px 600px, 50px 50px, 50px 50px;
}

.login-wrap {
  position: relative;
  z-index: 10;
  width: 420px;
  padding: 48px 40px;
  border-radius: var(--radius-lg);
  background: rgba(22, 27, 34, 0.85);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid var(--border-subtle);
  box-shadow:
    0 8px 60px rgba(0, 0, 0, 0.6),
    0 0 0 1px rgba(0, 212, 255, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.05);
  animation: fadeSlideUp 0.6s ease-out;

  // 顶部高光线
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 10%;
    width: 80%;
    height: 1px;
    background: linear-gradient(90deg, transparent, var(--accent-primary), transparent);
    opacity: 0.6;
  }
}

.login-title {
  text-align: center;
  margin-bottom: 36px;

  .title-icon {
    font-size: 32px;
    margin-bottom: 12px;
  }

  h3 {
    font-size: 26px;
    font-weight: 700;
    background: var(--accent-gradient);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    letter-spacing: 1px;
    margin-bottom: 6px;
  }

  p {
    color: var(--text-muted);
    font-size: 13px;
    letter-spacing: 0.5px;
  }
}

/deep/ .el-form-item__label {
  color: var(--text-secondary) !important;
  font-size: 13px !important;
  font-weight: 500 !important;
}

/deep/ .el-input__inner {
  height: 44px !important;
  background-color: rgba(33, 38, 45, 0.8) !important;
  border: 1px solid rgba(0, 212, 255, 0.15) !important;
  color: var(--text-primary) !important;
  border-radius: 8px !important;
  font-size: 14px !important;
  transition: all 0.3s ease !important;

  &::placeholder {
    color: var(--text-muted) !important;
  }

  &:focus {
    border-color: var(--accent-primary) !important;
    box-shadow: 0 0 0 3px rgba(0, 212, 255, 0.12) !important;
    background-color: rgba(0, 212, 255, 0.04) !important;
  }
}

/deep/ .el-input__prefix, /deep/ .el-input__suffix {
  color: var(--text-muted) !important;
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  background: var(--accent-gradient) !important;
  border: none !important;
  border-radius: 8px !important;
  color: #fff !important;
  margin-top: 8px;
  transition: all 0.3s ease !important;
  box-shadow: 0 4px 20px rgba(0, 212, 255, 0.3) !important;
  cursor: pointer;

  &:hover {
    transform: translateY(-2px) !important;
    box-shadow: 0 8px 30px rgba(0, 212, 255, 0.5) !important;
  }

  &:active {
    transform: translateY(0) !important;
  }
}

.button-group {
  display: flex;
  justify-content: space-between;
  margin-top: 4px;
  margin-bottom: 8px;
}

/deep/ .el-button--text {
  color: var(--text-muted) !important;
  font-size: 13px !important;
  padding: 0 !important;
  transition: color 0.2s !important;

  &:hover {
    color: var(--accent-primary) !important;
  }
}

.divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 20px 0;
  color: var(--text-muted);
  font-size: 12px;

  &::before, &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: var(--border-subtle);
  }
}
</style>
