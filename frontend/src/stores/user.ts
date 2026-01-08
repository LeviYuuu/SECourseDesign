import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '');
  const userInfo = ref<any>({});

  // 初始化时尝试读取缓存
  try {
    const rawInfo = localStorage.getItem('userInfo');
    if (rawInfo && rawInfo !== 'undefined' && rawInfo !== 'null') {
      userInfo.value = JSON.parse(rawInfo);
    }
  } catch (e) {
    console.error('User info parse error', e);
    localStorage.removeItem('userInfo');
  }

  function setLoginState(newToken: string, user: any) {
    token.value = newToken;
    userInfo.value = user;
    localStorage.setItem('token', newToken);
    // 确保写入的是有效的 JSON 字符串
    localStorage.setItem('userInfo', JSON.stringify(user));
  }

  function logout() {
    token.value = '';
    userInfo.value = {};
    localStorage.clear();
  }

  return { token, userInfo, setLoginState, logout };
});
