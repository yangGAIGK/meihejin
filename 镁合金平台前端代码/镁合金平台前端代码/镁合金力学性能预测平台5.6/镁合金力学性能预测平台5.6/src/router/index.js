import Vue from 'vue'
import VueRouter from 'vue-router'
import Data from '../views/Data'
import Data2 from '../views/Data2'
import History from '../views/History'
import Main from '../views/Main'
import Loginout from '../views/Loginout'
import No from '../views/No'
import Login from '../views/Login'
import Welcome from '../views/Welcome'
import Echarts from '../views/Echarts'
import Input from "../views/Input"
import profile from "@/views/profile.vue";
import Data3 from "@/views/Data3.vue";
import bpTable from "@/views/bpTable.vue";
import psoTable from "@/views/psoTable.vue";
import gaTable from "@/views/gaTable.vue";
import Data2Next from '@/views/Data2Next.vue';
import bpModel2 from "@/views/bpModel2.vue"; // 导入新的页面组件
import psoModel2 from "@/views/psoModel2.vue";
import psobpModel2 from "@/views/psobpModel2.vue";
import DataNext from "@/views/DataNext.vue";
import bpModel from "@/views/bpModel.vue";
import psoModel from "@/views/psoModel.vue";
import psobpModel from "@/views/psobpModel.vue";
import psohistory from "@/views/psohistory.vue";
import psobphistory from "@/views/psobphistory.vue";
import goHistoryFrame from "@/views/data3history.vue";
import InputFrame from '@/views/InputFrame.vue'
import goImages from '@/views/data3images.vue'
import TempPredictSingle from '@/views/TempPredictSingle.vue'
import TempPredictBatch from '@/views/TempPredictBatch.vue'
import TempPredictHistory from '@/views/TempPredictHistory.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: "/welcome",
    component: Welcome,
  },
  {
    path: "/",
    redirect: "/welcome",
  },
  {
    path: "/login",
    component: Login,
  },
  {
    path: "/main",
    name: "main",
    component: Main,
    children: [
      { path: "/data1", component: Data },
      { path: "/data2", component: Data2 },
      { path: "/data3", component: Data3 },
      { path: "/history", component: History },
      { path: "/psohistory", component: psohistory },
      { path: "/psobphistory", component: psobphistory },
      { path: "/history", component: History },
      { path: "/out", component: Loginout },
      { path: "/echarts", component: Echarts },
      { path: "/input", component: Input },
      { path: "/inputFrame", component: InputFrame },
      { path: "/profile", component: profile },
      { path: "/bpTable", component: bpTable },
      { path: "/psoTable", component: psoTable },
      { path: "/gaTable", component: gaTable },
      // 在这里添加 /data2-next 路由
      { path: "/data2-next", component: Data2Next }, // 添加新的路由
      { path: "/bpModel2", component: bpModel2 }, // 添加新的路由
      { path: "/psoModel2", component: psoModel2 }, // 添加新的路由
      { path: "/psobpModel2", component: psobpModel2 }, // 添加新的路由
      { path: "/data-next", component: DataNext }, // 添加新的路由
      { path: "/bpModel", component: bpModel }, // 添加新的路由
      { path: "/psoModel", component: psoModel }, // 添加新的路由
      { path: "/psobpModel", component: psobpModel }, // 添加新的路由
      { path: "/data3history", component: goHistoryFrame }, // 添加新的路由
      { path: "/data3images", component: goImages },
      { path: "/tempPredictSingle", component: TempPredictSingle },
      { path: "/tempPredictBatch", component: TempPredictBatch },
      { path: "/tempPredictHistory", component: TempPredictHistory },
    ],
  },
  {
    path: "/no",
    name: "no",
    component: No,
  },
];

const originalPush = VueRouter.prototype.push;
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
}

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
