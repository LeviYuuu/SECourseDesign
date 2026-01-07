import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '');
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'));

  function setLoginState(newToken: string, user: any) {
    token.value = newToken;
    userInfo.value = user;
    localStorage.setItem('token', newToken);
    localStorage.setItem('userInfo', JSON.stringify(user));
  }

  function logout() {
    token.value = '';
    userInfo.value = {};
    localStorage.clear();
  }

  return { token, userInfo, setLoginState, logout };
});
