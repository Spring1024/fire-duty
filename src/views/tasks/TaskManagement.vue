<template>
  <div class="task-management">
    <!-- Tabs Section -->
    <div class="task-tabs-section">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="待检查" name="pending">
          <template #label>
            <span class="tab-label">
              待检查
              <el-badge :value="12" :hidden="false" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已完成" name="completed">
          <template #label>
            <span class="tab-label">
              已完成
              <el-badge :value="45" :hidden="false" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已超时" name="overdue">
          <template #label>
            <span class="tab-label">
              已超时
              <el-badge :value="3" :hidden="false" class="tab-badge overdue-badge" />
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- Content Section -->
    <div class="task-content">
      <!-- Title Row -->
      <div class="content-header">
        <h3 class="content-title">今日待检任务</h3>
        <div class="content-actions">
          <el-button plain @click="showTemplateDialog = true">模板管理</el-button>
          <el-button type="primary" @click="handleDispatch">派发任务</el-button>
        </div>
      </div>

      <!-- Task Table -->
      <el-table
        :data="currentTaskList"
        style="width: 100%"
        border
        stripe
        highlight-current-row
        :row-class-name="taskTableRowClassName"
        v-loading="loading"
      >
        <el-table-column prop="device" label="设备" min-width="180">
          <template #default="{ row }">
            <span class="device-cell">
              <span class="device-code">{{ row.deviceCode }}</span>
              {{ row.device }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="template" label="检查模板" min-width="180" />
        <el-table-column prop="location" label="位置" min-width="140" />
        <el-table-column prop="assignee" label="责任人" width="100" />
        <el-table-column prop="deadline" label="截止时间" width="150" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="taskStatusTagType(row.status)" size="small" effect="dark">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="text" size="small" @click="viewTask(row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- Template Management Dialog -->
    <el-dialog
      v-model="showTemplateDialog"
      title="任务模板管理"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-table :data="templateList" border stripe style="width: 100%">
        <el-table-column prop="name" label="模板名称" min-width="200" />
        <el-table-column prop="deviceType" label="适用设备类型" width="130" />
        <el-table-column prop="itemCount" label="检查项数量" width="110">
          <template #default="{ row }">
            {{ row.itemCount }}项
          </template>
        </el-table-column>
        <el-table-column prop="cycle" label="周期" width="100" />
        <el-table-column prop="lastUpdate" label="最后更新" width="130" />
      </el-table>
      <template #footer>
        <el-button @click="showTemplateDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// ---------- Tabs ----------
const activeTab = ref('pending')

function handleTabClick() {
  // Tab switching logic would go here when dynamic data is used
}

// ---------- Task Data ----------
const loading = ref(false)

const allTasks = {
  pending: [
    {
      deviceCode: 'EXT-001',
      device: '灭火器',
      deviceFull: '3层走廊灭火器',
      template: '月度灭火器检查表',
      location: 'A栋-3层',
      assignee: '张三',
      deadline: '今天 18:00',
      status: '待检查',
    },
    {
      deviceCode: 'HYD-008',
      device: '消火栓',
      deviceFull: '东侧消火栓',
      template: '月度消火栓检查表',
      location: 'A栋-1层',
      assignee: '李四',
      deadline: '今天 18:00',
      status: '待检查',
    },
    {
      deviceCode: 'EXT-005',
      device: '灭火器',
      deviceFull: 'C栋-1层灭火器',
      template: '月度灭火器检查表',
      location: 'C栋-1层',
      assignee: '赵六',
      deadline: '明天 18:00',
      status: '待检查',
    },
    {
      deviceCode: 'SPR-015',
      device: '喷淋',
      deviceFull: '地下车库喷淋',
      template: '季度喷淋检测表',
      location: 'B1层',
      assignee: '钱七',
      deadline: '今天 18:00',
      status: '待检查',
    },
  ],
  completed: [
    {
      deviceCode: 'EXT-003',
      device: '灭火器',
      deviceFull: '大厅灭火器',
      template: '月度灭火器检查表',
      location: 'A栋-1层',
      assignee: '张三',
      deadline: '2026-07-15 18:00',
      status: '已完成',
    },
    {
      deviceCode: 'SEN-031',
      device: '烟感',
      deviceFull: 'C栋温感',
      template: '季度烟感检测表',
      location: 'C栋-5层',
      assignee: '王五',
      deadline: '2026-07-14 18:00',
      status: '已完成',
    },
    {
      deviceCode: 'HYD-012',
      device: '消火栓',
      deviceFull: '西侧消火栓',
      template: '月度消火栓检查表',
      location: 'C栋-2层',
      assignee: '李四',
      deadline: '2026-07-13 18:00',
      status: '已完成',
    },
    {
      deviceCode: 'SPR-022',
      device: '喷淋',
      deviceFull: 'A栋车库喷淋',
      template: '月度喷淋检查表',
      location: 'A栋-B1层',
      assignee: '钱七',
      deadline: '2026-07-12 18:00',
      status: '已完成',
    },
  ],
  overdue: [
    {
      deviceCode: 'SEN-023',
      device: '烟感',
      deviceFull: 'B栋烟感',
      template: '季度烟感检测表',
      location: 'B栋-3层',
      assignee: '王五',
      deadline: '昨天 18:00',
      status: '已超时',
    },
    {
      deviceCode: 'EXT-007',
      device: '灭火器',
      deviceFull: 'B栋2层灭火器',
      template: '月度灭火器检查表',
      location: 'B栋-2层',
      assignee: '赵六',
      deadline: '2026-07-14 18:00',
      status: '已超时',
    },
    {
      deviceCode: 'HYD-015',
      device: '消火栓',
      deviceFull: 'B栋北侧消火栓',
      template: '月度消火栓检查表',
      location: 'B栋-1层',
      assignee: '李四',
      deadline: '2026-07-13 18:00',
      status: '已超时',
    },
  ],
}

const currentTaskList = computed(() => {
  return allTasks[activeTab.value] || []
})

function taskTableRowClassName({ row }) {
  if (row.status === '已超时') {
    return 'overdue-row'
  }
  return ''
}

function taskStatusTagType(status) {
  const map = {
    '待检查': 'warning',
    '已完成': 'success',
    '已超时': 'danger',
  }
  return map[status] || 'info'
}

// ---------- Template Management ----------
const showTemplateDialog = ref(false)

const templateList = [
  {
    name: '月度灭火器检查表',
    deviceType: '灭火器',
    itemCount: 6,
    cycle: '每月',
    lastUpdate: '2026-07-01',
  },
  {
    name: '月度消火栓检查表',
    deviceType: '消火栓',
    itemCount: 8,
    cycle: '每月',
    lastUpdate: '2026-07-01',
  },
  {
    name: '季度烟感检测表',
    deviceType: '烟感',
    itemCount: 4,
    cycle: '每季度',
    lastUpdate: '2026-07-01',
  },
  {
    name: '季度喷淋检测表',
    deviceType: '喷淋',
    itemCount: 5,
    cycle: '每季度',
    lastUpdate: '2026-07-01',
  },
  {
    name: '年度消防联动测试表',
    deviceType: '全部',
    itemCount: 12,
    cycle: '每年',
    lastUpdate: '2026-01-05',
  },
]

// ---------- Actions ----------
function handleDispatch() {
  ElMessage.info('派发任务功能开发中')
}

function viewTask(row) {
  ElMessage.info(`查看任务: ${row.deviceCode} ${row.device}`)
}
</script>

<style scoped>
.task-management {
  height: 100%;
}

/* ---------- Tabs Section ---------- */
.task-tabs-section {
  background: #fff;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
  padding: 0 16px;
  margin-bottom: 12px;
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.tab-badge {
  margin-top: 2px;
}

.overdue-badge :deep(.el-badge__content) {
  background-color: #f56c6c;
}

/* ---------- Content Section ---------- */
.task-content {
  background: #fff;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
  overflow: hidden;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.content-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.content-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ---------- Device cell ---------- */
.device-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.device-code {
  font-weight: 600;
  color: #1890ff;
  background: #e6f7ff;
  padding: 0 6px;
  border-radius: 3px;
  font-size: 12px;
  line-height: 20px;
}

/* ---------- Row highlights ---------- */
:deep(.overdue-row) {
  --el-table-tr-bg-color: #fef0f0;
}

:deep(.overdue-row:hover > td) {
  background-color: #fde2e2 !important;
}
</style>
