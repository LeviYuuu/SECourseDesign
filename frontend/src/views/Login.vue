<template>
  <div class="login-page">
    <h2>社交焦虑陪练系统</h2>
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field v-model="form.account" name="学号" label="学号" placeholder="请输入学号" />
        <van-field v-model="form.password" type="password" name="密码" label="密码" placeholder="请输入密码" />
      </van-cell-group>
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit">登录</van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '@/api';
import { useUserStore } from '@/stores/user';
import { showToast } from 'vant';

const router = useRouter();
const userStore = useUserStore();
const form = reactive({ account: '20230001', password: 'password', loginType: 'PASSWORD', accountType: 'student_no' });

const onSubmit = async () => {
  try {
    const res: any = await login(form);
    userStore.setLoginState(res.token, res.user);
    showToast('登录成功');
    router.push('/scenarios');
  } catch (e) {
    // 错误已在 request.ts 处理
  }
};
</script>

<style scoped>
.login-page { padding-top: 100px; text-align: center; background-color: #f7f8fa; height: 100vh; }
h2 { margin-bottom: 40px; color: #1989fa; }
</style>
