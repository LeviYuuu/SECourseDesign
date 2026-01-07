import { createApp } from 'vue'
import './style.css' // 如果你刚才保留了空文件，这行留着；如果删了文件，这行也要删
import App from './App.vue'
import router from './router' // 确保引入了路由
import { createPinia } from 'pinia'
// 引入 Vant 样式
import 'vant/lib/index.css';

const app = createApp(App)

app.use(createPinia())
app.use(router) // ⚠️ 必须挂载路由，否则 <router-view> 无效

app.mount('#app')
