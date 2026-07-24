# 数智消防履职管理平台 — 开发计划 v3.0

> 基于 PRD v3.0 四大体系 | 2026-07-24
> 编排者：黑博士（PM Agent / 技术交付编排者）

---

## 一、项目全景

### 1.1 四大体系架构（PRD v3.0）

```
数智消防履职管理平台
│
├─ 体系一：数智履职管控体系 ◄── 🆕 重点新增（Phase 2）
│   ├─ 1.1 法规数字化拆解引擎
│   │   ├─ 法规库管理（录入/更新/版本控制）
│   │   ├─ 业态自动匹配（按行业/建筑类型筛选）
│   │   └─ 任务规则引擎（法规→岗位→任务自动映射）
│   ├─ 1.2 闭环履职管理
│   │   ├─ 定责：岗位消防责任清单自动生成
│   │   ├─ 履职：移动端检查+拍照留痕（✅已有）
│   │   ├─ 考责：履职指数自动月度考评
│   │   ├─ 督责：超时自动提醒（短信/弹窗）
│   │   └─ 追责：全流程档案一键导出
│   └─ 1.3 电子化档案中心
│       ├─ 全量数据自动归档
│       ├─ 多维度检索
│       └─ 标准台账一键导出
│
├─ 体系二：消防监测预警体系 ◄── 🔄 复用+扩展（Phase 2-3）
│   ├─ 2.1 IoT物联接入层
│   │   ├─ 设备品类扩展（喷淋/水压/电气火灾/防火门）
│   │   ├─ 协议适配（Modbus/MQTT/ONVIF）
│   │   └─ 实时状态 7×24 采集
│   ├─ 2.2 AI智能预警
│   │   ├─ 多模态AI（YOLOv8 + 传感器融合）
│   │   ├─ 告警分级 P0/P1/P2
│   │   └─ 联动处置：告警→派单→处置→销项
│   └─ 2.3 远程值守中心
│
├─ 体系三：火灾防控量化体系 ◄── 🆕 全新模块（Phase 2）
│   ├─ 3.1 FPC 火灾防控能力指数
│   ├─ 3.2 FSD 消防安全履职指数
│   └─ 3.3 数字孪生驾驶舱（Phase 3）
│       ├─ 2.5D平面建模（MVP）
│       ├─ 设备点位标注 + 隐患热力图
│       └─ 一屏概览全单位消防态势
│
└─ 体系四：数据分析决策体系 ◄── 🔄 完善（Phase 2-3）
    ├─ 4.1 多维统计看板（✅已有基础 → 强化）
    ├─ 4.2 预测预警
    └─ 4.3 自动报告生成
```

### 1.2 现有基础盘点

| 维度 | 状态 | 详细说明 |
|:---|:---:|:---|
| **前端框架** | ✅ 已完成 | Vue 3 + Element Plus + Pinia + Vue Router |
| **前端页面** | ✅ 8页面 | Login, Dashboard, Devices, Tasks, Rectifications, Statistics, SystemUsers, SystemGrids |
| **移动端** | ✅ 1页面 | MobileScan（扫码检查） |
| **前端 API 层** | ✅ 9模块 | auth, dashboard, device, task, rectification, statistics, user, grid, mobile |
| **前端 Store** | ✅ 8个 | app, dashboard, device, task, rectification, statistics, user, grid |
| **API 契约** | ✅ 8模块39端点 | 定义在 API_CONTRACT.md |
| **Java 后端** | ⚠️ 11微服务Stub | 有 pom.xml 骨架，无 Java 源文件 |
| **Go 后端** | ✅ 完整 | 9模块 + 迁移脚本 + Docker |
| **数据库迁移** | ⚠️ Flyway 已有 | 需确认迁移脚本完整性 |
| **法规库模块** | ❌ 未开始 | PRD v3.0 新增 — reg_law/reg_article/reg_rule 表 |
| **双指数模块** | ❌ 未开始 | PRD v3.0 新增 — FPC + FSD 模型 |
| **档案管理** | ❌ 未开始 | PRD v3.0 新增 — 电子化档案中心 |
| **数字孪生** | ❌ 未开始 | PRD v3.0 新增 — 2.5D驾驶舱 |
| **AI预警** | ❌ 未开始 | PRD v3.0 新增 — YOLOv8 + 传感器融合 |

---

## 二、分期里程碑（12周）

### Phase 1 — MVP 联调加固（第1-4周）🎯 当前执行阶段

