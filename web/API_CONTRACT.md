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
