import axios from 'axios';

const instance = axios.create({
    baseURL: 'http://localhost:8080',  // 后端服务的地址
    timeout: 10000,  // 设置请求超时
});

export default instance;
