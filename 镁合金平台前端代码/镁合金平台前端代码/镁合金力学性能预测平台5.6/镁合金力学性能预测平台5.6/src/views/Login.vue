<template>
  <div class="login-root">
    <!-- 背景装饰 -->
    <div class="bg-orb orb1"></div>
    <div class="bg-orb orb2"></div>
    <div class="grid-overlay"></div>

    <!-- 入口选择页 -->
    <transition name="fade-slide">
      <div v-if="mode === 'select'" class="login-wrap" key="select">
        <div class="login-title">
          <div class="title-icon">⬡</div>
          <h3>镁合金智能分析平台</h3>
          <p>MAGNESIUM ALLOY ANALYSIS PLATFORM</p>
        </div>
        <div class="role-cards">
          <div class="role-card" @click="mode = 'user'">
            <i class="el-icon-user-solid card-icon"></i>
            <span class="card-label">用户登录</span>
            <span class="card-desc">普通用户入口</span>
          </div>
          <div class="role-card admin-card" @click="mode = 'admin'">
            <i class="el-icon-s-custom card-icon"></i>
            <span class="card-label">管理员登录</span>
            <span class="card-desc">系统管理员入口</span>
          </div>
        </div>
      </div>
    </transition>

    <!-- 用户登录 -->
    <transition name="fade-slide">
      <div v-if="mode === 'user'" class="login-wrap" key="user">
        <div class="back-btn" @click="mode = 'select'">
          <i class="el-icon-arrow-left"></i> 返回
        </div>
        <div class="login-title">
          <div class="title-icon">⬡</div>
          <h3>用户登录</h3>
          <p>MAGNESIUM ALLOY ANALYSIS PLATFORM</p>
        </div>
        <el-form ref="loginForm" :model="user" :rules="loginRules" status-icon label-width="70px">
          <el-form-item prop="username" label="用户名">
            <el-input v-model="user.username" placeholder="请输入用户名" prefix-icon="el-icon-user"></el-input>
          </el-form-item>
          <el-form-item prop="password" label="密码">
            <el-input v-model="user.password" show-password placeholder="请输入密码" prefix-icon="el-icon-lock"></el-input>
          </el-form-item>
          <div class="button-group">
            <el-button type="text" @click="mode = 'register'">注册账号</el-button>
            <el-button type="text" @click="mode = 'forgot'">忘记密码</el-button>
          </div>
          <el-button class="login-btn" @click="doLogin" :loading="loginLoading">登 录</el-button>
        </el-form>
      </div>
    </transition>

    <!-- 管理员登录 -->
    <transition name="fade-slide">
      <div v-if="mode === 'admin'" class="login-wrap admin-wrap" key="admin">
        <div class="back-btn" @click="mode = 'select'">
          <i class="el-icon-arrow-left"></i> 返回
        </div>
        <div class="login-title">
          <div class="title-icon admin-icon">⬡</div>
          <h3 class="admin-title">管理员登录</h3>
          <p>ADMINISTRATOR ENTRANCE</p>
        </div>
        <el-form ref="adminForm" :model="adminUser" :rules="loginRules" status-icon label-width="70px">
          <el-form-item prop="username" label="账号">
            <el-input v-model="adminUser.username" placeholder="管理员账号" prefix-icon="el-icon-s-custom"></el-input>
          </el-form-item>
          <el-form-item prop="password" label="密码">
            <el-input v-model="adminUser.password" show-password placeholder="管理员密码" prefix-icon="el-icon-lock"></el-input>
          </el-form-item>
          <el-button class="login-btn admin-btn" @click="doAdminLogin" :loading="loginLoading">登 录</el-button>
        </el-form>
      </div>
    </transition>

    <!-- 注册 -->
    <transition name="fade-slide">
      <div v-if="mode === 'register'" class="login-wrap wide-wrap" key="register">
        <div class="back-btn" @click="mode = 'user'">
          <i class="el-icon-arrow-left"></i> 返回登录
        </div>
        <div class="login-title">
          <div class="title-icon">⬡</div>
          <h3>注册账号</h3>
          <p>CREATE YOUR ACCOUNT</p>
        </div>
        <el-form ref="regForm" :model="regForm" :rules="regRules" status-icon label-width="90px">
          <el-form-item prop="username" label="用户名">
            <el-input v-model="regForm.username" placeholder="3-20位，字母/数字/下划线" prefix-icon="el-icon-user"></el-input>
          </el-form-item>
          <el-form-item prop="phone" label="手机号">
            <el-input v-model="regForm.phone" placeholder="11位手机号（唯一绑定）" prefix-icon="el-icon-mobile-phone"></el-input>
          </el-form-item>
          <el-form-item prop="email" label="邮箱">
            <el-input v-model="regForm.email" placeholder="有效邮箱（唯一绑定）" prefix-icon="el-icon-message"></el-input>
          </el-form-item>
          <el-form-item prop="password" label="密码">
            <el-input v-model="regForm.password" show-password placeholder="8-16位，含数字+小写字母+特殊字符或大写" prefix-icon="el-icon-lock"></el-input>
          </el-form-item>
          <el-form-item prop="checkPass" label="确认密码">
            <el-input v-model="regForm.checkPass" show-password placeholder="再次输入密码" prefix-icon="el-icon-lock"></el-input>
          </el-form-item>
          <el-button class="login-btn" @click="doRegister" :loading="regLoading">注 册</el-button>
        </el-form>
      </div>
    </transition>

    <!-- 忘记密码 -->
    <transition name="fade-slide">
      <div v-if="mode === 'forgot'" class="login-wrap wide-wrap" key="forgot">
        <div class="back-btn" @click="mode = 'user'">
          <i class="el-icon-arrow-left"></i> 返回登录
        </div>
        <div class="login-title">
          <div class="title-icon">⬡</div>
          <h3>{{ forgotStep === 1 ? '身份验证' : '重置密码' }}</h3>
          <p>RESET YOUR PASSWORD</p>
        </div>

        <!-- Step 1: 验证邮箱+手机 -->
        <el-form v-if="forgotStep === 1" ref="verifyForm" :model="verifyForm" :rules="verifyRules" status-icon label-width="80px">
          <el-form-item prop="email" label="邮箱">
            <el-input v-model="verifyForm.email" placeholder="注册时绑定的邮箱" prefix-icon="el-icon-message"></el-input>
          </el-form-item>
          <el-form-item prop="phone" label="手机号">
            <el-input v-model="verifyForm.phone" placeholder="注册时绑定的手机号" prefix-icon="el-icon-mobile-phone"></el-input>
          </el-form-item>
          <el-button class="login-btn" @click="doVerify" :loading="verifyLoading">验证身份</el-button>
        </el-form>

        <!-- Step 2: 重置密码 -->
        <el-form v-if="forgotStep === 2" ref="resetForm" :model="resetForm" :rules="resetRules" status-icon label-width="90px">
          <el-form-item prop="newPwd" label="新密码">
            <el-input v-model="resetForm.newPwd" show-password placeholder="8-16位，含数字+小写字母+特殊字符或大写" prefix-icon="el-icon-lock"></el-input>
          </el-form-item>
          <el-form-item prop="rePwd" label="确认新密码">
            <el-input v-model="resetForm.rePwd" show-password placeholder="再次输入新密码" prefix-icon="el-icon-lock"></el-input>
          </el-form-item>
          <el-button class="login-btn" @click="doResetPwd" :loading="resetLoading">重置密码</el-button>
        </el-form>
      </div>
    </transition>
  </div>
