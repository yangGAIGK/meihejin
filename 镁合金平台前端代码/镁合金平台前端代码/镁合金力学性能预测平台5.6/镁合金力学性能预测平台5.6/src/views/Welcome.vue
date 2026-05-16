<template>
  <div class="main">
    <div id="particles-js"></div>
    <div class="overlay"></div>

    <div class="content-wrapper">
      <div class="system-logo">
        <div class="logo-icon">
          <i class="el-icon-cpu"></i>
        </div>
      </div>

      <div class="title-section">
        <h1 class="system-title">镁合金性能-裂纹-温度预测识别集成系统</h1>
        <p class="system-subtitle">MAGNESIUM ALLOY ANALYSIS & PREDICTION PLATFORM</p>
      </div>

      <div class="action-section">
        <el-button class="enter-btn" @click="Login">
          <span>进入系统</span>
          <i class="el-icon-arrow-right"></i>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import "particles.js";

export default {
  mounted () {
    this.loadParticles();
  },
  methods: {
    Login () {
      this.$router.push('login')
    },
    // 优化粒子特效，更克制不抢戏
    loadParticles () {
      particlesJS("particles-js", {
        particles: {
          number: {
            value: 60,
            density: {
              enable: true,
              value_area: 1500,
            },
          },
          color: {
            value: "#4a9eff",
          },
          shape: {
            type: "circle",
          },
          opacity: {
            value: 0.5,
            random: { enable: true, minimumValue: 0.2 }
          },
          size: {
            value: 2,
            random: { enable: true, minimumValue: 1 }
          },
          line_linked: {
            enable: true,
            distance: 200,
            color: "#4a9eff",
            opacity: 0.25,
            width: 1,
          },
          move: {
            enable: true,
            speed: 1.5,
            direction: "none",
            random: true,
            straight: false,
            out_mode: "out",
            bounce: false
          },
        },
        interactivity: {
          detect_on: "canvas",
          events: {
            onhover: {
              enable: true,
              mode: "grab",
            },
            onclick: {
              enable: false
            },
            resize: true
          },
          modes: {
            grab: {
              distance: 140,
              line_linked: { opacity: 0.4 }
            }
          },
        },
        retina_detect: true,
      });
    },
  }
}
</script>

<style lang="less" scoped>
.main {
  background-image: url("../assets/001.png");
  height: 100vh;
  background-size: cover;
  background-position: center;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

/* 深色遮罩，提升文字可读性 */
.overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(8, 12, 24, 0.65);
  z-index: 1;
}

/* 粒子背景层 */
#particles-js {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
}

/* 内容居中容器 —— 已添加向上移动 */
.content-wrapper {
  position: relative;
  z-index: 3;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 40px;
  padding: 0 20px;
  animation: fadeSlideUp 1.5s ease-out;
  /* 核心：整体向上移动，可自行调整数值 */
  transform: translateY(-12%);
}

/* 顶部图标 */
.system-logo {
  .logo-icon {
    width: 100px;
    height: 100px;
    border: 2px solid rgba(74, 158, 255, 0.6);
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    background: rgba(74, 158, 255, 0.05);
    box-shadow: 0 0 30px rgba(74, 158, 255, 0.2);

    i {
      font-size: 40px;
      color: #4a9eff;
    }
  }
}

/* 标题区域 */
.title-section {
  .system-title {
    font-size: 52px;
    font-weight: 600;
    color: #ffffff;
    margin: 0 0 12px 0;
    letter-spacing: 4px;
    text-shadow: 0 0 20px rgba(74, 158, 255, 0.4);
  }

  .system-subtitle {
    font-size: 16px;
    color: rgba(255, 255, 255, 0.6);
    margin: 0;
    letter-spacing: 2px;
    text-transform: uppercase;
  }
}

/* 按钮区域 */
.action-section {
  margin-top: 20px;

  .enter-btn {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    padding: 16px 50px;
    font-size: 18px;
    font-weight: 500;
    letter-spacing: 2px;
    color: #ffffff;
    background: linear-gradient(90deg, rgba(74, 158, 255, 0.2), rgba(74, 158, 255, 0.05));
    border: 1px solid rgba(74, 158, 255, 0.5);
    border-radius: 4px;
    box-shadow: 0 0 20px rgba(74, 158, 255, 0.15);
    transition: all 0.3s ease;

    i {
      font-size: 16px;
      transition: transform 0.3s ease;
    }

    &:hover {
      background: linear-gradient(90deg, rgba(74, 158, 255, 0.35), rgba(74, 158, 255, 0.1));
      border-color: rgba(74, 158, 255, 0.8);
      box-shadow: 0 0 30px rgba(74, 158, 255, 0.3);
      transform: translateY(-2px);

      i {
        transform: translateX(4px);
      }
    }

    &:active {
      transform: translateY(0);
      box-shadow: 0 0 15px rgba(74, 158, 255, 0.2);
    }
  }
}

/* 整体渐入动画 */
@keyframes fadeSlideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>