**目标：核心闭环打通，设备→巡检→整改链路完整可用，修复已知BUG**

| 周次 | 前端（左膀）任务 | 后端（右臂）任务 |
|:---:|:---|:---|
| **W1** | ① 联调配置 + 登录认证全链路验证 | ① Java 微服务初始化（从 Go 迁移关键逻辑） |
| | ② Dashboard/Devices/Tasks 联调 | ② Auth 微服务实现（JWT 鉴权） |
| | ③ Statistics/User/Grid 联调 | ③ Device/Task 微服务实现 |
| **W2** | ① 设备管理页功能完善 | ① Rectification/Grid 微服务实现 |
| | ② 设备导入导出交互优化 | ② User/Statistics 微服务实现 |
| | ③ 设备树组件优化 | ③ Mobile 微服务实现 |
| **W3** | ① 巡检任务页联调 | ① 任务微服务定时/超时逻辑 |
| | ② 巡检模板管理页增强 | ② 水印照片服务 |
| | ③ 移动端扫码页联调 | ③ 移动端 API 完善 |
| **W4** | ① 隐患整改页联调 | ① 整改超时预警逻辑 |
| | ② 整改时间线+照片组件优化 | ② 照片上传存储服务 |
| | ③ 修复已知 BUG | ③ 全量集成测试 |
| | **里程碑：MVP 可用版交付** | |

### Phase 2 — 新功能开发（第5-8周）

**目标：法规库 + 双指数 + 档案三大新模块上线**

| 周次 | 前端（左膀）任务 | 后端（右臂）任务 |
|:---:|:---|:---|
| **W5** | ① 法规库页面（列表/分类/全文搜索） | ① 法规库微服务（新建 fire-duty-regulation） |
| | ② 法规详情/版本展示页 | ② 法规CRUD + 全文检索 + 版本管理 |
| **W6** | ① 双指数评估看板页 | ① 双指数微服务（新建 fire-duty-evaluation） |
| | ② 指数趋势图表（ECharts） | ② FPC/FSD 计算引擎 |
| | ③ 指数详情/维度分析页 | ③ 指数数据聚合服务 |
| **W7** | ① 档案管理页（单位/设备/人员档案） | ① 档案管理微服务（新建 fire-duty-archive） |
| | ② 档案归档/检索/导出页 | ② 档案CRUD + 归档规则引擎 |
| | ③ 法规→任务规则配置页 | ③ 任务规则引擎（法规匹配→任务生成） |
| **W8** | ① 统计报表强化（新增图表） | ① 统计微服务增强 |
| | ② 用户管理页增强（档案关联） | ② 用户管理增强 + 档案关联 |
| | ③ 超时提醒 UI 组件 | ③ 超时提醒服务 + 短信通知集成 |
| | **里程碑：功能完整版交付** | |

### Phase 3 — 高阶特性 + 上线（第9-12周）

**目标：数字孪生 + IoT扩展 + AI预警 + 性能优化**

| 周次 | 前端（左膀）任务 | 后端（右臂）任务 |
|:---:|:---|:---|
| **W9** | ① 数字孪生驾驶舱（2.5D平面图） | ① 数字孪生数据服务 |
| | ② 设备点位标注 + 隐患热力图 | ② 设备坐标/状态数据管理 |
| **W10** | ① IoT 设备状态监控面板 | ① IoT 物联接入扩展（MQTT/Modbus） |
| | ② 告警列表/处置面板 | ② AI预警引擎（YOLOv8集成） |
| **W11** | ① 移动端 PWA / H5 体验优化 | ① 移动端接口性能优化 |
| | ② 扫码流程体验优化 | ② 水印照片性能优化 |
| | ③ 高德地图集成 | ③ 地图服务API封装 |
| **W12** | ① 性能优化（首屏/懒加载） | ① 数据库查询优化 + Redis缓存 |
| | ② 部署文档 & 运维手册 | ② 生产环境部署 + 监控告警 |
| | ③ 用户使用手册 | ③ API限流/熔断 |
| | **里程碑：生产就绪版交付** | |

---

## 三、Phase 1 详细任务分解

### W1（第1周）：基础联调

