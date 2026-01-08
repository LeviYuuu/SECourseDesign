import axios from 'axios';
import { showToast, showDialog } from 'vant';

const service = axios.create({
  baseURL: 'http://localhost:8080', 
  timeout: 15000,
});

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data;
    // ✅ 严格遵循 V1.1 规约：code === 1 代表成功
    if (res.code !== 1) {
      if (res.code === 401) {
        showDialog({ message: '登录失效，请重新登录' }).then(() => {
          localStorage.clear();
          window.location.href = '/login';
        });
      } else {
        showToast(res.msg || '系统错误');
      }
      return Promise.reject(new Error(res.msg || 'Error'));
    }
    // 直接返回 data 层数据
    return res.data; 
  },
  (error) => {
    showToast(error.message || '网络异常');
    return Promise.reject(error);
  }
);

export default service;
