<template>
  <div class="register-container">
    <div class="register-box">
      <h2>注册新账号</h2>
      <form @submit.prevent="handleRegister">
        
        <div class="form-group">
          <label>邮箱</label>
          <input 
            v-model="form.email" 
            type="email" 
            placeholder="请输入邮箱" 
            required 
          />
        </div>

        <div class="form-group">
          <label>昵称</label>
          <input 
            v-model="form.nickname" 
            type="text" 
            placeholder="您的称呼" 
            required 
          />
        </div>

        <div class="form-group">
          <label>密码</label>
          <input 
            v-model="form.password" 
            type="password" 
            placeholder="设置密码" 
            required 
          />
        </div>

        <button type="submit" :disabled="loading" class="submit-btn">
          {{ loading ? '注册中...' : '立即注册' }}
        </button>

        <div class="link-area">
          <router-link to="/login">已有账号？去登录</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { register } from '@/api'; // 确保这里指向正确

const router = useRouter();
const loading = ref(false);

const form = ref({
  email: '',
  password: '',
  nickname: ''
});

const handleRegister = async () => {
  if (!form.value.email || !form.value.password) {
    alert('请填写完整信息');
    return;
  }

  loading.value = true;
  try {
    // 调用接口
    // 因为 request.ts 的拦截器机制：
    // 1. 如果 code != 1，会抛出异常进入 catch
    // 2. 如果 code == 1，返回的是 res.data (即 { userId: xxx })
    const data = await register({
      email: form.value.email,
      password: form.value.password,
      nickname: form.value.nickname
    });

    // 能走到这里，说明绝对成功了
    // 我们可以判断一下 data 是否存在，或者 data.userId 是否存在
    if (data) {
      alert('注册成功！请登录。');
      router.push('/login');
    } 
  } catch (error: any) {
    console.error(error);
    // 错误信息已经在 request.ts 里弹出了(showToast)，这里可以不再alert，或者做个兜底
    // 如果想要显示错误，通常 request.ts 抛出的 error.message 就是后端返回的 msg
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.register-box {
  width: 360px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box; /* 关键：防止padding撑大宽度 */
  outline: none;
  transition: border-color 0.2s;
}

.form-group input:focus {
  border-color: #409eff;
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  margin-top: 10px;
}

.submit-btn:hover {
  background-color: #66b1ff;
}

.submit-btn:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.link-area {
  text-align: right;
  margin-top: 15px;
}

.link-area a {
  color: #409eff;
  font-size: 14px;
  text-decoration: none;
}

.link-area a:hover {
  text-decoration: underline;
}
</style>