#### 前端任务 PRD-FE-W1-01：联调配置 + 登录认证全链路验证
| 字段 | 内容 |
|:----|:-----|
| **目标** | 配置前端代理，对接后端网关，全链路登录验证 |
| **文件** | `vite.config.js`, `src/api/request.js`, `src/api/auth.js`, `src/stores/app.js` |
| **API 依赖** | `POST /api/v1/auth/login`, `GET /api/v1/auth/me`, `POST /api/v1/auth/refresh` |
| **验收** | 登录页能成功调用后端 Auth 接口，token 存储/刷新/路由守卫完整，401 自动跳转登录 |

#### 后端任务 PRD-BE-W1-01：Auth 微服务 + JWT 鉴权
| 字段 | 内容 |
|:----|:-----|
| **目标** | 实现 Auth 微服务，提供登录/刷新/获取用户信息接口 |
| **文件** | `backend-java/fire-duty-auth/` |
| **表结构** | `sys_user`, `sys_role`, `sys_permission` |
| **API** | `POST /api/v1/auth/login`, `GET /api/v1/auth/me`, `POST /api/v1/auth/refresh`, `PUT /api/v1/auth/password` |
| **验收** | Auth 服务独立可运行，JWT 签发/验证完整，密码 BCrypt 加密 |

#### 前端任务 PRD-FE-W1-02：Dashboard/Devices/Tasks 联调
| 字段 | 内容 |
|:----|:-----|
| **目标** | 三个核心页面对接真实后端数据 |
| **文件** | `src/views/dashboard/Dashboard.vue`, `src/views/devices/`, `src/views/tasks/` |
| **API 依赖** | `GET /api/v1/dashboard/stats`, `GET /api/v1/devices`, `GET /api/v1/tasks` |
| **验收** | 页面数据从 Mock 切换为后端真实数据，无报错 |

#### 后端任务 PRD-BE-W1-02：Device/Task 微服务
| 字段 | 内容 |
|:----|:-----|
| **目标** | 实现 Device 和 Task 微服务 |
| **文件** | `backend-java/fire-duty-device/`, `backend-java/fire-duty-task/` |
| **表结构** | `dev_device`, `dev_device_type`, `task_task`, `task_template`, `task_result` |
| **API** | 参照 API_CONTRACT.md 中设备管理和巡检任务模块 |
| **验收** | CRUD 接口完整，支持分页/搜索/筛选 |

#### 前端任务 PRD-FE-W1-03：Statistics/User/Grid 联调
| 字段 | 内容 |
|:----|:-----|
| **目标** | Statistics/User/Grid 对接到后端真实数据 |
| **文件** | `src/views/statistics/`, `src/views/system/` |
| **API 依赖** | `GET /api/v1/statistics/*`, `GET /api/v1/users`, `GET /api/v1/grids` |
| **验收** | 统计数据正确渲染，用户/网格管理页功能完整 |

#### 后端任务 PRD-BE-W1-03：Rectification/Grid/User/Statistics/Mobile 微服务
| 字段 | 内容 |
|:----|:-----|
| **目标** | 实现剩余 5 个微服务 |
| **文件** | `backend-java/fire-duty-rectification/`, `backend-java/fire-duty-grid/`, `backend-java/fire-duty-user/`, `backend-java/fire-duty-statistics/`, `backend-java/fire-duty-mobile/` |
| **表结构** | `rec_rectification`, `grid_grid`, `sys_user`, `sta_*`, `mobile_scan` |
| **API** | 参照 API_CONTRACT.md 中对应模块 |
| **验收** | 所有模块编译通过，API 响应符合契约规范 |

### W2-W4 任务（详见 Phase 1 阶段任务分配）

---

## 四、Phase 2 新增模块 API 契约规划

### 4.1 法规库模块（约8端点）— 新增微服务：fire-duty-regulation

| 方法 | 路由 | 说明 |
|:----|:-----|:-----|
| GET | `/api/v1/laws` | 法规列表（分页+分类筛选+全文搜索） |
| GET | `/api/v1/laws/:id` | 法规详情（含条款列表） |
| POST | `/api/v1/laws` | 创建法规 |
| PUT | `/api/v1/laws/:id` | 更新法规 |
| DELETE | `/api/v1/laws/:id` | 删除法规 |
| GET | `/api/v1/laws/:id/articles` | 获取条款列表 |
| POST | `/api/v1/laws/:id/articles` | 创建条款 |
| POST | `/api/v1/rules/match` | 规则匹配引擎（输入业态→返回规则→生成任务） |

**数据库表**：`reg_law`, `reg_article`, `reg_rule`

### 4.2 双指数模块（约6端点）— 新增微服务：fire-duty-evaluation

