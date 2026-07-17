<template>
  <div class="rectification-management">
    <!-- Tabs -->
    <el-tabs v-model="activeTab" class="rect-tabs">
      <el-tab-pane label="待派发" name="pending">
        <template #label>
          <span>待派发 <el-badge :value="3" :hidden="false" /></span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="整改中" name="ongoing">
        <template #label>
          <span>整改中 <el-badge :value="5" :hidden="false" /></span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="待复核" name="review">
        <template #label>
          <span>待复核 <el-badge :value="2" :hidden="false" /></span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="已闭环" name="closed">
        <template #label>
          <span>已闭环 <el-badge :value="128" :hidden="false" /></span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- Info bar -->
    <div class="info-bar">
      <span class="info-left">共 8 项待处理</span>
      <span class="info-right">⏰ 超时 2 项</span>
    </div>

    <!-- Table -->
    <el-table :data="rectificationList" stripe style="width: 100%" @row-click="handleRowClick">
      <el-table-column prop="description" label="隐患描述" min-width="200" />
      <el-table-column prop="deviceCode" label="设备" width="120" />
      <el-table-column label="级别" width="100">
        <template #default="{ row }">
          <el-tag :type="row.levelType" effect="dark" size="small">{{ row.level }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="foundTime" label="发现时间" width="140" />
      <el-table-column prop="assignee" label="负责人" width="100" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.statusType" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- Detail Drawer -->
    <el-drawer
      v-model="drawerVisible"
      title="隐患详情"
      direction="rtl"
      size="480px"
      :show-close="true"
    >
      <template #header="{ close, titleId, titleClass }">
        <div class="drawer-header">
          <h4 :id="titleId" :class="titleClass">隐患详情</h4>
          <el-icon class="drawer-close" @click="close"><Close /></el-icon>
        </div>
      </template>

      <div class="drawer-body">
        <!-- Basic info -->
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="隐患描述">{{ currentRect?.description }}</el-descriptions-item>
          <el-descriptions-item label="设备信息">{{ currentRect?.deviceCode }}</el-descriptions-item>
          <el-descriptions-item label="级别">
            <el-tag :type="currentRect?.levelType" effect="dark" size="small">{{ currentRect?.level }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发现时间">{{ currentRect?.foundTime }}</el-descriptions-item>
        </el-descriptions>

        <!-- Timeline -->
        <div class="section-title">处置时间线</div>
        <el-timeline class="rect-timeline">
          <el-timeline-item timestamp="07-11 10:30" placement="top" type="primary">
            巡检员张三提交异常
          </el-timeline-item>
          <el-timeline-item timestamp="07-11 10:32" placement="top" type="primary">
            系统自动生成本整改工单
          </el-timeline-item>
          <el-timeline-item timestamp="待处理" placement="top" type="danger">
            🔴 待派发至责任人
          </el-timeline-item>
        </el-timeline>

        <!-- Photo compare -->
        <div class="section-title">整改照片对比</div>
        <div class="photo-compare">
          <div class="photo-box">
            <div class="photo-label">整改前照片</div>
            <div class="photo-placeholder">
              <el-icon :size="32"><PictureFilled /></el-icon>
              <span>点击上传整改前照片</span>
            </div>
          </div>
          <div class="photo-box">
            <div class="photo-label">整改后照片</div>
            <div class="photo-placeholder">
              <el-icon :size="32"><PictureFilled /></el-icon>
              <span>点击上传整改后照片</span>
            </div>
          </div>
        </div>

        <!-- Assign dropdown -->
        <div class="section-title">指派责任人</div>
        <el-select v-model="selectedAssignee" placeholder="请选择整改负责人" style="width: 100%">
          <el-option label="李四（中网格组长）" value="李四" />
          <el-option label="王五（维保人员）" value="王五" />
        </el-select>
      </div>

      <!-- Bottom buttons -->
      <template #footer>
        <div class="drawer-footer">
          <el-button type="primary" @click="handleDispatch">派发工单</el-button>
          <el-button @click="drawerVisible = false">关闭</el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Close, PictureFilled } from '@element-plus/icons-vue'

const activeTab = ref('pending')
const drawerVisible = ref(false)
const selectedAssignee = ref('')
const currentRect = ref(null)

const rectificationList = ref([
  {
    description: '灭火器压力表指针在红区',
    deviceCode: 'EXT-001',
    level: '紧急',
    levelType: 'danger',
    foundTime: '07-11 10:30',
    assignee: '—',
    status: '待派发',
    statusType: 'warning',
  },
  {
    description: 'B栋烟感信号丢失',
    deviceCode: 'SEN-023',
    level: '紧急',
    levelType: 'danger',
    foundTime: '07-10 15:20',
    assignee: '王五',
    status: '已超时',
    statusType: 'danger',
  },
  {
    description: '消火栓箱门损坏',
    deviceCode: 'HYD-008',
    level: '一般',
    levelType: '',
    foundTime: '07-10 09:15',
    assignee: '李四',
    status: '整改中',
    statusType: 'primary',
  },
])

function handleRowClick(row) {
  currentRect.value = row
  drawerVisible.value = true
}

function handleDispatch() {
  if (!selectedAssignee.value) {
    ElMessage.warning('请选择整改负责人')
    return
  }
  ElMessage.success(`已派发给 ${selectedAssignee.value}`)
  drawerVisible.value = false
}
</script>

<style scoped>
.rectification-management {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.rect-tabs :deep(.el-badge) {
  margin-left: 4px;
}
.rect-tabs :deep(.el-badge__content) {
  position: relative;
  top: -2px;
}

.info-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0 16px;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}
.info-left {
  color: #666;
}
.info-right {
  color: #f56c6c;
  font-weight: 600;
}

/* Drawer */
.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.drawer-close {
  cursor: pointer;
  font-size: 18px;
}
.drawer-body {
  padding: 0 4px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 20px 0 12px;
  padding-left: 8px;
  border-left: 3px solid #1890ff;
}
.rect-timeline {
  padding-left: 8px;
}
.photo-compare {
  display: flex;
  gap: 12px;
}
.photo-box {
  flex: 1;
}
.photo-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}
.photo-placeholder {
  height: 120px;
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 13px;
  gap: 6px;
  cursor: pointer;
  transition: border-color 0.2s;
}
.photo-placeholder:hover {
  border-color: #1890ff;
  color: #1890ff;
}
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
