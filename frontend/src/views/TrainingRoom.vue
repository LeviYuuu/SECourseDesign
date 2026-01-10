<template>
  <div class="room-container">
    <van-nav-bar 
      :title="`第 ${currentRound} ${totalRounds > 0 ? '/ ' + totalRounds : ''} 轮`" 
      left-text="结束" 
      @click-left="manualFinish" 
      fixed 
      placeholder 
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
        <div class="bubble">
          <van-loading type="spinner" size="16px" /> 思考中...
        </div>
      </div>
    </div>
    
    <div class="input-area">
      <van-icon 
        :name="inputMode === 'TEXT' ? 'volume-o' : 'comment-o'" 
        size="28" 
        color="#1989fa"
        style="margin-right: 10px;"
        @click="toggleMode"
      />

      <van-field 
        v-if="inputMode === 'TEXT'"
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

      <div v-else class="voice-btn-wrapper">
        <!-- 优化语音按钮：长按开始，松开结束 -->
        <van-button 
          block 
          :type="recording ? 'danger' : 'primary'" 
          @pointerdown="handlePointerDown"
          @pointerup="handlePointerUp"
          @pointerleave="handlePointerLeave"
          @touchstart.prevent
          @touchend.prevent
        >
          {{ recording ? '松开 停止' : '长按 说话' }}
          <div v-if="recording" class="recording-indicator">
            <span class="pulse"></span>
            录音中...
          </div>
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted, onUnmounted } from'vue';
import { useRoute, useRouter } from'vue-router';
import { showConfirmDialog, showToast, closeToast } from'vant';
import { getNextQuestion, submitEvaluation, transcribeAudio, generateTTS } from '@/api';
import { useUserStore } from '@/stores/user';

// ----------------------------------------------------------------------
// 1. 状态定义
// ----------------------------------------------------------------------
const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const sessionId = route.params.sessionId as string;

const messages = ref<any[]>([
  { role: 'SYSTEM', content: '你好，我是面试官。请开始你的自我介绍。' } 
]);
const currentRound = ref(1);
const totalRounds = ref(10);
const inputText = ref('');
const loading = ref(false);
const chatRef = ref<HTMLElement>();
const inputMode = ref<'TEXT' | 'VOICE'>('TEXT'); 
const recording = ref(false);
const recordingTimer = ref<NodeJS.Timeout | null>(null);
const recordingDuration = ref(0);

// 录音相关对象
let mediaRecorder: MediaRecorder | null = null;
let audioChunks: Blob[] = [];

// ----------------------------------------------------------------------
// 2. 对话处理核心逻辑
// ----------------------------------------------------------------------

// 统一处理对话逻辑（无论是文本还是语音转出的文本）
const handleConversation = async (content: string) => {
  if (!content.trim() || loading.value) return;

  // 1. 用户消息上屏
  messages.value.push({ role: 'USER', content: content });
  inputText.value = ''; 
  loading.value = true;
  scrollToBottom();

  try {
    // 2. 调用后端接口
    const res: any = await getNextQuestion({
      sessionId: sessionId,
      content: content 
    });

    // 调试：查看后端返回
    console.log("Backend Response:", res);

    // 3. 字段兼容处理
    // 优先读取 content，其次 question/text，最后兜底
    const aiText = res.content || res.question || res.text || "AI 回复内容为空";
    const isEnd = res.isEnd === true; // 确保是布尔值
    const roundNo = res.round || res.currentRound;
    const total = res.totalRounds;

    // 4. AI 消息上屏
    messages.value.push({ 
      role: 'AI', 
      content: aiText,
      hint: res.hint 
    });

    // 5. 更新状态
    if (roundNo) currentRound.value = roundNo;
    if (total) totalRounds.value = total;

    // 6. 播放 TTS (即便要结束了，也先把这句话读完)
    await playAiAudio(aiText);

    // 7. 结束判断逻辑 (核心修复点)
    // 触发条件：后端明确返回 isEnd，或者当前轮次 >= 总轮次
    if (isEnd || (totalRounds.value > 0 && currentRound.value >= totalRounds.value)) {
      showToast({ message: '本轮训练结束，正在生成报告...', type: 'success', duration: 2000 });
      
      loading.value = true; // 保持加载状态，防止用户重复点击

      // 延迟跳转，给 TTS 一点播放时间
      setTimeout(() => {
        submitTask();
      }, 2500);
      return;
    }

  } catch (error) {
    console.error('对话请求失败', error);
    showToast('AI 响应失败');
  } finally {
    loading.value = false;
    scrollToBottom();
  }
};

