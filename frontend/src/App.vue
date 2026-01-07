<script setup lang="ts">
// 这里不需要写逻辑，路由会接管一切
</script>

<template>
  <router-view />
</template>

<style>
/* 全局样式重置，防止默认样式的干扰 */
body {
  margin: 0;
  padding: 0;
  background-color: #f7f8fa; /* 统一背景色 */
  font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica, Segoe UI, Arial, Roboto, 'PingFang SC', 'miui', 'Hiragino Sans GB', 'Microsoft Yahei', sans-serif;
}
</style>
