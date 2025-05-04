    import axios from 'axios';
    import router from "../router";
    import { ElMessage } from 'element-plus';
    
    const request = axios.create({
      baseURL: '/api',
      timeout: 5000,
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    request.interceptors.request.use(
      config => {
        const result = JSON.parse(localStorage.getItem('loginUser'));
        if (result && result.token) {
          config.headers.token = result.token;
        }
        return config;
      },
      error => {
        return Promise.reject(error);
      }
    );
    
    request.interceptors.response.use(
      response => {
        return response.data;
      },
      error => {
        if (error.response) {
          if (error.response.status === 401) {
            ElMessage.error('登录已超时，请重新登录');
            router.push('/login');
          } else {
            // 处理其他状态码错误
            ElMessage.error(`非401错误，请求失败: ${error.response.status} ${error.response.statusText}`);
          }
        } else if (error.request) {
          // 请求已发出但没有收到响应
          ElMessage.error('请求超时或网络错误，请稍后再试');
        } else {
          // 其他错误
          ElMessage.error(`请求失败: ${error.message}`);
        }
        return Promise.reject(error);
      }
    );
    
    export default request;