</template>

<script>
import axios from 'axios';

// 密码强度：8-16位，含数字、小写字母，且含≥1个特殊字符或大写字母
const PWD_REGEX = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{8,16}$/;

export default {
  name: 'Login',
  data() {
    const validatePwd = (rule, value, cb) => {
      if (!value) return cb(new Error('请输入密码'));
      if (!PWD_REGEX.test(value)) {
        return cb(new Error('8-16位，需含数字+小写字母，且含≥1个大写字母或特殊字符'));
      }
      cb();
    };
    const validateCheckPass = (rule, value, cb) => {
      if (!value) return cb(new Error('请再次输入密码'));
      if (value !== this.regForm.password) return cb(new Error('两次输入密码不一致'));
      cb();
    };
    const validateRePwd = (rule, value, cb) => {
      if (!value) return cb(new Error('请再次输入新密码'));
      if (value !== this.resetForm.newPwd) return cb(new Error('两次输入密码不一致'));
      cb();
    };

    return {
      mode: 'select', // select | user | admin | register | forgot

      // 用户登录
      user: { username: '', password: '' },
      loginLoading: false,
      loginRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
      },

      // 管理员登录
      adminUser: { username: '', password: '' },

      // 注册
      regForm: { username: '', phone: '', email: '', password: '', checkPass: '' },
      regLoading: false,
      regRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { pattern: /^[a-zA-Z0-9_]{3,20}$/, message: '3-20位，字母/数字/下划线', trigger: 'blur' },
        ],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的11位手机号', trigger: 'blur' },
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
        ],
        password: [{ validator: validatePwd, trigger: 'blur' }],
        checkPass: [{ validator: validateCheckPass, trigger: 'blur' }],
      },

      // 忘记密码
      forgotStep: 1,
      verifyForm: { email: '', phone: '' },
      verifyLoading: false,
      verifyRules: {
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
        ],
        phone: [
          { required: true, message: '请输入手机号', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的11位手机号', trigger: 'blur' },
        ],
      },
      verifiedUid: '',
      resetForm: { newPwd: '', rePwd: '' },
      resetLoading: false,
      resetRules: {
        newPwd: [{ validator: validatePwd, trigger: 'blur' }],
        rePwd: [{ validator: validateRePwd, trigger: 'blur' }],
      },
    };
  },
  watch: {
    mode(val) {
      // 切换模式时重置表单
      if (val === 'register') {
        this.$nextTick(() => this.$refs.regForm && this.$refs.regForm.resetFields());
      }
      if (val === 'forgot') {
        this.forgotStep = 1;
        this.verifiedUid = '';
        this.$nextTick(() => {
          this.$refs.verifyForm && this.$refs.verifyForm.resetFields();
        });
      }
    }
  },
  methods: {
    // ---------- 用户登录 ----------
    doLogin() {
      this.$refs.loginForm.validate(valid => {
        if (!valid) return;
        this.loginLoading = true;
        axios.post('http://localhost:8080/user/login', {
          username: this.user.username,
          password: this.user.password,
        }).then(res => {
          const { code, msg, data } = res.data;
          if (code === 1) {
            window.localStorage.setItem('Authorization', data);
            const loading = this.$loading({ lock: true, text: '正在登录...', background: 'rgba(0,0,0,0.7)' });
            setTimeout(() => { this.$router.push('/data1'); loading.close(); }, 500);
          } else if (code === -1) {
            this.$message.error(msg); // 账号锁定
          } else {
            this.$message.warning(msg);
          }
        }).catch(err => {
          this.$message.error('登录失败：' + err.message);
        }).finally(() => { this.loginLoading = false; });
      });
    },

    // ---------- 管理员登录（本地硬编码） ----------
    doAdminLogin() {
      if (this.adminUser.username === 'admin' && this.adminUser.password === '123456') {
        this.$message.success('管理员登录成功');
        localStorage.setItem('uid', 'admin_user_001');
        setTimeout(() => this.$router.push({ name: 'main' }), 800);
      } else {
        this.$message.error('管理员账号或密码错误');
      }
    },

    // ---------- 注册 ----------
    doRegister() {
      this.$refs.regForm.validate(valid => {
        if (!valid) return;
        this.regLoading = true;
        axios.post('http://localhost:8080/user/registerservlet', {
          username: this.regForm.username,
          phone:    this.regForm.phone,
          email:    this.regForm.email,
          password: this.regForm.password,
        }).then(res => {
          if (res.data.code === 1 || res.data.code === '1') {
            this.$message.success('注册成功，请登录');
            this.mode = 'user';
          } else {
            this.$message.error(res.data.msg || '注册失败');
          }
        }).catch(err => {
          this.$message.error('注册失败：' + err.message);
        }).finally(() => { this.regLoading = false; });
      });
    },

    // ---------- 忘记密码：验证身份 ----------
    doVerify() {
      this.$refs.verifyForm.validate(valid => {
        if (!valid) return;
        this.verifyLoading = true;
        axios.post('http://localhost:8080/user/verifyUser', {
          email: this.verifyForm.email,
          phone: this.verifyForm.phone,
        }).then(res => {
          if (res.data.code === 1 || res.data.code === '1') {
            this.verifiedUid = res.data.data.uid;
            this.forgotStep = 2;
            this.$message.success('身份验证通过，请设置新密码');
          } else {
            this.$message.error(res.data.msg || '邮箱或手机号不匹配');
          }
        }).catch(err => {
          this.$message.error('验证失败：' + err.message);
        }).finally(() => { this.verifyLoading = false; });
      });
    },

    // ---------- 忘记密码：重置密码 ----------
    doResetPwd() {
      this.$refs.resetForm.validate(valid => {
        if (!valid) return;
        this.resetLoading = true;
        axios.put('http://localhost:8080/user/resetPwd', {
          uid:    this.verifiedUid,
          newPwd: this.resetForm.newPwd,
        }).then(res => {
          if (res.data.code === 1 || res.data.code === '1') {
            this.$message.success('密码重置成功，请重新登录');
            this.mode = 'user';
          } else {
            this.$message.error(res.data.msg || '密码重置失败');
          }
        }).catch(err => {
          this.$message.error('重置失败：' + err.message);
        }).finally(() => { this.resetLoading = false; });
      });
    },
  },
};
</script>

