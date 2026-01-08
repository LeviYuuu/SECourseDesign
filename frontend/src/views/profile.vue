<template>
  <div class="profile-page">
    <van-nav-bar title="个人档案" left-arrow @click-left="$router.back()" fixed placeholder />
    
    <div class="chart-section">
      <div class="sec-title">近7次能力趋势</div>
      <div class="chart" ref="lineRef" style="width: 100%; height: 250px;"></div>
    </div>

    <div class="list-section">
      <div class="sec-title">训练历史</div>
      <van-cell-group v-if="list.length > 0">
        <van-cell v-for="item in list" :key="item.sessionId" 
          :title="item.scenario" 
          :value="item.score + '分'" 
          :label="item.completedAt"
          is-link
          @click="$router.push(`/report/${item.sessionId}`)"
        />
      </van-cell-group>
      <div v-else class="empty-list">暂无历史记录</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { getGrowthTrend, getHistoryList } from '@/api';
import * as echarts from 'echarts';

const lineRef = ref<HTMLElement>();
const list = ref<any[]>([]);

const initChart = (labels: string[], scores: number[]) => {
  if (!lineRef.value) return;
  echarts.dispose(lineRef.value); // 清理旧实例

  const myChart = echarts.init(lineRef.value);
  myChart.setOption({
    grid: { top: 30, right: 20, bottom: 20, left: 40, containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: labels, axisLine: { lineStyle: { color: '#ccc' } } },
    yAxis: { type: 'value', min: 0, max: 100, splitLine: { lineStyle: { type: 'dashed' } } },
    series: [{ 
      data: scores, 
      type: 'line', 
      smooth: true, 
      symbolSize: 8,
      itemStyle: { color: '#1989fa', borderWidth: 2 },
      areaStyle: { color: new echarts.graphic.LinearGradient(0,0,0,1, [{offset:0, color:'rgba(25,137,250,0.5)'}, {offset:1, color:'rgba(25,137,250,0.0)'}]) }
    }]
  });
};

onMounted(async () => {
  try {
    const [trendRes, listRes] = await Promise.all([
      getGrowthTrend({ userId: 1001, days: 7 }),
      getHistoryList({ userId: 1001, page: 1, size: 10 })
    ]);
    
    // 强制类型断言，确保数据正确
    const trendData = trendRes as any;
    list.value = (listRes as any).records || [];
    
    // 🔴 关键：数据回来后初始化图表
    if (trendData && trendData.labels) {
      nextTick(() => initChart(trendData.labels, trendData.scores));
    }
  } catch (e) {
    console.error("加载档案失败", e);
  }
});
</script>

<style scoped>
.profile-page { min-height: 100vh; background: #f7f8fa; }
.chart-section, .list-section { background: #fff; margin-bottom: 15px; padding: 15px; }
.sec-title { font-weight: bold; margin-bottom: 15px; border-left: 4px solid #1989fa; padding-left: 10px; font-size: 16px; }
.empty-list { text-align: center; color: #999; padding: 20px; font-size: 13px; }
</style>
