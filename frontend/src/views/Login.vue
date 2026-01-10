<template>
  <div class="login-page">
    <div class="logo-area">
      <div class="logo">💬</div>
      <h2>社交焦虑陪练系统</h2>
      <p class="sub-title">AI 驱动的专业场景模拟训练</p>
    </div>

    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="form.username"
          name="username"
          label="账号"
          placeholder="请输入学号/手机号"
          :rules="[{ required: true, message: '请填写账号' }]"
        />
        <van-field
          v-model="form.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          :rules="[{ required: true, message: '请填写密码' }]"
        />
      </van-cell-group>
      
      <div style="margin: 30px 16px;">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          立即登录
        </van-button>
      </div>
      
      <div style="text-align: right; margin-top: 10px;">
        <span style="color: #666; font-size: 14px;">还没有账号？</span>
        <router-link to="/register" style="color: #409eff; font-size: 14px; text-decoration: none;">
          去注册
        </router-link>
      </div>
    </van-form>

    <div class="footer">
      基于 V1.1 微服务架构设计
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '@/api';
import { useUserStore } from '@/stores/user';
import { showToast } from 'vant';

const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);

const form = reactive({
  username: '20230001',
  password: 'password'
});

const onSubmit = async () => {
  loading.value = true;
  try {
    const data: any = await login(form);
    
    // ✅ 确保正确存储用户信息
    if (data && data.token) {
      userStore.setLoginState(data.token, { 
        userId: data.userId,
        nickname: data.nickname || form.username
      });
      showToast('登录成功');
      router.push('/scenarios');
    } else {
      throw new Error('登录失败：返回数据格式异常');
    }
  } catch (err: any) {
    console.error('Login error:', err);
    showToast(err.message || '登录失败');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-page { min-height: 100vh; background-color: #f7f8fa; padding-top: 80px; box-sizing: border-box; }
.logo-area { text-align: center; margin-bottom: 40px; }
.logo { font-size: 60px; margin-bottom: 10px; }
h2 { color: #333; margin: 0 0 10px 0; font-size: 24px; }
.sub-title { color: #999; font-size: 14px; margin: 0; }
.footer { text-align: center; margin-top: 50px; color: #ccc; font-size: 12px; }
</style>