<style lang="less" scoped>
@keyframes float-orb {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}
@keyframes fadeSlideUp {
  from { opacity: 0; transform: translateY(24px); }
  to   { opacity: 1; transform: translateY(0); }
}

.fade-slide-enter-active, .fade-slide-leave-active {
  transition: all 0.35s ease;
  position: absolute;
}
.fade-slide-enter { opacity: 0; transform: translateY(20px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-20px); }

.login-root {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100vh;
  background: var(--bg-base);
  position: relative;
  overflow: hidden;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}
.orb1 {
  width: 600px; height: 600px;
  background: radial-gradient(circle, rgba(0,212,255,0.12) 0%, transparent 70%);
  top: -150px; left: -150px;
  animation: float-orb 8s ease-in-out infinite;
}
.orb2 {
  width: 500px; height: 500px;
  background: radial-gradient(circle, rgba(124,58,237,0.1) 0%, transparent 70%);
  bottom: -100px; right: -100px;
  animation: float-orb 10s ease-in-out infinite reverse;
}
.grid-overlay {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(0,212,255,0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0,212,255,0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
}

.login-wrap {
  position: relative;
  z-index: 10;
  width: 420px;
  padding: 44px 40px;
  border-radius: var(--radius-lg);
  background: rgba(22, 27, 34, 0.88);
  backdrop-filter: blur(24px);
  -webkit-backdrop-filter: blur(24px);
  border: 1px solid var(--border-subtle);
  box-shadow: 0 8px 60px rgba(0,0,0,0.6), inset 0 1px 0 rgba(255,255,255,0.05);
  animation: fadeSlideUp 0.5s ease-out;

  &::before {
    content: '';
    position: absolute;
    top: 0; left: 10%; width: 80%; height: 1px;
    background: linear-gradient(90deg, transparent, var(--accent-primary), transparent);
    opacity: 0.5;
  }
}

.wide-wrap { width: 480px; }

.admin-wrap {
  &::before {
    background: linear-gradient(90deg, transparent, #a855f7, transparent);
  }
}

.back-btn {
  color: var(--text-muted);
  font-size: 13px;
  cursor: pointer;
  margin-bottom: 20px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
  &:hover { color: var(--accent-primary); }
}

.login-title {
  text-align: center;
  margin-bottom: 32px;

  .title-icon {
    font-size: 30px;
    color: var(--accent-primary);
    filter: drop-shadow(0 0 8px rgba(0,212,255,0.5));
    margin-bottom: 10px;
  }
  .admin-icon { color: #a855f7; filter: drop-shadow(0 0 8px rgba(168,85,247,0.5)); }

  h3 {
    font-size: 24px;
    font-weight: 700;
    background: var(--accent-gradient);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    letter-spacing: 1px;
    margin-bottom: 6px;
  }
  .admin-title {
    background: linear-gradient(135deg, #a855f7, #7c3aed);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  p {
    color: var(--text-muted);
    font-size: 11px;
    letter-spacing: 1.5px;
  }
}

// 入口选择卡片
.role-cards {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}
.role-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 28px 16px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-subtle);
  background: rgba(255,255,255,0.03);
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    border-color: var(--accent-primary);
    background: rgba(0,212,255,0.06);
    transform: translateY(-4px);
    box-shadow: 0 8px 30px rgba(0,212,255,0.15);
  }

  .card-icon {
    font-size: 36px;
    color: var(--accent-primary);
  }
  .card-label {
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);
  }
  .card-desc {
    font-size: 12px;
    color: var(--text-muted);
  }
}
.admin-card {
  &:hover {
    border-color: #a855f7;
    background: rgba(168,85,247,0.06);
    box-shadow: 0 8px 30px rgba(168,85,247,0.15);
  }
  .card-icon { color: #a855f7; }
}

/deep/ .el-form-item__label {
  color: var(--text-secondary) !important;
  font-size: 13px !important;
  font-weight: 500 !important;
}

/deep/ .el-input__inner {
  height: 42px !important;
  background-color: rgba(33,38,45,0.8) !important;
  border: 1px solid rgba(0,212,255,0.15) !important;
  color: var(--text-primary) !important;
  border-radius: 8px !important;
  font-size: 14px !important;
  transition: all 0.3s ease !important;

  &::placeholder { color: var(--text-muted) !important; }
  &:focus {
    border-color: var(--accent-primary) !important;
    box-shadow: 0 0 0 3px rgba(0,212,255,0.12) !important;
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  background: var(--accent-gradient) !important;
  border: none !important;
  border-radius: 8px !important;
  color: #fff !important;
  margin-top: 6px;
  transition: all 0.3s ease !important;
  box-shadow: 0 4px 20px rgba(0,212,255,0.3) !important;

  &:hover {
    transform: translateY(-2px) !important;
    box-shadow: 0 8px 30px rgba(0,212,255,0.5) !important;
  }
}
.admin-btn {
  background: linear-gradient(135deg, #a855f7, #7c3aed) !important;
  box-shadow: 0 4px 20px rgba(168,85,247,0.3) !important;
  &:hover { box-shadow: 0 8px 30px rgba(168,85,247,0.5) !important; }
}

.button-group {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
}

/deep/ .el-button--text {
  color: var(--text-muted) !important;
  font-size: 13px !important;
  padding: 0 !important;
  &:hover { color: var(--accent-primary) !important; }
}
</style>
