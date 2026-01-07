import request from '@/utils/request';

// --- 开关控制 ---
const USE_MOCK = true; // ⚠️设置为 false 时连接真实后端，设置为 true 时使用模拟数据

// 辅助函数：模拟网络延迟 (让体验更真实，像真的在请求服务器)
const mockResponse = (data: any, time = 500) => {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(data); // 直接返回 data，模拟 request 拦截器处理后的结果
    }, time);
  });
};

// --- UC07 登录 ---
export const login = (data: any) => {
  if (USE_MOCK) {
    return mockResponse({
      token: "mock_token_eyJh...",
      user: { userId: 1001, nickname: "同学A", avatar: "" }
    });
  }
  return request({ url: '/auth/login', method: 'post', data });
};

// --- UC01 场景列表 ---
export const getScenarios = () => {
  if (USE_MOCK) {
    return mockResponse({
      total: 2,
      items: [
        {
          scenarioId: 1,
          title: "互联网大厂面试",
          rolePersona: "严厉的面试官",
          difficulty: "L2",
          description: "模拟字节/阿里后端岗面试，考察并发与数据库。",
          rounds: 6
        },
        {
          scenarioId: 2,
          title: "向导师汇报进度",
          rolePersona: "温和的导师",
          difficulty: "L1",
          description: "汇报本周的论文阅读情况，解释进度滞后的原因。",
          rounds: 4
        }
      ]
    });
  }
  return request({ url: '/scenarios', method: 'get', params: { page: 1, pageSize: 50 } });
};

export const createSession = (data: any) => {
  if (USE_MOCK) {
    return mockResponse({
      sessionId: 9999, // 假 ID
      sessionStatus: "IN_PROGRESS",
      currentRound: 0
    });
  }
  return request({ url: '/sessions', method: 'post', data });
};

// --- UC03 对练核心 (重点模拟) ---
export const submitMessage = (sessionId: number, data: any) => {
  if (USE_MOCK) {
    // 模拟 AI 的回复逻辑
    const round = data.roundNo + 1;
    const isFinished = round >= 6; // 假设 6 轮结束
    
    return mockResponse({
      nextQuestion: `(模拟AI回复) 你刚才提到的观点很有趣，但是在高并发场景下，如何保证数据一致性呢？这是第 ${round} 轮追问。`,
      currentRound: round,
      sessionStatus: isFinished ? "PENDING_EVAL" : "IN_PROGRESS"
    }, 1500); // 延迟 1.5秒，模拟 AI 思考
  }
  return request({ url: `/sessions/${sessionId}/messages`, method: 'post', data });
};

export const finishSession = (sessionId: number) => {
  if (USE_MOCK) {
    return mockResponse({});
  }
  return request({ url: `/sessions/${sessionId}/finish`, method: 'post' });
};

// --- UC02 语音 ---
export const uploadAudio = (sessionId: number, formData: FormData) => {
  if (USE_MOCK) {
    return mockResponse({
      audioUrl: "https://www.w3schools.com/html/horse.mp3" // 一个公网可访问的音频 demo
    });
  }
  return request({ 
    url: `/sessions/${sessionId}/audio`, 
    method: 'post', 
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

// --- UC04/05 报告 ---
export const getReport = (sessionId: number) => {
  if (USE_MOCK) {
    // 模拟 30% 概率正在生成中，70% 概率生成完毕
    const isReady = Math.random() > 0.3; 
    
    if (!isReady) {
       // 模拟后端返回“任务还在跑”
       // 注意：request 拦截器里如果不符合 code=0 会抛错，这里为了演示简单，直接模拟成功返回但 status 还是 RUNNING
       return mockResponse({ status: "RUNNING", report: null });
    }

    return mockResponse({
      status: "READY",
      report: {
        totalScore: 85,
        dimensionScores: [
          { dimensionCode: "逻辑性", score: 8.5 },
          { dimensionCode: "流畅度", score: 7.0 },
          { dimensionCode: "情感控制", score: 9.0 },
          { dimensionCode: "词汇量", score: 6.5 },
          { dimensionCode: "反应速度", score: 8.0 },
          { dimensionCode: "切题度", score: 9.5 }
        ],
        suggestions: [
          { category: "逻辑性", suggestionText: "建议在回答时先说结论，再分三点阐述，避免逻辑跳跃。" },
          { category: "语速", suggestionText: "你的语速稍快，容易吃字，建议放慢节奏，给自己思考时间。" }
        ],
        rewriteExamples: [
          { before: "我不知道这个怎么做...", after: "关于这个技术细节，我目前涉猎较少，但我可以尝试从原理角度分析一下..." }
        ]
      }
    });
  }
  return request({ url: `/reports/${sessionId}`, method: 'get' });
};
// --- UC06 档案与成长曲线 ---

// 1. 获取历史训练记录列表
export const getHistoryList = (params?: any) => {
  if (USE_MOCK) {
    return mockResponse({
      total: 5,
      items: [
        { sessionId: 101, title: '互联网大厂面试', score: 85, date: '2025-12-28', difficulty: 'L2' },
        { sessionId: 102, title: '向导师汇报进度', score: 72, date: '2025-12-27', difficulty: 'L1' },
        { sessionId: 103, title: '小组作业争论', score: 68, date: '2025-12-25', difficulty: 'L2' },
        { sessionId: 104, title: '互联网大厂面试', score: 60, date: '2025-12-20', difficulty: 'L3' },
        { sessionId: 105, title: '社团招新宣讲', score: 78, date: '2025-12-18', difficulty: 'L1' },
      ]
    });
  }
  return request({ url: '/history', method: 'get', params });
};

// 2. 获取成长曲线数据 (近7次或近30天均分)
export const getUserGrowth = () => {
  if (USE_MOCK) {
    return mockResponse({
      dates: ['12-18', '12-20', '12-25', '12-27', '12-28'],
      scores: [78, 60, 68, 72, 85], // 模拟一个波动上升的趋势
      feedback: "您的表达逻辑性显著提升，但在高压场景下仍需注意语速控制。"
    });
  }
  // 规约中未显式定义此聚合接口，通常由 /history 数据前端处理或新增 analytics 接口
  // 这里假设后端提供了一个聚合接口
  return request({ url: '/analytics/growth', method: 'get' });
};