| 方法 | 路由 | 说明 |
|:----|:-----|:-----|
| GET | `/api/v1/evaluation/fpc` | 获取FPC指数（当前期） |
| GET | `/api/v1/evaluation/fpc/trend` | FPC趋势（多期对比） |
| GET | `/api/v1/evaluation/fsd` | 获取FSD指数（当前期） |
| GET | `/api/v1/evaluation/fsd/trend` | FSD趋势（多期对比） |
| POST | `/api/v1/evaluation/calculate` | 手动触发指数计算 |
| GET | `/api/v1/evaluation/overview` | 双指数总览（FPC+FSD同期对比） |

**数据库表**：`idx_fpc_record`, `idx_fsd_record`

### 4.3 档案管理模块（约6端点）— 新增微服务：fire-duty-archive

| 方法 | 路由 | 说明 |
|:----|:-----|:-----|
| GET | `/api/v1/archives` | 档案列表（分页+多维筛选） |
| GET | `/api/v1/archives/:id` | 档案详情 |
| POST | `/api/v1/archives/generate` | 触发自动归档 |
| GET | `/api/v1/archives/export` | 档案导出（PDF/Excel） |
| GET | `/api/v1/archives/categories` | 档案分类统计 |
| DELETE | `/api/v1/archives/:id` | 删除档案 |

---

## 五、分支策略

```
main
 ├── develop ─────────────── 日常开发分支
 │    ├── feature/phase1-mvp ──── Phase 1 功能分支（当前）
 │    ├── feature/laws-library ── 法规库（Phase 2）
 │    ├── feature/dual-index ──── 双指数（Phase 2）
 │    ├── feature/archives ────── 档案管理（Phase 2）
 │    ├── feature/alerts ──────── 超时提醒（Phase 2）
 │    └── feature/map ────────── 地图集成（Phase 3）
 ├── release/mvp ──────────── Phase 1 发布分支
 ├── release/v2.0 ─────────── Phase 2 发布分支
 └── release/v3.0 ─────────── Phase 3 发布分支
```

---

## 六、协作规范

### 6.1 任务交接格式

**给前端 Agent（左膀）的指令必须包含：**
- [ ] 页面组件结构说明
- [ ] 交互流程描述
- [ ] 调用哪些 API（引用 API 契约）
- [ ] Mock 数据结构（后端未就绪时）
- [ ] 目标文件路径（`src/views/`、`src/api/`、`src/stores/`）
- [ ] 验收标准（DoD）

**给后端 Agent（右臂）的指令必须包含：**
- [ ] API 路由（RESTful）
- [ ] 请求/响应 Schema（引用 API 契约）
- [ ] 数据库实体关系（表名 + 字段）
- [ ] 核心业务逻辑伪代码
- [ ] 目标文件路径（`backend-java/fire-duty-*/`）
- [ ] 验收标准（DoD）

### 6.2 交付摘要格式

每个子任务完成后，Agent 必须输出标准摘要：

```
=== 任务交付摘要 ===
任务ID: TASK-XXX
改动的文件: [file1, file2, ...]
核心变更: [简要说明]
联调状态: [待联调 | 联调通过]
依赖项: [依赖哪些接口/模块]
下一个 Agent 需要知道: [关键信息]
```

---

## 七、风险与缓解

| 风险 | 影响 | 缓解措施 |
|:---|:---:|:---|
| 前端 Agent 虚报完成度（历史问题） | ⚠️ 中 | 必须由我亲自验证代码存在 + 构建通过才算验收 |
| Java 后端无源文件需从 Go 迁移 | ⚠️ 高 | W1 安排并行迁移，先核心再扩展 |
| 双指数计算模型细节不足 | ⚠️ 低 | PRD 已提供完整公式和权重 |
| 原型设计缺少法规库/双指数页面 | ⚠️ 中 | 需依据 PRD 描述从零设计 |
| 飞书文件权限 | ⚠️ 低 | 波比已确认公开链接可读 |

---

## 八、当前执行状态

| Phase | 状态 | 责任人 |
|:----|:----:|:------|
| **Phase 1 — W1** | 🔴 正在进行 | 黑博士编排 → 左膀（FE）/ 右臂（BE） |
| **Phase 2 — W5-8** | ⏳ 待启动 | 依赖 Phase 1 完成 |
| **Phase 3 — W9-12** | ⏳ 待启动 | 依赖 Phase 2 完成 |

> 文档版本：v3.0 | 最后更新：2026-07-24 | 编排者：黑博士
