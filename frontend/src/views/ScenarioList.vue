<template>
  <div class="page-container">
    <van-nav-bar title="选择训练场景" fixed placeholder>
        <template #right>
            <van-icon name="manager-o" size="20" color="#1989fa" @click="$router.push('/profile')" />
        </template>
    </van-nav-bar>
    
    <div class="content-pad">
      <div v-for="item in scenarios" :key="item.scenarioId" class="scenario-card" @click="openConfig(item)">
        <div class="card-header">
          <span class="title">{{ item.title }}</span>
          <van-tag type="primary">{{ item.rolePersona }}</van-tag>
        </div>
        <div class="card-desc">{{ item.description }}</div>
        <div class="card-footer">
          <span>难度: {{ item.difficulty }}</span>
          <span>预计 {{ item.rounds }} 轮</span>
        </div>
      </div>
    </div>

    <van-action-sheet v-model:show="showConfig" title="训练设置">
      <div class="sheet-content">
        <div class="section-title">选择难度等级</div>
        <van-radio-group v-model="selectedDifficulty">
          <van-cell-group inset>
            <van-cell title="L1 入门 (温和)" clickable @click="selectedDifficulty = 'L1'">
              <template #right-icon><van-radio name="L1" /></template>
            </van-cell>
            <van-cell title="L2 进阶 (标准)" clickable @click="selectedDifficulty = 'L2'">
              <template #right-icon><van-radio name="L2" /></template>
            </van-cell>
            <van-cell title="L3 高压 (刁难)" clickable @click="selectedDifficulty = 'L3'">
              <template #right-icon><van-radio name="L3" /></template>
            </van-cell>
          </van-cell-group>
        </van-radio-group>
        
        <div class="btn-area">
          <van-button type="primary" block round @click="startTraining" :loading="creating">
            开始训练
          </van-button>
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
  try {
    const res: any = await getScenarios();
    scenarios.value = res.items || [];
  } catch (error) {
    console.error(error);
  }
});

const openConfig = (item: any) => {
  currentScenario.value = item;
  selectedDifficulty.value = item.difficulty; // 默认选中当前难度
  showConfig.value = true;
};

const startTraining = async () => {
  creating.value = true;
  try {
    const res: any = await createSession({
      scenarioId: currentScenario.value.scenarioId,
      difficulty: selectedDifficulty.value,
      turnLimit: 6
    });
    showConfig.value = false;
    router.push(`/training/${res.sessionId}`);
  } catch (error) {
    showToast('创建会话失败');
  } finally {
    creating.value = false;
  }
};
</script>

<style scoped>
.page-container { min-height: 100vh; background: #f7f8fa; }
.content-pad { padding: 16px; }

.scenario-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  transition: all 0.2s;
}
.scenario-card:active { transform: scale(0.98); }

.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.title { font-size: 17px; font-weight: bold; color: #333; }
.card-desc { font-size: 13px; color: #666; margin-bottom: 12px; line-height: 1.5; }
.card-footer { display: flex; justify-content: space-between; font-size: 12px; color: #999; border-top: 1px solid #f0f0f0; padding-top: 8px; }

.sheet-content { padding: 16px 0; }
.section-title { padding: 0 16px 10px; font-size: 14px; color: #666; }
.btn-area { padding: 24px 16px 10px; }
</style>
