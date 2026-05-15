import Vue from 'vue'
import App from './App.vue'
import store from './store';
import router from './router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import axios from 'axios'
// import './api/mock'
//axios.defaults.baseURL='http://localhost:8088'
Vue.config.productionTip = false
Vue.use(ElementUI);
new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
