import request from '@/utils/request';

// ✅ 关闭 Mock，连接真实后端
const USE_MOCK = false;

// ==========================================
// 1. 认证服务 (Auth Subsystem)
// ==========================================
// 对应后端 AuthController
export const login = (data: { username: string; password?: string }) => {
  return request({ url: '/auth/login', method: 'post', data });
};

export const register = (data: any) => {
  return request({ url: '/auth/register', method: 'post', data });
};

// ==========================================
// 2. 场景服务 (Scenario Subsystem)
// ==========================================
export const getScenarios = (params: any = {}) => {
  return request({ url: '/scenario/list', method: 'get', params });
};

// ==========================================
// 3. 会话服务 (Session/Dialogue Subsystem)
// ==========================================
// 创建会话
export const createSession = (data: any) => {
  return request({ url: '/session/create', method: 'post', data });
};

// 获取下一轮问题
export const getNextQuestion = (data: any) => {
  return request({ url: '/dialogue/next', method: 'post', data });
};

// ==========================================
// 4. 评估服务 (Evaluation Subsystem)
// ==========================================
export const submitEvaluation = (data: { sessionId: string }) => {
  return request({ url: '/evaluation/submit', method: 'post', data });
};

export const getReport = (params: { sessionId: string }) => {
  return request({ url: '/evaluation/report', method: 'get', params });
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
  return request({ url: '/speech/transcribe', method: 'post', data });
};

export const checkRisk = (data: { content: string }) => {
  return request({ url: '/risk/check', method: 'post', data });
};
