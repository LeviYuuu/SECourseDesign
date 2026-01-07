<template>
  <div class="report-container">
    <van-nav-bar title="评估报告" left-arrow @click-left="goBack" fixed placeholder />

    <div v-if="loading" class="loading-state">
      <van-loading size="36px" vertical>
        <div class="loading-text">AI 正在深度分析您的表现...</div>
        <div class="sub-text">约需 3-5 秒，请稍候</div>
      </van-loading>
    </div>

    <div v-else-if="report" class="report-content">
      <div class="score-card">
        <div class="score-num">{{ report.totalScore }}<span class="unit">分</span></div>
        <div class="score-label">综合表现评分</div>
      </div>

      <div class="chart-wrapper">
        <div class="chart-title">能力维度分析</div>
        <div ref="radarChart" style="width: 100%; height: 300px;"></div>
      </div>

      <van-collapse v-model="activeNames" class="suggestion-panel">
        <van-collapse-item title="🎯 改进建议" name="1" icon="fire-o">
          <div v-for="(sug, i) in report.suggestions" :key="i" class="sug-item">
            <van-tag type="warning" plain>{{ sug.category }}</van-tag>
            <p>{{ sug.suggestionText }}</p>
          </div>
        </van-collapse-item>
        
        <van-collapse-item title="💡 高情商改写示例" name="2" icon="gem-o">
          <div v-for="(ex, i) in report.rewriteExamples" :key="i" class="rewrite-card">
            <div class="orig-text">❌ {{ ex.before }}</div>
            <div class="better-text">✅ {{ ex.after }}</div>
          </div>
        </van-collapse-item>
      </van-collapse>

      <div class="footer-btn">
        <van-button block round type="primary" @click="goBack">返回首页</van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getReport } from '@/api';
import * as echarts from 'echarts';

const route = useRoute();
const router = useRouter();
const sessionId = Number(route.params.sessionId);

const loading = ref(true);
const report = ref<any>(null);
const activeNames = ref(['1', '2']);
const radarChart = ref<HTMLElement>();
let pollTimer: any = null;

// 初始化 ECharts 雷达图
const initChart = (dimensionScores: any[]) => {
  if (!radarChart.value) return;
  
  const myChart = echarts.init(radarChart.value);
  const indicators = dimensionScores.map(d => ({ name: d.dimensionCode, max: 10 }));
  const values = dimensionScores.map(d => d.score);

  const option = {
    radar: {
      indicator: indicators,
      shape: 'circle',
      splitArea: { areaStyle: { color: ['#fff', '#f5f5f5'] } }
    },
    series: [{
      type: 'radar',
      data: [{
        value: values,
        name: '能力维度',
        areaStyle: { color: 'rgba(25, 137, 250, 0.4)' },
        lineStyle: { color: '#1989fa' }
      }]
    }]
  };
  myChart.setOption(option);
};

// 获取数据逻辑（含轮询）
const fetchData = async () => {
  try {
    const res: any = await getReport(sessionId);
    // 只有状态是 READY 才展示，否则继续轮询
    if (res.status === 'READY') {
      report.value = res.report;
      loading.value = false;
      clearInterval(pollTimer);
      // DOM 更新后渲染图表
      nextTick(() => initChart(res.report.dimensionScores));
    } else if (res.status === 'FAILED') {
      loading.value = false;
      clearInterval(pollTimer);
      // 可以在这里处理失败提示
    }
  } catch (e) {
    console.error(e);
  }
};

onMounted(() => {
  fetchData();
  // 每 2 秒轮询一次，模拟异步等待报告生成
  pollTimer = setInterval(fetchData, 2000);
});

onUnmounted(() => clearInterval(pollTimer));

const goBack = () => router.push('/scenarios');
</script>

<style scoped>
.report-container { min-height: 100vh; background: #f7f8fa; padding-bottom: 30px; }
.loading-state { padding-top: 150px; text-align: center; }
.loading-text { margin-top: 20px; font-size: 16px; color: #333; font-weight: bold; }
.sub-text { margin-top: 8px; font-size: 12px; color: #999; }

.score-card { background: linear-gradient(135deg, #1989fa, #39b9f5); color: #fff; text-align: center; padding: 40px 0; }
.score-num { font-size: 56px; font-weight: bold; line-height: 1; }
.unit { font-size: 16px; margin-left: 4px; font-weight: normal; }
.score-label { margin-top: 10px; opacity: 0.9; }

.chart-wrapper { background: #fff; margin: 16px; border-radius: 12px; padding: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.chart-title { font-weight: bold; margin-bottom: 10px; border-left: 4px solid #1989fa; padding-left: 10px; }

.suggestion-panel { margin: 16px; border-radius: 12px; overflow: hidden; }
.sug-item { margin-bottom: 12px; border-bottom: 1px dashed #eee; padding-bottom: 12px; }
.sug-item p { font-size: 14px; color: #333; margin: 6px 0 0 0; line-height: 1.5; }

.rewrite-card { background: #f9f9f9; padding: 10px; border-radius: 6px; margin-bottom: 10px; font-size: 14px; }
.orig-text { color: #999; text-decoration: line-through; margin-bottom: 4px; }
.better-text { color: #07c160; font-weight: bold; }

.footer-btn { padding: 20px 30px; }
</style>
