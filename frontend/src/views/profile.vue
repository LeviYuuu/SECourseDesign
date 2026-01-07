<template>
  <div class="profile-container">
    <van-nav-bar title="个人成长档案" left-arrow @click-left="$router.back()" fixed placeholder />

    <div class="user-card">
      <div class="avatar-box">
        <van-image round width="60px" height="60px" src="https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg" />
      </div>
      <div class="info-box">
        <div class="nickname">同学小夏</div>
        <div class="uid">学号: 20230001</div>
      </div>
      <div class="stat-box">
        <div class="stat-num">5</div>
        <div class="stat-label">总练次</div>
      </div>
    </div>

    <div class="section-box">
      <div class="section-title">
        <van-icon name="chart-trending-o" color="#1989fa" /> 成长轨迹
      </div>
      <div class="chart-container" ref="lineChartRef"></div>
      <div class="chart-tip" v-if="growthFeedback">
        💡 AI 点评: {{ growthFeedback }}
      </div>
    </div>

    <div class="section-box">
      <div class="section-title">
        <van-icon name="todo-list-o" color="#1989fa" /> 训练历史
      </div>
      
      <van-list>
        <div v-for="item in historyList" :key="item.sessionId" class="history-item" @click="viewReport(item.sessionId)">
          <div class="item-left">
            <div class="item-title">{{ item.title }}</div>
            <div class="item-date">{{ item.date }} · {{ item.difficulty }}</div>
          </div>
          <div class="item-right">
            <span class="score-tag" :class="getScoreClass(item.score)">{{ item.score }}分</span>
            <van-icon name="arrow" color="#ccc" />
          </div>
        </div>
      </van-list>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { getHistoryList, getUserGrowth } from '@/api';
import * as echarts from 'echarts';

const router = useRouter();
const lineChartRef = ref<HTMLElement>();
const historyList = ref<any[]>([]);
const growthFeedback = ref('');

// 初始化折线图
const initChart = (dates: string[], scores: number[]) => {
  if (!lineChartRef.value) return;
  const myChart = echarts.init(lineChartRef.value);
  
  const option = {
    grid: { top: 30, right: 20, bottom: 20, left: 30, containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: { 
      type: 'category', 
      data: dates,
      axisLine: { lineStyle: { color: '#999' } }
    },
    yAxis: { 
      type: 'value', 
      max: 100, 
      min: 0,
      splitLine: { lineStyle: { type: 'dashed' } }
    },
    series: [{
      data: scores,
      type: 'line',
      smooth: true,
      symbolSize: 8,
      itemStyle: { color: '#1989fa', borderWidth: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(25, 137, 250, 0.5)' },
          { offset: 1, color: 'rgba(25, 137, 250, 0.05)' }
        ])
      }
    }]
  };
  myChart.setOption(option);
};

const fetchData = async () => {
  try {
    // 并行请求数据
    const [histRes, growthRes] = await Promise.all([
      getHistoryList(),
      getUserGrowth()
    ]);

    historyList.value = (histRes as any).items;
    
    const gData = growthRes as any;
    growthFeedback.value = gData.feedback;
    
    // 渲染图表
    nextTick(() => {
      initChart(gData.dates, gData.scores);
    });

  } catch (error) {
    console.error(error);
  }
};

const viewReport = (sessionId: number) => {
  router.push(`/report/${sessionId}`);
};

const getScoreClass = (score: number) => {
  if (score >= 80) return 'high';
  if (score >= 60) return 'mid';
  return 'low';
};

onMounted(() => {
  fetchData();
});
</script>

<style scoped>
.profile-container { min-height: 100vh; background: #f7f8fa; padding-bottom: 20px; }

/* 用户卡片 */
.user-card { 
  background: white; margin: 16px; padding: 20px; border-radius: 12px; 
  display: flex; align-items: center; 
  box-shadow: 0 2px 10px rgba(0,0,0,0.03);
}
.info-box { flex: 1; margin-left: 16px; }
.nickname { font-size: 18px; font-weight: bold; color: #333; }
.uid { font-size: 13px; color: #999; margin-top: 4px; }
.stat-box { text-align: center; padding-left: 16px; border-left: 1px solid #eee; }
.stat-num { font-size: 20px; font-weight: bold; color: #1989fa; }
.stat-label { font-size: 12px; color: #999; }

/* 通用板块 */
.section-box { background: white; margin: 16px; border-radius: 12px; padding: 16px; }
.section-title { font-size: 16px; font-weight: bold; margin-bottom: 16px; display: flex; align-items: center; gap: 6px; }

/* 图表 */
.chart-container { width: 100%; height: 220px; }
.chart-tip { margin-top: 10px; background: #f0f9ff; padding: 10px; font-size: 13px; color: #1989fa; border-radius: 8px; line-height: 1.4; }

/* 列表项 */
.history-item { 
  display: flex; justify-content: space-between; align-items: center; 
  padding: 12px 0; border-bottom: 1px solid #f5f5f5; 
}
.history-item:last-child { border-bottom: none; }
.item-title { font-size: 15px; color: #333; margin-bottom: 4px; }
.item-date { font-size: 12px; color: #999; }
.item-right { display: flex; align-items: center; gap: 8px; }
.score-tag { font-weight: bold; font-size: 16px; }
.high { color: #07c160; }
.mid { color: #ff976a; }
.low { color: #ee0a24; }
</style>
