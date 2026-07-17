<template>
  <div class="user-management">
    <!-- Toolbar -->
    <div class="user-toolbar">
      <span class="toolbar-title">用户列表 共 24 人</span>
      <div class="toolbar-actions">
        <el-button plain>导入</el-button>
        <el-button type="primary">新增用户</el-button>
      </div>
    </div>

    <!-- Table -->
    <el-table :data="userList" stripe style="width: 100%">
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="username" label="用户名" width="130" />
      <el-table-column label="角色" width="150">
        <template #default="{ row }">
          <el-tag :type="row.roleType" effect="plain" size="small">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="grid" label="所属网格" width="150" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <span class="status-cell">
            <span :class="['status-dot', row.status === '正常' ? 'dot-green' : 'dot-gray']" />
            {{ row.status }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default>
          <el-button link type="primary" size="small">编辑</el-button>
          <el-button link type="danger" size="small">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const userList = ref([
  {
    name: '管理员',
    username: 'admin',
    role: '超级管理员',
    roleType: 'danger',
    grid: '全局',
    phone: '138****0000',
    status: '正常',
  },
  {
    name: '张三',
    username: 'zhangsan',
    role: '大网格负责人',
    roleType: 'primary',
    grid: '未来科技产业园',
    phone: '138****0001',
    status: '正常',
  },
  {
    name: '李四',
    username: 'lisi',
    role: '中网格组长',
    roleType: 'success',
    grid: 'A栋',
    phone: '138****0002',
    status: '正常',
  },
  {
    name: '王五',
    username: 'wangwu',
    role: '小网格检查员',
    roleType: 'warning',
    grid: 'A栋-3层',
    phone: '138****0003',
    status: '停用',
  },
  {
    name: '赵六',
    username: 'zhaoliu',
    role: '维保单位',
    roleType: '',
    grid: '按授权',
    phone: '138****0004',
    status: '正常',
  },
])
</script>

<style scoped>
.user-management {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.user-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}
.toolbar-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
.toolbar-actions {
  display: flex;
  gap: 8px;
}

.status-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}
.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.dot-green {
  background: #67c23a;
}
.dot-gray {
  background: #c0c4cc;
}
</style>
