# 图书管理系统前端

基于 Vue 3 + Element Plus 开发的现代化图书管理系统前端界面。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **UI组件库**: Element Plus
- **路由管理**: Vue Router 4
- **状态管理**: Pinia
- **HTTP客户端**: Axios
- **构建工具**: Vite
- **样式**: CSS3 + 响应式设计
- **图标**: Element Plus Icons

## 功能特性

### 🔐 用户认证
- 用户登录/注册
- JWT Token 认证
- 角色权限控制（管理员/读者）
- 自动登录状态管理

### 👨‍💼 管理员功能
- **仪表盘**: 系统概览和统计数据
- **用户管理**: 用户CRUD、状态管理、添加管理员
- **图书管理**: 图书CRUD、库存管理、分类管理
- **分类管理**: 图书分类的增删改查
- **借阅管理**: 借阅审核、归还管理、逾期处理

### 👨‍🎓 读者功能
- **图书浏览**: 卡片式图书展示、搜索筛选
- **借阅申请**: 在线借阅申请、备注说明
- **我的借阅**: 借阅历史、当前借阅状态
- **个人信息**: 账户信息管理

### 🎨 界面特性
- 现代化美观的UI设计
- 响应式布局，支持移动端
- 暗色侧边栏导航
- 卡片式内容展示
- 动画过渡效果
- 统一的错误处理和消息提示

## 快速开始

### 环境要求
- Node.js 16+
- npm 或 yarn

### 安装依赖
```bash
npm install
```

### 启动开发服务器
```bash
npm run dev
```

访问 http://localhost:5173

### 构建生产版本
```bash
npm run build
```

### 预览生产版本
```bash
npm run preview
```

## 项目结构

```
src/
├── assets/          # 静态资源
├── components/      # 公共组件
├── layouts/         # 布局组件
│   └── Layout.vue   # 主布局
├── router/          # 路由配置
│   └── index.js     # 路由定义
├── stores/          # 状态管理
│   └── auth.js      # 认证状态
├── utils/           # 工具函数
│   └── api.js       # API接口
├── views/           # 页面组件
│   ├── admin/       # 管理员页面
│   ├── reader/      # 读者页面
│   ├── Dashboard.vue # 仪表盘
│   └── Login.vue    # 登录页面
├── App.vue          # 根组件
├── main.js          # 入口文件
└── style.css        # 全局样式
```

## API接口配置

项目通过 Vite 代理配置连接后端API：

```javascript
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

确保后端服务运行在 `http://localhost:8080`

## 默认账户

### 管理员账户
- 用户名: `admin`
- 密码: `admin123`

### 读者账户
- 用户名: `reader1` / `reader2` / `reader3`
- 密码: `123456`

## 开发说明

### 状态管理
使用 Pinia 进行状态管理，主要包括：
- 用户认证状态
- Token 管理
- 用户信息存储

### 路由守卫
实现了完整的路由权限控制：
- 登录状态检查
- 角色权限验证
- 自动重定向

### 错误处理
统一的错误处理机制：
- HTTP请求拦截器
- 错误消息提示
- 登录过期处理

### 响应式设计
支持多种设备：
- 桌面端：完整功能展示
- 平板端：自适应布局
- 移动端：简化操作界面

## 部署说明

### 生产环境配置
1. 修改API基础URL
2. 配置生产环境变量
3. 优化构建配置

### 服务器部署
```bash
# 构建项目
npm run build

# 将 dist/ 目录部署到服务器
```

### Docker部署
```dockerfile
FROM nginx:alpine
COPY dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
```

## 浏览器支持

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## 开发团队

前端采用现代化的开发方式，遵循 Vue 3 最佳实践，提供优秀的用户体验。

## 许可证

MIT License
