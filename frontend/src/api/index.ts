import request from '@/utils/request';

// ==========================================
// 1. 认证服务 (User/Auth)
// ==========================================
// ✅ 修改：指向 /user/login
export const login = (data: { username: string; password?: string }) => {
  return request({ url: '/user/login', method: 'post', data });
};

// ✅ 修改：指向 /user/register
export const register = (data: any) => {
  return request({ url: '/user/register', method: 'post', data });
};

// ==========================================
// 2. 场景服务 (Scenario)
// ==========================================
// 保持不变，后端也是 /scenario/list
export const getScenarios = (params: any = {}) => {
  return request({ url: '/scenario/list', method: 'get', params });
};

// ==========================================
// 3. 会话服务 (Session/Chat)
// ==========================================
// 保持不变
export const createSession = (data: any) => {
  return request({ url: '/session/create', method: 'post', data });
};

export const getNextQuestion = (data: any) => {
  return request({
    url: '/dialogue/next',
    method: 'post',
    data
  });
};

export function generateTTS(data: { text: string; sessionId?: string }) {
  return request({
    url: '/speech/tts',
    method: 'post',
    data
  });
}

// 生成/提交评估
export function submitEvaluation(data: { sessionId: string }) {
  return request({
    url: '/evaluation/submit',
    method: 'post',
    data
  });
}

// 获取评估报告
export function getEvaluationReport(sessionId: string) {
  return request({
    url: `/evaluation/report/${sessionId}`,
    method: 'get'
  });
}

export const getReport = (params: { sessionId: string }) => {
  return request({ url: '/report/detail', method: 'get', params });
};

// ==========================================
// 5. 档案服务 (Profile Subsystem) 👈 [之前缺失的部分]
// ==========================================
// 获取成长趋势
export const getGrowthTrend = (params: { userId: number; days: number }) => {
  return request({ url: '/profile/trend', method: 'get', params });
};

// 获取历史记录
export const getHistoryList = (params: { userId: number; page?: number; size?: number }) => {
  return request({ url: '/profile/history', method: 'get', params });
};

// ==========================================
// 6. 语音与风控服务 (Speech & Risk Subsystem)
// ==========================================
export const transcribeAudio = (data: FormData) => {
  return request({
    url: '/speech/transcribe',
    method: 'post',
    data: data, // 直接传递 FormData 对象
    headers: {
      'Content-Type': 'multipart/form-data' 
    }
  });
};

export const checkRisk = (data: { content: string }) => {
  return request({ url: '/risk/check', method: 'post', data });
};
