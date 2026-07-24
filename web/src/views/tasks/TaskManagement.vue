<template>
  <div class="task-management">
    <!-- Tabs Section -->
    <div class="task-tabs-section">
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="待检查" name="pending">
          <template #label>
            <span class="tab-label">
              待检查
              <el-badge :value="tabCounts.pending" :hidden="false" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已完成" name="completed">
          <template #label>
            <span class="tab-label">
              已完成
              <el-badge :value="tabCounts.completed" :hidden="false" class="tab-badge" />
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="已超时" name="overdue">
          <template #label>
            <span class="tab-label">
              已超时
              <el-badge :value="tabCounts.overdue" :hidden="false" class="tab-badge overdue-badge" />
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
        <el-table-column prop="title" label="任务名称" min-width="180" />
        <el-table-column prop="templateName" label="检查模板" min-width="180" />
        <el-table-column prop="location" label="位置" min-width="140" />
        <el-table-column prop="assignedTo" label="责任人" width="100" />
        <el-table-column prop="scheduledDate" label="计划时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.scheduledDate) }}
          </template>
        </el-table-column>
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
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTaskStore } from '@/stores/task'

const taskStore = useTaskStore()

// ---------- Tabs ----------
const activeTab = ref('pending')

const tabMap = {
  pending: { status: 'pending', label: '待检查' },
  completed: { status: 'completed', label: '已完成' },
  overdue: { status: 'overdue', label: '已超时' },
}

function handleTabClick() {
  taskStore.fetchTasks({ status: tabMap[activeTab.value]?.status })
}

// ---------- Task Data ----------
const loading = computed(() => taskStore.loading)
const tabCounts = computed(() => taskStore.tabCounts)

const currentTaskList = computed(() => {
  return taskStore.taskList
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
const templateList = computed(() => taskStore.templateList)

// ---------- Actions ----------
async function handleDispatch() {
  try {
    await ElMessageBox.confirm('确认派发任务？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info',
    })
    // 打开派发任务对话框（后续可扩展为独立弹窗组件）
    ElMessage.info('派发任务功能开发中')
  } catch {
    // 用户取消
  }
}

function formatDateTime(v) {
  if (!v) return '—'
  return String(v).replace('T', ' ').slice(0, 16)
}

function viewTask(row) {
  ElMessage.info(`查看任务: ${row.title || row.id}`)
}

// ---------- Init ----------
onMounted(() => {
  taskStore.fetchTasks({ status: tabMap[activeTab.value]?.status })
  taskStore.fetchTemplates()
})
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
