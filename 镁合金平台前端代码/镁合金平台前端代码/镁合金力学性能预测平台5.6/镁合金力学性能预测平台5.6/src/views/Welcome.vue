<template>
  <div class="main">
    <div id="particles-js"></div>
    <div class="title">镁合金性能-裂纹-温度预测识别集成系统</div>
    <el-button class="btn" @click="Login">欢迎使用</el-button>
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
    // 加载粒子特效
    loadParticles () {
      particlesJS("particles-js", {
        particles: {
          number: {
            value: 80,
            density: {
              enable: true,
              value_area: 1200,
            },
          },
          color: {
            value: "#ffffff",
          },
          shape: {
            type: "circle",
          },
          opacity: {
            value: 0.8,
          },
          size: {
            value: 3
          },
          line_linked: {
            enable: true,
            distance: 180,
            color: "#00ffff",
            opacity: 0.6,
            width: 1,
          },
          move: {
            enable: true,
            speed: 8,
            direction: "none",
            random: false,
            straight: false,
            out_mode: "out",
            attract: {
              enable: true,
            },
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
              enable: true,
              mode: "push",
            },
          },
          modes: {
            repulse: {
              distance: 100,
              duration: 0.4,
            },
            push: {
              particles_nb: 4,
            },
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
  background-size: 100% 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  animation: fadeIn 1.5s ease-in-out, backgroundPulse 6s infinite alternate;
}

/* 粒子背景层 */
#particles-js {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

/* 背景渐变动画 */
@keyframes backgroundPulse {
  0% {
    filter: brightness(1);
  }
  100% {
    filter: brightness(1.3);
  }
}

/* 淡入效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 顶部标题样式 */
.title {
  position: absolute;
  top: 28%; /* 往上挪一点 */
  font-size: 68px;
  font-weight: bold;
  color: #00ffff;
  text-shadow: 2px 2px 8px rgba(0, 255, 255, 0.8);
  z-index: 1;
  animation: titleFadeIn 2s ease-in-out, titlePulse 2s infinite alternate;
  text-align: center;
  width: 90%;
  line-height: 1.3;
}

/* 标题渐入动画 */
@keyframes titleFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 标题缩放动画 */
@keyframes titlePulse {
  0% {
    transform: scale(1);
  }
  100% {
    transform: scale(1.05);
  }
}

.btn {
  position: absolute;
  top: 48%; /* 关键：往上挪，离标题更近 */
  transform: translateY(-50%);
  width: 300px;
  height: 80px;
  background-color: transparent;
  border: 2px solid #00ffff;
  border-radius: 8px;
  font-size: 24px;
  font-weight: bold;
  color: #00ffff;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.5);
  transition: all 0.3s ease;
  animation: btnPulse 1.5s infinite alternate;
}

.btn:hover {
  background-color: rgba(0, 255, 255, 0.2);
  color: #fff;
  box-shadow: 0 4px 10px rgba(0, 255, 0.5);
  cursor: pointer;
}

/* 按钮缩放动画 */
@keyframes btnPulse {
  0% {
    transform: scale(1);
  }
  100% {
    transform: scale(1.1);
  }
}
</style>