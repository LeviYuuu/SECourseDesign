import { createRouter, createWebHistory } from 'vue-router';
import Login from '@/views/Login.vue';
import ScenarioList from '@/views/ScenarioList.vue';
import TrainingRoom from '@/views/TrainingRoom.vue';
import ReportDetail from '@/views/ReportDetail.vue';
import Profile from '@/views/Profile.vue';
import Register from '@/views/Register.vue';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login', component: Login },
    { path: '/scenarios', component: ScenarioList },
    { path: '/training/:sessionId', component: TrainingRoom },
    { path: '/report/:sessionId', component: ReportDetail },
    { path: '/profile', component: Profile },
    { path: '/register', component: Register},
  ]
});

export default router;
