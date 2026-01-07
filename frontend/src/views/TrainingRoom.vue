<template>
  <div class="room-container">
    <van-nav-bar 
      :title="`训练中 (第 ${currentRound} 轮)`" 
      left-arrow
      left-text="结束"
      @click-left="handleFinish"
      fixed 
      placeholder 
    />

    <div class="chat-list" ref="chatRef">
      <div v-for="(msg, index) in messages" :key="index" :class="['msg-row', msg.role === 'USER' ? 'msg-right' : 'msg-left']">
        <div class="avatar">{{ msg.role === 'USER' ? '我' : 'AI' }}</div>
        <div class="bubble">
          <div v-if="msg.audioUrl" class="voice-content">
            <van-icon name="volume-o" size="16" /> 
            <span>点击播放录音</span>
          </div>
          <div class="text-content">{{ msg.content }}</div>
        </div>
      </div>
      
      <div v-if="loading" class="msg-row msg-left">
        <div class="avatar">AI</div>
        <div class="bubble loading-bubble">
          <van-loading type="spinner" size="16px" color="#666" /> 正在思考追问...
        </div>
      </div>
    </div>

    <div class="input-area">
      <div class="tool-bar">
        <div class="icon-btn" @click="toggleMode">
          <van-icon :name="inputMode === 'TEXT' ? 'volume-o' : 'keyboard-o'" size="26" color="#333"/>
        </div>
        
        <div class="input-wrapper" v-if="inputMode === 'TEXT'">
          <input 
            v-model="inputText" 
            class="msg-input" 
            placeholder="请输入回答..." 
            @keyup.enter="sendText"
          />
        </div>

        <div class="input-wrapper" v-else>
          <button 
            class="voice-btn" 
            :class="{ recording: isRecording }"
            @mousedown="startRecord" 
            @mouseup="stopRecord"
            @touchstart.prevent="startRecord"
            @touchend.prevent="stopRecord"
          >
            {{ isRecording ? '松开 发送' : '按住 说话' }}
          </button>
        </div>

        <div class="send-btn" v-if="inputMode === 'TEXT'">
          <van-button size="small" type="primary" @click="sendText" :disabled="!inputText">发送</van-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { submitMessage, finishSession } from '@/api'; // 确保你的 api/index.ts 包含这些
import { showConfirmDialog, showToast } from 'vant';

const route = useRoute();
const router = useRouter();
const sessionId = Number(route.params.sessionId);

// 状态管理
const messages = ref<any[]>([
  { role: 'SYSTEM', content: '你好，我是你的面试官。请先做一个简单的自我介绍。' }
]);
const currentRound = ref(1);
const loading = ref(false);
const inputMode = ref<'TEXT'|'VOICE'>('TEXT');
const inputText = ref('');
const isRecording = ref(false);
const chatRef = ref<HTMLElement>();

// 自动滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (chatRef.value) {
      chatRef.value.scrollTop = chatRef.value.scrollHeight;
    }
  });
};

const toggleMode = () => {
  inputMode.value = inputMode.value === 'TEXT' ? 'VOICE' : 'TEXT';
};

// 1. 发送文本逻辑
const sendText = async () => {
  if (!inputText.value.trim()) return;
  
  // UI 乐观更新：先显示用户说的话
  const userMsg = inputText.value;
  messages.value.push({ role: 'USER', content: userMsg });
  inputText.value = '';
  scrollToBottom();

  await processResponse(userMsg);
};

// 2. 模拟录音逻辑
const startRecord = () => {
  isRecording.value = true;
  showToast('正在录音...');
};
const stopRecord = () => {
  isRecording.value = false;
  // 这里模拟 ASR 结果，实际应调用 uploadAudio 接口
  const mockAsrText = "（语音转文字结果）我觉得在这个项目中，我遇到的最大困难是并发处理...";
  messages.value.push({ role: 'USER', content: mockAsrText, audioUrl: 'mock_audio.mp3' });
  scrollToBottom();
  processResponse(mockAsrText);
};

// 3. 处理后端交互
const processResponse = async (userText: string) => {
  loading.value = true;
  try {
    // 调用 API 提交回答
    const res: any = await submitMessage(sessionId, {
      roundNo: currentRound.value,
      userText: userText
    });

    // 接收 AI 回复
    messages.value.push({ role: 'SYSTEM', content: res.nextQuestion });
    currentRound.value = res.currentRound;

    // 检查是否需要跳转报告页
    if (res.sessionStatus === 'PENDING_EVAL' || res.sessionStatus === 'TERMINATED') {
      showToast({ message: '训练结束，生成报告中...', duration: 2000 });
      setTimeout(() => router.replace(`/report/${sessionId}`), 1000);
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
    scrollToBottom();
  }
};

// 4. 手动结束训练
const handleFinish = () => {
  showConfirmDialog({
    title: '结束训练',
    message: '确定要提前结束本次对练并生成报告吗？'
  }).then(async () => {
    await finishSession(sessionId);
    router.replace(`/report/${sessionId}`);
  }).catch(() => {});
};
</script>

<style scoped>
.room-container { height: 100vh; display: flex; flex-direction: column; background: #ededed; }

.chat-list { flex: 1; overflow-y: auto; padding: 16px; display: flex; flex-direction: column; gap: 16px; padding-bottom: 20px; }

.msg-row { display: flex; width: 100%; }
.msg-left { justify-content: flex-start; }
.msg-right { justify-content: flex-end; }

.avatar { width: 40px; height: 40px; border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 12px; color: #fff; flex-shrink: 0; }
.msg-left .avatar { background: #ff976a; margin-right: 10px; }
.msg-right .avatar { background: #1989fa; margin-left: 10px; order: 2; }

.bubble { max-width: 70%; padding: 10px 14px; border-radius: 8px; font-size: 15px; line-height: 1.5; word-wrap: break-word; position: relative; }
.msg-left .bubble { background: #fff; color: #333; }
.msg-right .bubble { background: #95ec69; color: #000; } /* 类似微信的绿色 */

.loading-bubble { display: flex; align-items: center; gap: 8px; color: #999; font-size: 13px; }
.voice-content { font-size: 13px; opacity: 0.8; margin-bottom: 4px; display: flex; align-items: center; gap: 4px; }

/* 底部输入框样式 */
.input-area { background: #f7f7f7; padding: 8px 10px; border-top: 1px solid #dcdcdc; padding-bottom: env(safe-area-inset-bottom); }
.tool-bar { display: flex; align-items: center; height: 40px; gap: 10px; }
.input-wrapper { flex: 1; height: 100%; }

.msg-input { width: 100%; height: 100%; border: none; border-radius: 4px; padding: 0 10px; background: #fff; box-sizing: border-box; }
.msg-input:focus { outline: none; }

.voice-btn { width: 100%; height: 100%; border: 1px solid #ddd; background: #fff; border-radius: 4px; font-weight: bold; color: #333; }
.voice-btn:active, .voice-btn.recording { background: #ddd; color: #000; }
</style>
