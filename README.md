# 🔥 消防履职系统

消防设备巡检与隐患整改管理平台。

## 技术栈

| 层 | 技术 |
|:---|:-----|
| 前端 | Vue 3 + Element Plus + Pinia + Axios |
| 后端 | Go 1.22 + Gin + pgx |
| 数据库 | PostgreSQL 16 |
| 部署 | Docker Compose (Nginx + Go API + PostgreSQL) |

## 快速启动

### 开发模式

```bash
# 1. 启动后端（需本地安装 Go 1.22+）
cd backend
go run ./cmd/server/

# 2. 启动前端（另一终端）
npm run dev
# 访问 http://localhost:5173
# 前端通过 Vite proxy 转发 /api/* 到 localhost:8080
```

### Docker 部署

```bash
# 构建前端
npm run build

# 一键启动全部服务
cd backend
docker compose up -d

# 访问 http://localhost:80
# API: http://localhost:8080/api/v1
```

## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 超级管理员 |
| zhangsan | 123456 | 大网格负责人 |
| lisi | 123456 | 中网格组长 |
| wangwu | 123456 | 小网格检查员 |

## 项目结构

```
/
├── API_CONTRACT.md          # API 契约文档
├── src/                     # 前端源码
│   ├── api/                 # API 客户端（9 个模块）
│   ├── stores/              # Pinia 状态管理（7 个 Store）
│   ├── views/               # 页面组件（8 个页面）
│   └── router/              # 路由配置
├── backend/                 # 后端源码
│   ├── cmd/server/main.go   # 入口
│   ├── internal/            # 内部模块
│   │   ├── auth/            # 用户认证
│   │   ├── device/          # 设备管理
│   │   ├── task/            # 巡检任务
│   │   ├── rectification/   # 整改工单（状态机）
│   │   ├── statistics/      # 统计报表
│   │   ├── user/            # 用户管理
│   │   ├── grid/            # 网格管理
│   │   ├── mobile/          # 移动端 API
│   │   ├── middleware/      # JWT Auth + RBAC
│   │   └── model/           # 数据模型
│   ├── migrations/          # 数据库迁移
│   │   ├── 001_init.sql     # DDL（11 张表）
│   │   └── 002_seed.sql     # 种子数据
│   ├── pkg/                 # 公共包
│   └── docker-compose.yaml  # Docker 编排
```

## API 概览

| 模块 | 路由 | 说明 |
|:-----|:-----|:-----|
| Auth | POST /api/v1/auth/login | 登录 |
| Auth | GET /api/v1/auth/me | 当前用户 |
| Dashboard | GET /api/v1/dashboard/stats | 仪表盘统计 |
| Devices | GET /api/v1/devices | 设备列表 |
| Devices | GET /api/v1/devices/tree | 设备树 |
| Tasks | GET /api/v1/tasks | 任务列表 |
| Tasks | GET /api/v1/tasks/templates | 任务模板 |
| Rectifications | PUT /api/v1/rectifications/:id/dispatch | 派发工单 |
| Rectifications | PUT /api/v1/rectifications/:id/review | 复核工单 |
| Statistics | GET /api/v1/statistics/compliance | 合规率 |
| Users | GET /api/v1/users | 用户列表 |
| Grids | GET /api/v1/grids | 网格列表 |
| Mobile | GET /api/v1/mobile/sync | 离线同步 |
