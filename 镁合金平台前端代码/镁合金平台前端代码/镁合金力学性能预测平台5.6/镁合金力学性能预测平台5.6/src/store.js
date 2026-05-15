import Vue from "vue";
import Vuex from "vuex"

Vue.use(Vuex)
export default new Vuex.Store({
  state: {
    //格式化的数据
    dealedData: [],
    //开始导入的数据
    etableData: [],
    //图像导入的数据
    imageData: [],
    //归一化的数据
    oneData: [],
    //手动输入的数据
    insertData: [],
    //文件导入数据方式的状态
    fileStatus: true,
    //手动输入数据方式的状态
    insertStatus: true,
    //预测的数据
    totalData: [],
    strength: [],
    startHard: [],
    yieldstr: []
  },
  mutations: {
    //获取数据
    getData(state, data) {
      state.etableData = data
    },
    //格式化数据
    dealData(state, data) {
      state.dealedData = data
    },
    //归一化数据
    oneDealData(state, data) {
      state.oneData = data
    },
    //手动输入的数据
    getInsertData(state, data) {
      state.insertData = data
    },
    //文件状态变更
    fileStatusChange(state) {
      state.fileStatus = !state.fileStatus
    },
    //手动输入状态变更
    insertStatusChange(state) {
      state.insertStatus = !state.insertStatus
    },
    //历史记录所呈现的数据
    gettotalData(state, data) {
      state.totalData = data
    },
    getstartHard(state, data) {
      state.startHard = data
    },
    getyieldstr(state, data) {
      state.yieldstr = data
    },
    getstrength(state, data) {
      state.strength = data
    }
  }
})