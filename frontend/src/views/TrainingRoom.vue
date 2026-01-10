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
        <van-button 
          block 
          :type="recording ? 'danger' : 'primary'" 
          @touchstart.prevent="startRecord" 
          @touchend.prevent="stopRecord"
          @mousedown.prevent="startRecord" 
          @mouseup.prevent="stopRecord"
        >
          {{ recording ? '松开 发送' : '按住 说话' }}
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

// ----------------------------------------------------------------------
// 1. 状态定义
// ----------------------------------------------------------------------
const route = useRoute();
const router = useRouter();
const sessionId = route.params.sessionId as string;

const messages = ref<any[]>([
  { role: 'SYSTEM', content: '你好，我是面试官。请开始你的自我介绍。' } 
]);
const currentRound = ref(1);
const totalRounds = ref(10); // 默认 10，后续根据接口更新
const inputText = ref('');
const loading = ref(false);
const chatRef = ref<HTMLElement>();
const inputMode = ref<'TEXT' | 'VOICE'>('TEXT'); 
const recording = ref(false);

// 录音相关对象
let mediaRecorder: MediaRecorder | null = null;
let audioChunks: Blob[] = [];

// ----------------------------------------------------------------------
// 2. 核心逻辑：发送消息与处理响应
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
// 3. 录音功能实现 (补全缺失部分)
// ----------------------------------------------------------------------
const startRecord = async () => {
  try {
    // 请求麦克风权限
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    mediaRecorder = new MediaRecorder(stream);
    audioChunks = [];

    // 收集音频数据
    mediaRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        audioChunks.push(event.data);
      }
    };

    // 录音结束后的回调
    mediaRecorder.onstop = handleAudioUpload;

    mediaRecorder.start();
    recording.value = true;
    showToast({ message: '正在录音...', type: 'loading', duration: 0, forbidClick: true });
  } catch (err) {
    console.error("无法启动录音", err);
    showToast("无法访问麦克风");
  }
};

const stopRecord = () => {
  if (mediaRecorder && recording.value) {
    mediaRecorder.stop();
    recording.value = false;
    // 关闭所有轨道以释放麦克风
    mediaRecorder.stream.getTracks().forEach(track => track.stop());
    closeToast();
  }
};

const handleAudioUpload = async () => {
  if (audioChunks.length === 0) return;
  
  // 创建 WAV 或 WebM Blob
  const audioBlob = new Blob(audioChunks, { type: 'audio/wav' });
  const formData = new FormData();
  formData.append('audioFile', audioBlob, 'voice.wav'); 
  formData.append('sessionId', sessionId);
  
  // 注意：userId 应该动态获取，这里示例写死或从 Store 获取
  formData.append('userId', '1001'); 

  try {
    showToast({ message: '识别中...', type: 'loading', duration: 0 });
    const res: any = await transcribeAudio(formData);
    closeToast();
    
    // 识别成功后，将文本发送给对话逻辑
    if (res && res.transcript) {
      handleConversation(res.transcript);
    } else {
      showToast('未识别到有效内容');
    }
  } catch (error) {
    console.error(error);
    closeToast();
    showToast('语音识别失败');
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

onMounted(() => {
  scrollToBottom();
});

// 组件销毁时清理录音资源
onUnmounted(() => {
  if (mediaRecorder && mediaRecorder.state !== 'inactive') {
    mediaRecorder.stop();
  }
});
</script>

<style scoped>
.room-container { height: 100vh; display: flex; flex-direction: column; background: #f7f8fa; }
/* 这里的 margin-top 必须对应 nav-bar 的高度，vant nav-bar 默认约 46px */
.chat-list { flex: 1; overflow-y: auto; padding: 15px; padding-bottom: 20px; margin-top: 46px; } 
.msg-row { display: flex; margin-bottom: 15px; }
.msg-left { justify-content: flex-start; }
.msg-right { justify-content: flex-end; }
.avatar { width: 40px; height: 40px; background: #ddd; border-radius: 50%; text-align: center; line-height: 40px; font-size: 12px; flex-shrink: 0; }
.msg-left .avatar { margin-right: 10px; background: #1989fa; color: white; }
.msg-right .avatar { margin-left: 10px; background: #07c160; color: white; order: 2; }
.bubble { max-width: 70%; padding: 10px 14px; border-radius: 8px; font-size: 15px; line-height: 1.5; background: #fff; word-break: break-all; }
.msg-right .bubble { background: #95ec69; }
.hint-box { margin-top: 8px; font-size: 12px; color: #666; background: #f0f0f0; padding: 5px; border-radius: 4px; }
.input-area { background: #fff; padding: 10px; display: flex; align-items: center; border-top: 1px solid #ebedf0; }
.voice-btn-wrapper { flex: 1; }
</style>
