<template>
  <div class="page-container">
    <van-nav-bar title="选择训练场景" fixed placeholder>
       <template #right>
        <van-icon name="manager-o" size="20" color="#1989fa" @click="$router.push('/profile')" />
      </template>
    </van-nav-bar>
    
    <div class="content-pad">
      <div v-for="item in scenarios" :key="item.id" class="scenario-card" @click="openConfig(item)">
        <div class="card-header">
          <span class="title">{{ item.title }}</span>
          <van-tag type="primary">{{ item.category }}</van-tag>
        </div>
        <div class="card-desc">{{ item.description }}</div>
        <div class="card-footer">
          <span>角色: {{ item.rolePersona }}</span>
          <span>难度: {{ item.difficulty }}</span>
        </div>
      </div>
    </div>

    <van-action-sheet v-model:show="showConfig" title="训练配置">
      <div class="sheet-content">
        <div class="section-title">难度选择</div>
        <van-radio-group v-model="selectedDifficulty">
          <van-cell-group inset>
            <van-cell title="L1 入门" clickable @click="selectedDifficulty = 'L1'">
              <template #right-icon><van-radio name="L1" /></template>
            </van-cell>
            <van-cell title="L2 进阶" clickable @click="selectedDifficulty = 'L2'">
              <template #right-icon><van-radio name="L2" /></template>
            </van-cell>
             <van-cell title="L3 高压" clickable @click="selectedDifficulty = 'L3'">
              <template #right-icon><van-radio name="L3" /></template>
            </van-cell>
          </van-cell-group>
        </van-radio-group>
        <div class="btn-area">
          <van-button type="primary" block round @click="startTraining" :loading="creating">开始训练</van-button>
        </div>
      </div>
    </van-action-sheet>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getScenarios, createSession } from '@/api';
import { showToast } from 'vant';

const router = useRouter();
const scenarios = ref<any[]>([]);
const showConfig = ref(false);
const currentScenario = ref<any>({});
const selectedDifficulty = ref('L1');
const creating = ref(false);

onMounted(async () => {
  const res: any = await getScenarios();
  scenarios.value = res || [];
});

const openConfig = (item: any) => {
  currentScenario.value = item;
  selectedDifficulty.value = item.difficulty;
  showConfig.value = true;
};

const startTraining = async () => {
  creating.value = true;
  try {
    // 🔍 调试日志：看看当前选中的场景数据到底长什么样
    console.log('当前选中的场景:', currentScenario.value);

    // 1. 获取用户 ID (优先从本地存储取，取不到才用默认值)
    // 注意：请确保登录成功后，您把 userId 存到了 localStorage 或 Pinia 中
    const storageUserId = localStorage.getItem('userId');
    const finalUserId = storageUserId ? Number(storageUserId) : 1001;

    // 2. 发送请求
    const res: any = await createSession({
      userId: finalUserId, 
      
      // ✅ 修复点：使用 templateId (对应后端实体类字段)
      // 为了兼容性，如果后端既认 scenarioId 也认 templateId，建议直接传 templateId
      templateId: currentScenario.value.templateId, 

      config: {
        difficulty: selectedDifficulty.value,
        rounds: currentScenario.value.defaultRounds || 6
      }
    });

    router.push(`/training/${res.sessionId}`);
  } catch (error) {
    console.error(error); // 打印错误详情
    showToast('创建失败，请检查控制台日志');
  } finally {
    creating.value = false;
  }
};
</script>

<style scoped>
.page-container { min-height: 100vh; background: #f7f8fa; }
.content-pad { padding: 16px; }
.scenario-card { background: #fff; border-radius: 12px; padding: 16px; margin-bottom: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.card-header { display: flex; justify-content: space-between; margin-bottom: 8px; font-weight: bold; font-size: 16px; }
.card-desc { font-size: 13px; color: #666; margin-bottom: 12px; }
.card-footer { display: flex; justify-content: space-between; font-size: 12px; color: #999; border-top: 1px solid #f0f0f0; padding-top: 8px; }
.sheet-content { padding: 16px 0; }
.section-title { padding: 0 16px 10px; font-size: 14px; color: #666; }
.btn-area { padding: 24px 16px 10px; }
</style>