// 文本发送入口
const sendText = () => {
  handleConversation(inputText.value);
};

// 播放 TTS 音频
const playAiAudio = async (text: string) => {
  if (!text) return;
  try {
    const res: any = await generateTTS({ text, sessionId });
    // 兼容：可能是直接返回 URL 字符串，也可能是 { audioUrl: '...' }
    const relativeUrl = typeof res === 'string' ? res : (res.audioUrl || (res.data && res.data.audioUrl));
    
    if (relativeUrl) {
      // 拼接完整路径 (如果 relativeUrl 已经是 http 开头则无需拼接)
      const fullUrl = relativeUrl.startsWith('http') ? relativeUrl : `http://localhost:8080${relativeUrl}`;
      const audio = new Audio(fullUrl);
      await audio.play();
    }
  } catch (e) {
    console.error("TTS 播放异常", e);
  }
};

// ----------------------------------------------------------------------
// 3. 优化的录音控制逻辑
// ----------------------------------------------------------------------

// 处理指针按下（开始录音）
const handlePointerDown = async (event: PointerEvent) => {
  if (recording.value || loading.value) return;
  
  event.preventDefault();
  recording.value = true;
  recordingDuration.value = 0;
  
  // 开始计时
  recordingTimer.value = setInterval(() => {
    recordingDuration.value++;
    // 最长录音时间60秒，自动停止
    if (recordingDuration.value >= 60) {
      handlePointerUp();
    }
  }, 1000);
  
  try {
    // 请求麦克风权限
    const stream = await navigator.mediaDevices.getUserMedia({ 
      audio: {
        echoCancellation: true,
        noiseSuppression: true,
        sampleRate: 16000
      } 
    });
    
    mediaRecorder = new MediaRecorder(stream, {
      mimeType: 'audio/webm;codecs=opus',
      audioBitsPerSecond: 128000
    });
    
    audioChunks = [];

    // 收集音频数据
    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        audioChunks.push(event.data);
      }
    };

    // 录音结束后的回调
    mediaRecorder.onstop = handleAudioUpload;
    
    // 录音错误处理
    mediaRecorder.onerror = (error) => {
      console.error("录音错误:", error);
      showToast("录音失败");
      cleanupRecording();
    };

    // 开始录音
    mediaRecorder.start(100); // 每100ms收集一次数据
    
  } catch (err: any) {
    console.error("无法启动录音", err);
    showToast(err.name === 'NotAllowedError' ? "请允许麦克风权限" : "无法访问麦克风");
    cleanupRecording();
  }
};

// 处理指针抬起（停止录音）
const handlePointerUp = (event?: PointerEvent) => {
  event?.preventDefault();
  stopRecording();
};

// 处理指针离开（停止录音，防止拖拽出去）
const handlePointerLeave = (event: PointerEvent) => {
  if (recording.value) {
    event.preventDefault();
    stopRecording();
  }
};

// 停止录音的通用方法
const stopRecording = () => {
  if (mediaRecorder && recording.value) {
    // 停止录音
    mediaRecorder.stop();
    
    // 停止定时器
    if (recordingTimer.value) {
      clearInterval(recordingTimer.value);
      recordingTimer.value = null;
    }
    
    // 立即清理流，防止内存泄漏
    cleanupStream();
    
    // 显示录音时长
    if (recordingDuration.value > 0) {
      console.log(`录音时长: ${recordingDuration.value}秒`);
    }
  }
};

// 清理录音流
const cleanupStream = () => {
  if (mediaRecorder) {
    const stream = mediaRecorder.stream;
    if (stream) {
      stream.getTracks().forEach(track => {
        track.stop();
        stream.removeTrack(track);
      });
    }
  }
};

// 清理录音状态
const cleanupRecording = () => {
  recording.value = false;
  recordingDuration.value = 0;
  
  if (recordingTimer.value) {
    clearInterval(recordingTimer.value);
    recordingTimer.value = null;
  }
  
  cleanupStream();
};

