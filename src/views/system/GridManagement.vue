<template>
  <div class="grid-management">
    <!-- Toolbar -->
    <div class="grid-toolbar">
      <span class="toolbar-title">网格列表</span>
      <el-button type="primary">新增网格</el-button>
    </div>

    <!-- Table -->
    <el-table :data="gridList" stripe style="width: 100%">
      <el-table-column prop="name" label="网格名称" min-width="160" />
      <el-table-column label="级别" width="110">
        <template #default="{ row }">
          <el-tag :type="row.levelType" size="small">{{ row.level }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="leader" label="负责人" width="100" />
      <el-table-column prop="deviceCount" label="设备数量" width="100" />
      <el-table-column prop="contact" label="联系人" width="100" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="scope" label="管辖范围" min-width="180" />
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

const gridList = ref([
  {
    name: '未来科技产业园',
    level: '大网格',
    levelType: 'primary',
    leader: '张三',
    deviceCount: 568,
    contact: '张三',
    phone: '138****0001',
    scope: 'A/B/C栋及地下车库',
  },
  {
    name: 'A栋',
    level: '中网格',
    levelType: 'success',
    leader: '李四',
    deviceCount: 180,
    contact: '李四',
    phone: '138****0002',
    scope: 'A栋1-6层',
  },
  {
    name: 'B栋',
    level: '中网格',
    levelType: 'success',
    leader: '王五',
    deviceCount: 205,
    contact: '王五',
    phone: '138****0003',
    scope: 'B栋1-8层',
  },
  {
    name: 'C栋',
    level: '中网格',
    levelType: 'success',
    leader: '赵六',
    deviceCount: 183,
    contact: '赵六',
    phone: '138****0004',
    scope: 'C栋1-5层',
  },
  {
    name: 'A栋-3层',
    level: '小网格',
    levelType: 'warning',
    leader: '钱七',
    deviceCount: 35,
    contact: '钱七',
    phone: '138****0005',
    scope: 'A栋3层',
  },
])
</script>

<style scoped>
.grid-management {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.grid-toolbar {
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
</style>
