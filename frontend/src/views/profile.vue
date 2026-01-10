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
import { useUserStore } from '@/stores/user';
import { showToast } from 'vant';
import { useRouter } from 'vue-router';

const lineRef = ref<HTMLElement>();
const list = ref<any[]>([]);
const userStore = useUserStore();
const router = useRouter();

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

// 模拟数据函数
const getMockTrendData = () => {
  return {
    labels: ['Day1', 'Day2', 'Day3', 'Day4', 'Day5', 'Day6', 'Day7'],
    scores: [65, 70, 75, 80, 85, 80, 90]
  };
};

const getMockHistoryList = () => {
  return {
    records: [
      { sessionId: 1, scenario: '模拟面试', score: 85, completedAt: '2023-10-01 10:30' },
      { sessionId: 2, scenario: '公开演讲', score: 78, completedAt: '2023-10-02 14:20' },
      { sessionId: 3, scenario: '小组讨论', score: 92, completedAt: '2023-10-03 16:45' },
      { sessionId: 4, scenario: '团队协作', score: 88, completedAt: '2023-10-04 09:15' },
      { sessionId: 5, scenario: '客户沟通', score: 82, completedAt: '2023-10-05 11:30' }
    ]
  };
};

onMounted(async () => {
  try {
    const userId = userStore.userInfo.userId;
    
    if (!userId) {
      showToast('请先登录');
      router.push('/login');
      return;
    }
    
    // 尝试调用真实 API，如果失败则使用模拟数据
    try {
      const [trendRes, listRes] = await Promise.all([
        getGrowthTrend({ userId: userId, days: 7 }),
        getHistoryList({ userId: userId, page: 1, size: 10 })
      ]);
      
      const trendData = trendRes as any;
      list.value = (listRes as any).records || [];
      
      if (trendData && trendData.labels) {
        nextTick(() => initChart(trendData.labels, trendData.scores));
      }
    } catch (apiError) {
      console.warn('API 调用失败，使用模拟数据:', apiError);
      
      // 使用模拟数据
      const trendData = getMockTrendData();
      const listData = getMockHistoryList();
      
      list.value = listData.records;
      nextTick(() => initChart(trendData.labels, trendData.scores));
      
      showToast('使用模拟数据显示档案');
    }
  } catch (e) {
    console.error("加载档案失败", e);
    showToast('加载档案失败');
  }
});
</script>

<style scoped>
.profile-page { min-height: 100vh; background: #f7f8fa; }
.chart-section, .list-section { background: #fff; margin-bottom: 15px; padding: 15px; }
.sec-title { font-weight: bold; margin-bottom: 15px; border-left: 4px solid #1989fa; padding-left: 10px; font-size: 16px; }
.empty-list { text-align: center; color: #999; padding: 20px; font-size: 13px; }
</style>