// 处理音频上传
const handleAudioUpload = async () => {
  if (audioChunks.length === 0) {
    // 录音时间太短，不处理
    if (recordingDuration.value < 1) {
      showToast("录音时间太短");
    }
    cleanupRecording();
    return;
  }
  
  // 创建音频Blob
  const audioBlob = new Blob(audioChunks, { type: 'audio/webm;codecs=opus' });
  
  // 检查音频大小（过小可能是无效录音）
  if (audioBlob.size < 1024) { // 小于1KB
    showToast("未检测到有效语音");
    cleanupRecording();
    return;
  }
  
  const formData = new FormData();
  formData.append('audioFile', audioBlob, `voice_${Date.now()}.webm`);
  formData.append('sessionId', sessionId);
  
  // 从store获取用户ID
  const userId = userStore.userInfo.userId?.toString() || '1001';
  formData.append('userId', userId);
  
  try {
    showToast({ message: '识别中...', type: 'loading', duration: 0 });
    const res: any = await transcribeAudio(formData);
    closeToast();
    
    // 识别成功后，将文本发送给对话逻辑
    if (res && res.transcript) {
      handleConversation(res.transcript);
    } else if (res && res.text) {
      handleConversation(res.text);
    } else {
      showToast('未识别到有效内容');
    }
  } catch (error: any) {
    console.error('语音识别失败:', error);
    closeToast();
    showToast('语音识别失败: ' + (error.message || '网络错误'));
  } finally {
    cleanupRecording();
  }
};

// ----------------------------------------------------------------------
// 4. 其他辅助逻辑
// ----------------------------------------------------------------------
const scrollToBottom = () => nextTick(() => {
  if (chatRef.value) {
    chatRef.value.scrollTop = chatRef.value.scrollHeight;
  }
});

const toggleMode = () => {
  inputMode.value = inputMode.value === 'TEXT' ? 'VOICE' : 'TEXT';
};

const manualFinish = () => {
  showConfirmDialog({ title: '确认结束', message: '是否提前结束训练并生成报告？' })
    .then(submitTask)
    .catch(() => {});
};

const submitTask = async () => {
  try {
    showToast({ message: '生成报告中...', type: 'loading', duration: 0, forbidClick: true });
    await submitEvaluation({ sessionId });
    closeToast();
    // 跳转报告页
    router.replace(`/report/${sessionId}`);
  } catch (e) {
    closeToast();
    console.error(e);
    // 即使提交接口报错，也尝试跳转，或者提示用户
    showToast('提交异常，尝试跳转...');
    setTimeout(() => router.replace(`/report/${sessionId}`), 1000);
  }
};

const initRoom = async () => {
  scrollToBottom();
};

onMounted(() => {
  initRoom();
});

// 组件销毁时清理资源
onUnmounted(() => {
  cleanupRecording();
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop();
  }
});
</script>

<style scoped>
.room-container { 
  height: 100vh; 
  display: flex; 
  flex-direction: column; 
  background: #f7f8fa; 
}

/* 这里的 margin-top 必须对应 nav-bar 的高度，vant nav-bar 默认约 46px */
.chat-list { 
  flex: 1; 
  overflow-y: auto; 
  padding: 15px; 
  padding-bottom: 20px; 
  margin-top: 46px; 
} 

.msg-row { 
  display: flex; 
  margin-bottom: 15px; 
}

.msg-left { 
  justify-content: flex-start; 
}

.msg-right { 
  justify-content: flex-end; 
}

.avatar { 
  width: 40px; 
  height: 40px; 
  background: #ddd; 
  border-radius: 50%; 
  text-align: center; 
  line-height: 40px; 
  font-size: 12px; 
  flex-shrink: 0; 
}

.msg-left .avatar { 
  margin-right: 10px; 
  background: #1989fa; 
  color: white; 
}

.msg-right .avatar { 
  margin-left: 10px; 
  background: #07c160; 
  color: white; 
  order: 2; 
}

.bubble { 
  max-width: 70%; 
  padding: 10px 14px; 
  border-radius: 8px; 
  font-size: 15px; 
  line-height: 1.5; 
  background: #fff; 
  word-break: break-all; 
}

.msg-right .bubble { 
  background: #95ec69; 
}

.hint-box { 
  margin-top: 8px; 
  font-size: 12px; 
  color: #666; 
  background: #f0f0f0; 
  padding: 5px; 
  border-radius: 4px; 
}

.input-area { 
  background: #fff; 
  padding: 10px; 
  display: flex; 
  align-items: center; 
  border-top: 1px solid #ebedf0; 
}

.voice-btn-wrapper { 
  flex: 1; 
  position: relative; 
}

.recording-indicator {
  margin-top: 5px;
  font-size: 12px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.pulse {
  width: 10px;
  height: 10px;
  background-color: #ff4444;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    transform: scale(0.8);
    opacity: 0.7;
  }
  50% {
    transform: scale(1.2);
    opacity: 1;
  }
  100% {
    transform: scale(0.8);
    opacity: 0.7;
  }
}

/* 优化触摸反馈 */
.van-button--primary:active {
  opacity: 0.8;
}

.van-button--danger:active {
  opacity: 0.8;
}
</style>