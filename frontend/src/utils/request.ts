import axios from 'axios';
import { showToast, showDialog } from 'vant';

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api', // 配合 vite.config.ts 代理
  timeout: 15000,
});

// 请求拦截器：注入 Bearer Token
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

// 响应拦截器：处理业务状态码
service.interceptors.response.use(
  (response) => {
    const res = response.data;
    // 规约约定：code === 0 为成功
    if (res.code !== 0) {
      // 处理 Token 过期 (401)
      if (res.code === 401) {
        showDialog({ message: '登录已过期，请重新登录' }).then(() => {
          localStorage.clear();
          window.location.href = '/login';
        });
      } else {
        showToast(res.msg || '系统错误');
      }
      return Promise.reject(new Error(res.msg || 'Error'));
    }
    return res.data;
  },
  (error) => {
    showToast(error.message || '网络异常');
    return Promise.reject(error);
  }
);

export default service;
