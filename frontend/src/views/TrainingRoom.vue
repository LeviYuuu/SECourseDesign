<template>
  <div class="room-container">
    <van-nav-bar 
      :title="`第 ${currentRound} 轮`" 
      left-text="结束" 
      @click-left="manualFinish" 
      fixed placeholder 
    />

    <div class="chat-list" ref="chatRef">
      <div v-for="(msg, index) in messages" :key="index" :class="['msg-row', msg.role === 'USER' ? 'msg-right' : 'msg-left']">
        <div class="avatar">{{ msg.role === 'USER' ? '我' : 'AI' }}</div>
        <div class="bubble">
          {{ msg.content }}
          <div v-if="msg.hint" class="hint-box">💡 提示: {{ msg.hint }}</div>
        </div>
      </div>
      
      <div v-if="loading" class="msg-row msg-left">
        <div class="avatar">AI</div>
        <div class="bubble">...</div>
      </div>
    </div>

    <div class="input-area">
      <van-field 
        v-model="inputText" 
        placeholder="请输入回答..." 
        center 
        clearable
        @keydown.enter.prevent="sendText"
      >
        <template #button>
          <van-button size="small" type="primary" @click="sendText" :disabled="loading || !inputText">发送</van-button>
        </template>
      </van-field>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getNextQuestion, submitEvaluation } from '@/api';
import { showConfirmDialog, showToast, closeToast } from 'vant'; // 👈 引入 closeToast

const route = useRoute();
const router = useRouter();
const sessionId = route.params.sessionId as string;

const messages = ref<any[]>([
  { role: 'SYSTEM', content: '你好，我是面试官。请开始你的自我介绍。' } 
]);
const currentRound = ref(1);
const inputText = ref('');
const loading = ref(false);
const chatRef = ref<HTMLElement>();

const scrollToBottom = () => nextTick(() => {
  if (chatRef.value) chatRef.value.scrollTop = chatRef.value.scrollHeight;
});

// ✅ 核心修复：提交任务并清理 Toast
const submitTask = async () => {
  try {
    // 开启一个持续的 Loading，禁止用户乱点
    showToast({ message: '正在生成评估报告...', type: 'loading', duration: 0, forbidClick: true });
    
    // 提交评估
    await submitEvaluation({ sessionId });
    
    // 🔴 关键修复：跳转前必须关闭 Loading！
    closeToast();
    
    // 跳转
    router.replace(`/report/${sessionId}`);
  } catch (e) {
    closeToast(); // 失败也要关闭
    showToast('提交失败，请重试');
  }
};

const sendText = async () => {
  if (!inputText.value) return;
  
  messages.value.push({ role: 'USER', content: inputText.value });
  const val = inputText.value;
  inputText.value = '';
  loading.value = true;
  scrollToBottom();

  try {
    const res: any = await getNextQuestion({
      sessionId: sessionId,
      currentRound: currentRound.value,
      userAnswer: val
    });

    if (res.isEnd) {
      if (res.question) {
        messages.value.push({ role: 'SYSTEM', content: res.question });
        scrollToBottom();
      }
      // 延迟跳转
      setTimeout(() => {
        submitTask();
      }, 1500);
      return;
    }

    messages.value.push({ 
      role: 'SYSTEM', 
      content: res.question || "...", 
      hint: res.hint 
    });
    
    if (res.round) currentRound.value = res.round;

  } catch (error) {
    console.error(error);
    showToast('AI 响应失败');
  } finally {
    loading.value = false;
    scrollToBottom();
  }
};

const manualFinish = () => {
  showConfirmDialog({ title: '结束训练', message: '确定要提前结束并生成评估报告吗？' })
    .then(() => {
      submitTask();
    }).catch(() => {});
};
</script>

<style scoped>
/* 样式保持不变 */
.room-container { height: 100vh; display: flex; flex-direction: column; background: #ededed; }
.chat-list { flex: 1; overflow-y: auto; padding: 15px; display: flex; flex-direction: column; gap: 15px; }
.msg-row { display: flex; width: 100%; }
.msg-right { justify-content: flex-end; }
.avatar { width: 40px; height: 40px; border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 14px; color: #fff; flex-shrink: 0; }
.msg-left .avatar { background: #ff976a; margin-right: 10px; }
.msg-right .avatar { background: #1989fa; margin-left: 10px; order: 2; }
.bubble { max-width: 75%; padding: 12px; border-radius: 8px; font-size: 15px; line-height: 1.5; background: #fff; position: relative; word-wrap: break-word; }
.msg-right .bubble { background: #95ec69; color: #000; }
.hint-box { margin-top: 8px; padding-top: 8px; border-top: 1px dashed #eee; color: #e6a23c; font-size: 12px; }
.input-area { background: #f7f7f7; padding: 10px; border-top: 1px solid #ddd; }
</style>
