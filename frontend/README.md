# 面向大学生的专业场景社交焦虑陪练系统 (前端工程)

## 1. 项目简介

本项目是“面向大学生的专业场景社交焦虑陪练系统”的前端部分。系统旨在通过模拟面试、学术汇报等高压场景，利用 AI 角色扮演与实时语音交互，帮助大学生克服社交焦虑，提升表达能力。

**核心功能：**

* **场景选择 (UC01)**：支持多种难度（L1-L3）与场景类型选择。
* **语音对练 (UC02/UC03)**：模拟真实对话流，支持语音/文本双模式输入。
* **智能评估 (UC04/UC05)**：基于多维度的雷达图评分与高情商话术改写建议。
* **成长档案 (UC06)**：可视化展示用户的训练历史与能力成长曲线。

## 2. 技术栈

本项目基于现代化的前端技术栈构建：

* **核心框架**：Vue 3 (Composition API)
* **语言**：TypeScript
* **构建工具**：Vite 5
* **UI 组件库**：Vant 4 (移动端风格)
* **状态管理**：Pinia
* **网络请求**：Axios
* **数据可视化**：Apache ECharts
* **工具库**：unplugin-auto-import (自动导入)

## 3. 环境要求 (Prerequisites)

在运行本项目之前，请确保您的本地环境满足以下要求：

* **Node.js**: 版本需 >= 16.0.0 (推荐使用 LTS 版本，如 v18.x 或 v20.x)
* **包管理器**: npm (随 Node 安装) 或 yarn / pnpm

您可以通过以下命令检查版本：

```bash
node -v
npm -v
```

## 4. 快速开始 (Installation & Run)

### 4.1 克隆/进入项目

确保您已处于前端项目目录（`frontend`）下：

```bash
cd frontend
```

### 4.2 安装依赖

下载并安装项目所需的第三方库：

```bash
npm install
```

*(如果安装速度较慢，建议配置淘宝镜像或使用 cnpm)*

### 4.3 启动开发服务器

启动本地热更新开发环境：

```bash
npm run dev
```

启动成功后，终端将显示访问地址（通常为 `http://localhost:5173`）。在浏览器中打开该地址即可使用系统。

### 4.4 构建生产版本 (可选)

如果需要打包部署：

```bash
npm run build
```

打包生成的文件位于 `dist` 目录下。

## 5. 配置说明 (Configuration)

### 5.1 开启/关闭 Mock 模式 (关键)

本项目内置了完整的 Mock 数据逻辑，支持在无后端情况下独立运行。

* **文件位置**: `src/api/index.ts`
* **配置项**: `USE_MOCK`

**如何切换：**

1. **纯前端演示模式**（默认）：

   ```typescript
   // src/api/index.ts
   const USE_MOCK = true; 
   ```

   此时所有请求会被拦截，返回本地模拟数据，适合演示 UI 和交互流程。

2. **对接真实后端模式**：

   ```typescript
   // src/api/index.ts
   const USE_MOCK = false;
   ```

   此时请求将通过 Vite 代理转发至后端服务器。

### 5.2 后端接口代理配置

当关闭 Mock 模式连接真实后端时，需确认代理地址配置。

* **文件位置**: `vite.config.ts`
* **配置项**: `server.proxy`

<!-- end list -->

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080', // 确保此处指向您的 SpringBoot 后端端口
      changeOrigin: true,
      // rewrite: (path) => path.replace(/^\/api/, '') // 根据后端接口前缀决定是否开启
    }
  }
}
```

## 6. 目录结构说明

```text
frontend/
├── src/
│   ├── api/                # 后端接口定义与 Mock 数据逻辑
│   ├── assets/             # 静态资源 (图片、样式)
│   ├── components/         # 公共组件
│   ├── router/             # 路由配置 (Vue Router)
│   ├── stores/             # 状态管理 (Pinia)
│   ├── utils/              # 工具函数 (Axios 封装等)
│   ├── views/              # 页面视图
│   │   ├── Login.vue           # 登录页
│   │   ├── ScenarioList.vue    # 场景选择页
│   │   ├── TrainingRoom.vue    # 核心对练页
│   │   ├── ReportDetail.vue    # 评估报告页
│   │   └── Profile.vue         # 成长档案页
│   ├── App.vue             # 根组件
│   └── main.ts             # 入口文件
├── vite.config.ts          # Vite 配置文件
├── tsconfig.json           # TypeScript 配置文件
└── package.json            # 项目依赖描述
```

## 7. 常见问题 (FAQ)

**Q1: 为什么页面显示组件标签（如 `<van-button>`）而不是按钮？**

* **A**: 可能是自动导入插件配置未生效或未安装。请确保已运行 `npm install`，并检查 `vite.config.ts` 中包含 `AutoImport` 和 `Components` 配置。

**Q2: 登录后无法跳转？**

* **A**: 请检查控制台（F12 -> Console）是否有报错。如果是 Mock 模式，请确认 `src/api/index.ts` 中的 `USE_MOCK` 为 `true`。如果是对接后端，请检查后端服务是否启动且端口一致。

**Q3: 安装依赖时报错？**

* **A**: 请尝试删除 `node_modules` 文件夹和 `package-lock.json` 文件，然后重新运行 `npm install`。建议检查 Node.js 版本是否过低。
