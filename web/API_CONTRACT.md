# 消防履职系统 — API 契约 v1.0

Base URL: `/api/v1`

---

## 1. 用户认证

### POST /auth/login
Request:
```json
{
  "username": "string",
  "password": "string"
}
```
Response (200):
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "token": "string (JWT)",
    "refreshToken": "string (JWT)",
    "user": {
      "id": "integer",
      "name": "string",
      "username": "string",
      "role": "string (超级管理员|大网格负责人|中网格组长|小网格检查员|维保单位)",
      "avatar": "string",
      "gridId": "integer",
      "gridName": "string"
    }
  }
}
```

### POST /auth/refresh
Request:
```json
{ "refreshToken": "string" }
```
Response (200):
```json
{
  "code": 0,
  "data": {
    "token": "string",
    "refreshToken": "string"
  }
}
```

### GET /auth/me
Headers: `Authorization: Bearer <token>`
Response:
```json
{
  "code": 0,
  "data": {
    "id": "integer",
    "name": "string",
    "username": "string",
    "role": "string",
    "avatar": "string",
    "permissions": ["string", "..."]
  }
}
```

### PUT /auth/password
Request:
```json
{
  "oldPassword": "string",
  "newPassword": "string"
}
```
Response: `{ "code": 0 }`

---

## 2. 仪表盘

### GET /dashboard/stats
Response:
```json
{
  "code": 0,
  "data": {
    "totalDevices": 568,
    "onlineRate": 93.7,
    "todayInspections": 45,
    "pendingRectifications": 8,
    "completionRate": 71.1,
    "plannedTasks": 63,
    "completedTasks": 45,
    "overdueTasks": 18
  }
}
```

### GET /dashboard/alerts
Response:
```json
{
  "code": 0,
  "data": [{
    "severity": "P0|P1|P2",
    "description": "string",
    "time": "string",
    "deviceCode": "string",
    "relatedId": "integer"
  }]
}
```

---

## 3. 设备管理

### GET /devices
Query: `?page=1&pageSize=20&search=&type=&status=&gridId=`

Response:
```json
{
  "code": 0,
  "data": {
    "list": [{
      "id": "integer",
      "code": "string (EXT-001)",
      "name": "string",
      "type": "string (灭火器|消火栓|烟感|喷淋)",
      "location": "string",
      "status": "string (正常|故障|维护中)",
      "lastCheck": "string (date)",
      "manufacturer": "string",
      "installDate": "string (date)",
      "lastMaintenance": "string (date)",
      "gridId": "integer",
      "gridPath": "string (A栋/3层)"
    }],
    "total": 568,
    "page": 1,
    "pageSize": 20
  }
}
```

### GET /devices/tree
Response:
```json
{
  "code": 0,
  "data": [{
    "id": "string",
    "label": "string",
    "count": 568,
    "children": [{
      "id": "string",
      "label": "string",
      "count": 180,
      "children": [{
        "id": "string",
        "label": "string",
        "count": 32
      }]
    }]
  }]
}
```

### GET /devices/:id
Response: single device object (same schema as list item)

### POST /devices
Request: device create payload (minus id)
Response: created device object

### PUT /devices/:id
Request: partial/full device update
Response: updated device object

### DELETE /devices/:id
Response: `{ "code": 0 }`

### POST /devices/import
Multipart: file (Excel/CSV)
Response: `{ "code": 0, "data": { "imported": 45, "failed": 2, "errors": ["row 3: code duplicate"] } }`

### GET /devices/export
Query: `?type=&status=&gridId=`
Response: file download (Excel/CSV)

---

## 4. 巡检任务

### GET /tasks
Query: `?page=1&pageSize=20&tab=pending|completed|overdue&assignee=`

Response:
```json
{
  "code": 0,
  "data": {
    "list": [{
      "id": "integer",
      "deviceCode": "string",
      "deviceName": "string",
      "deviceType": "string",
      "templateName": "string",
      "location": "string",
      "assignee": "string",
      "deadline": "string (datetime)",
      "status": "string (待检查|已完成|已超时)",
      "createdAt": "string (datetime)"
    }],
    "counts": { "pending": 12, "completed": 45, "overdue": 3 }
  }
}
```

### POST /tasks
Request:
```json
{
  "deviceIds": [1,2,3],
  "templateId": 1,
  "assigneeId": 1,
  "deadline": "string (datetime)",
  "remark": "string"
}
```
Response: created task array

### GET /tasks/templates
Response:
```json
{
  "code": 0,
  "data": [{
    "id": "integer",
    "name": "string (月度灭火器检查表)",
    "deviceType": "string",
    "itemCount": 6,
    "cycle": "string (每月|每季度|每年)",
    "items": [{
      "id": "integer",
      "name": "string (外观检查)",
      "type": "string (boolean|enum|text)",
      "options": ["string", "..."]
    }]
  }]
}
```

### POST /tasks/templates
Request: template creation payload

### POST /tasks/:id/submit
Request:
```json
{
  "results": [{
    "itemId": 1,
    "result": "string (正常|异常|不适用)",
    "remark": "string",
    "imageUrls": ["string"]
  }],
  "overallRemark": "string"
}
```
Response: `{ "code": 0 }`

---

## 5. 隐患整改

### GET /rectifications
Query: `?page=1&pageSize=20&tab=pending|ongoing|review|closed`

Response:
```json
{
  "code": 0,
  "data": {
    "list": [{
      "id": "integer",
      "description": "string",
      "deviceCode": "string",
      "deviceName": "string",
      "level": "string (紧急|一般)",
      "levelType": "string (danger|warning)",
      "foundTime": "string (datetime)",
      "assignee": "string",
      "status": "string (待派发|整改中|待复核|已闭环|已超时)",
      "statusType": "string (warning|primary|info|success|danger)",
      "deadline": "string (datetime)"
    }],
    "counts": { "pending": 3, "ongoing": 5, "review": 2, "closed": 128 }
  }
}
```

### GET /rectifications/:id
Response (with timeline and photos):
```json
{
  "code": 0,
  "data": {
    "basic": { "...rectification object" },
    "timeline": [{
      "action": "string",
      "operator": "string",
      "comment": "string",
      "timestamp": "string (datetime)"
    }],
    "photos": {
      "before": [{ "url": "string", "takenAt": "string" }],
      "after": [{ "url": "string", "takenAt": "string" }]
    }
  }
}
```

### PUT /rectifications/:id/dispatch
Request: `{ "assigneeId": 1, "deadline": "string (datetime)" }`

### PUT /rectifications/:id/submit-fix
Request:
```json
{
  "comment": "string",
  "imageUrls": ["string (after photos)"]
}
```

### PUT /rectifications/:id/review
Request: `{ "approved": true|false, "comment": "string" }`

### POST /rectifications/:id/photos
Multipart: image file(s) + query param `?type=before|after`

---

## 6. 统计报表

### GET /statistics/compliance
Query: `?months=6`
Response:
```json
{
  "code": 0,
  "data": {
    "months": ["5月","6月","7月","8月","9月"],
    "rates": [60, 70, 85, 80, 88]
  }
}
```

### GET /statistics/hazard-distribution
Response:
```json
{
  "code": 0,
  "data": [{
    "type": "string (灭火器|消火栓|烟感|喷淋|其他)",
    "rate": 35.0,
    "color": "#f56c6c"
  }]
}
```

### GET /statistics/summary
Response:
```json
{
  "code": 0,
  "data": {
    "overallComplianceRate": 93.5,
    "topHazardType": "灭火器",
    "topHazardRate": 35.0,
    "totalHazardsThisMonth": 47
  }
}
```

### GET /statistics/export
Query: `?type=compliance|hazard&format=excel|csv`
Response: file download

---

## 7. 用户管理

### GET /users
Query: `?page=1&pageSize=20&role=&status=`
Response:
```json
{
  "code": 0,
  "data": {
    "list": [{
      "id": "integer",
      "name": "string",
      "username": "string",
      "role": "string",
      "gridId": "integer",
      "gridName": "string",
      "phone": "string",
      "status": "string (正常|停用)",
      "lastLogin": "string (datetime)"
    }],
    "total": 24
  }
}
```

### POST /users
### PUT /users/:id
### DELETE /users/:id

---

## 8. 网格管理

### GET /grids
Response:
```json
{
  "code": 0,
  "data": [{
    "id": "integer",
    "name": "string",
    "level": "string (大网格|中网格|小网格)",
    "parentId": "integer",
    "path": "string (物化路径, e.g. '1/2/5')",
    "leader": "string",
    "deviceCount": 568,
    "contact": "string",
    "phone": "string",
    "scope": "string"
  }]
}
```

### POST /grids
### PUT /grids/:id
### DELETE /grids/:id

---

## 通用错误响应
```json
{
  "code": 401,
  "message": "未登录或token已过期",
  "data": null
}
```
Error codes: 0=OK, 401=Unauthorized, 403=Forbidden, 404=NotFound, 422=ValidationError, 500=InternalError

---

## 9. 法规库（Phase 2 — 新增微服务：fire-duty-regulation）

### GET /laws
Query: `?page=1&pageSize=20&search=&region=&status=`
Response:
```json
{
  "code": 0,
  "data": {
    "list": [{
      "id": "string (UUID)",
      "lawName": "string",
      "lawCode": "string",
      "publishDate": "string (date)",
      "effectiveDate": "string (date)",
      "region": "string",
      "version": 1,
      "status": 1,
      "articleCount": 12
    }],
    "total": 45,
    "page": 1,
    "pageSize": 20
  }
}
```

### GET /laws/:id
Response:
```json
{
  "code": 0,
  "data": {
    "id": "string (UUID)",
    "lawName": "string",
    "lawCode": "string",
    "publishDate": "string",
    "effectiveDate": "string",
    "region": "string",
    "regionDetail": "string",
    "version": 1,
    "status": 1,
    "articles": [{
      "id": "string (UUID)",
      "articleNo": "string",
      "content": "string",
      "category": "string"
    }]
  }
}
```

### POST /laws
Request:
```json
{
  "lawName": "string",
  "lawCode": "string",
  "publishDate": "string",
  "effectiveDate": "string",
  "region": "string",
  "regionDetail": "string",
  "status": 1
}
```

### PUT /laws/:id
Request: partial law update payload

### DELETE /laws/:id
Response: `{ "code": 0 }`

### GET /laws/:id/articles
Response: same articles array as in law detail

### POST /laws/:id/articles
Request:
```json
{
  "articleNo": "string",
  "content": "string",
  "category": "string"
}
```

### POST /rules/match
Request:
```json
{
  "buildingType": "string (医院|学校|商场|住宅|工厂)",
  "region": "string (广东省)"
}
```
Response:
```json
{
  "code": 0,
  "data": [{
    "ruleId": "string",
    "lawName": "string",
    "articleNo": "string",
    "ruleType": "string",
    "frequency": "string",
    "targetRole": "string",
    "actionTemplate": "string"
  }]
}
```

---

## 10. 双指数评估（Phase 2 — 新增微服务：fire-duty-evaluation）

### GET /evaluation/fpc
Response:
```json
{
  "code": 0,
  "data": {
    "score": 82.5,
    "level": "L2",
    "aScore": 78.0,
    "bScore": 85.0,
    "cScore": 84.0,
    "period": "2026-07"
  }
}
```

### GET /evaluation/fpc/trend
Query: `?months=6`
Response:
```json
{
  "code": 0,
  "data": {
    "periods": ["2026-02","2026-03","2026-04","2026-05","2026-06","2026-07"],
    "scores": [70.1, 72.3, 74.8, 76.5, 78.2, 82.5],
    "levels": ["L3","L3","L2","L2","L2","L2"]
  }
}
```

### GET /evaluation/fsd
Response:
```json
{
  "code": 0,
  "data": {
    "score": 76.3,
    "period": "2026-07",
    "dScore": 80.0,
    "eScore": 75.0,
    "fScore": 78.0,
    "gScore": 72.0,
    "hScore": 74.0,
    "iScore": 70.0
  }
}
```

### GET /evaluation/fsd/trend
Query: `?months=6`
Response: same shape as fpc/trend

### POST /evaluation/calculate
Response:
```json
{
  "code": 0,
  "data": {
    "fpc": { "score": 82.5, "level": "L2" },
    "fsd": { "score": 76.3 }
  }
}
```

### GET /evaluation/overview
Response:
```json
{
  "code": 0,
  "data": {
    "fpc": { "score": 82.5, "level": "L2", "trend": "up" },
    "fsd": { "score": 76.3, "trend": "up" },
    "complianceRate": 94.2,
    "deviceOnlineRate": 96.8
  }
}
```

---

## 11. 档案中心（Phase 2 — 新增微服务：fire-duty-archive）

### GET /archives
Query: `?page=1&pageSize=20&type=&search=&dateFrom=&dateTo=`
Response:
```json
{
  "code": 0,
  "data": {
    "list": [{
      "id": "string (UUID)",
      "title": "string",
      "type": "string (单位|设备|人员|整改)",
      "summary": "string",
      "fileSize": "string (1.2MB)",
      "status": "string (已归档|生成中)",
      "createdAt": "string (datetime)"
    }],
    "total": 128
  }
}
```

### GET /archives/:id
Response: full archive detail with download URL

### POST /archives/generate
Request:
```json
{
  "type": "string (单位|设备|人员|整改)",
  "tenantId": "string (UUID)",
  "dateRange": { "from": "string", "to": "string" }
}
```
Response:
```json
{
  "code": 0,
  "data": {
    "archiveId": "string (UUID)",
    "status": "生成中",
    "estimatedTime": "30秒"
  }
}
```

### GET /archives/export
Query: `?archiveId=&format=pdf|excel`
Response: file download

### GET /archives/categories
Response:
```json
{
  "code": 0,
  "data": {
    "categories": [
      { "type": "单位", "count": 35 },
      { "type": "设备", "count": 128 },
      { "type": "人员", "count": 56 },
      { "type": "整改", "count": 89 }
    ]
  }
}
```

### DELETE /archives/:id
Response: `{ "code": 0 }`
