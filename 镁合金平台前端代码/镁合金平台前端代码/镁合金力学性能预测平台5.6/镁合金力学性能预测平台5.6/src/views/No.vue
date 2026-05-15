<template>
  <div>
    <h1>已退出</h1>
  </div>
</template>

<script>
export default {
  mounted() {
    // 清除登录状态
    localStorage.removeItem('uid');
    localStorage.removeItem('Authorization');
    this.$message.success('已退出登录');

    // 自动跳转到登录页面
    setTimeout(() => {
      this.$router.push('/login');
    }, 1000); // 1秒后跳转
  }
};
</script>

<style lang="less" scoped>
div {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f7f7f7;
}

h1 {
  color: #333;
  font-size: 24px;
  text-align: center;
}
</style>