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
import { closeToast } from 'vant'; // 👈 引入 closeToast
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
  const indicators = report.value.dimensionScores.map((d: any) => ({ name: d.name, max: 10 }));
  const values = report.value.dimensionScores.map((d: any) => d.score);
  
  myChart.setOption({
    radar: { 
      indicator: indicators,
      radius: '65%'
    },
    series: [{ 
      type: 'radar', 
      data: [{ value: values, name: '能力维度' }],
      areaStyle: { opacity: 0.2, color: '#1989fa' },
      lineStyle: { color: '#1989fa' }
    }]
  });
};

const fetchData = async () => {
  try {
    const res: any = await getReport({ sessionId });
    report.value = res;
    loading.value = false;
    clearInterval(pollTimer);
    nextTick(() => initChart());
  } catch (e) {
    // 继续轮询
  }
};

onMounted(() => {
  // 🔴 核心修复：进入页面时，强制关闭可能残留的全局 Loading
  closeToast();
  
  fetchData();
  pollTimer = setInterval(fetchData, 2000);
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
