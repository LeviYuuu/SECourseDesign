<template>
  <div class="report-page">
    <van-nav-bar title="评估报告" left-arrow @click-left="$router.push('/scenarios')" fixed placeholder />

    <div v-if="loading" class="loading-box">
      <van-loading vertical color="#1989fa">AI 正在生成报告...</van-loading>
    </div>

    <div v-else-if="report" class="content">
      <div class="score-header">
        <div class="score">{{ report.totalScore }}</div>
        <div class="label">综合得分</div>
      </div>

      <div class="chart-box" ref="radarRef" style="width: 100%; height: 300px;"></div>

      <van-collapse v-model="activeNames" class="panel">
        <van-collapse-item title="🌟 你的优势" name="1">
          <div v-if="report.strengths && report.strengths.length > 0">
             <van-tag v-for="(tag, i) in report.strengths" :key="i" type="success" size="medium" style="margin: 0 5px 5px 0;">
              {{ tag }}
            </van-tag>
          </div>
          <div v-else class="empty-tip">暂无数据</div>
        </van-collapse-item>

        <van-collapse-item title="🎯 改进建议" name="2">
          <div v-for="(item, i) in report.suggestions" :key="i" class="sug-item">
            <div class="sug-action">{{ i+1 }}. {{ item.action }}</div>
            <div class="sug-row"><span>原因：</span>{{ item.why }}</div>
            <div class="sug-row"><span>方法：</span>{{ item.how }}</div>
          </div>
        </van-collapse-item>
      </van-collapse>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted } from 'vue';
import { useRoute } from 'vue-router';
import { getReport } from '@/api';
import { closeToast, showToast } from 'vant';
import * as echarts from 'echarts';

const route = useRoute();
const sessionId = route.params.sessionId as string;
const loading = ref(true);
const report = ref<any>(null);
const activeNames = ref(['1', '2']);
const radarRef = ref<HTMLElement>();
let pollTimer: any = null;

const initChart = () => {
  if (!radarRef.value || !report.value) return;
  echarts.dispose(radarRef.value);

  const myChart = echarts.init(radarRef.value);
  
  // ✅ 处理维度评分数据，确保有默认值
  let dimensionScores = [];
  if (report.value.dimensionScores && Array.isArray(report.value.dimensionScores)) {
    dimensionScores = report.value.dimensionScores;
  } else {
    // 默认的维度数据（测试用）
    dimensionScores = [
      { name: '沟通表达', score: 7 },
      { name: '逻辑思维', score: 8 },
      { name: '应变能力', score: 6 },
      { name: '情绪管理', score: 7 },
      { name: '专业知识', score: 8 }
    ];
  }
  
  const indicators = dimensionScores.map((d: any) => ({ name: d.name, max: 10 }));
  const values = dimensionScores.map((d: any) => d.score);
  
  myChart.setOption({
    radar: { 
      indicator: indicators,
      radius: '65%',
      splitNumber: 5,
      axisName: {
        color: '#333'
      },
      splitArea: {
        areaStyle: {
          color: ['rgba(25,137,250,0.05)', 'rgba(25,137,250,0.02)']
        }
      }
    },
    series: [{ 
      type: 'radar', 
      data: [{ value: values, name: '能力维度' }],
      areaStyle: { opacity: 0.2, color: '#1989fa' },
      lineStyle: { color: '#1989fa', width: 2 },
      itemStyle: { color: '#1989fa' },
      symbolSize: 6
    }]
  });
};

const fetchData = async () => {
  try {
    console.log('获取报告数据，sessionId:', sessionId);
    const res: any = await getReport({ sessionId });
    console.log('报告数据:', res);
    
    if (res) {
      report.value = res;
      loading.value = false;
      clearInterval(pollTimer);
      nextTick(() => initChart());
    } else {
      // 如果报告不存在，继续轮询
      console.log('报告尚未生成，继续轮询...');
    }
  } catch (e: any) {
    console.error("获取报告失败:", e.message);
    // 如果是404错误（报告不存在），继续轮询
    if (e.message.includes('404')) {
      console.log('报告未生成，继续轮询...');
    } else {
      // 其他错误停止轮询
      clearInterval(pollTimer);
      loading.value = false;
      showToast('获取报告失败');
    }
  }
};

onMounted(() => {
  closeToast();
  
  fetchData();
  // 轮询间隔调整为3秒
  pollTimer = setInterval(fetchData, 3000);
});

onUnmounted(() => clearInterval(pollTimer));
</script>

<style scoped>
/* 样式保持不变 */
.report-page { min-height: 100vh; background: #f7f8fa; padding-bottom: 20px; }
.loading-box { padding-top: 100px; text-align: center; }
.score-header { background: #1989fa; color: #fff; text-align: center; padding: 30px; }
.score { font-size: 48px; font-weight: bold; }
.chart-box { background: #fff; margin-bottom: 10px; }
.panel { margin-top: 10px; }
.sug-item { margin-bottom: 15px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
.sug-action { font-weight: bold; color: #333; margin-bottom: 6px; }
.sug-row { font-size: 13px; color: #666; }
.sug-row span { color: #999; margin-right: 5px; }
.empty-tip { color: #999; font-size: 12px; }
</style>
