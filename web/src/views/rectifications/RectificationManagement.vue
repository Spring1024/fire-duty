<template>
  <div class="rectification-management">
    <!-- Tabs -->
    <el-tabs v-model="activeTab" class="rect-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="待派发" name="pending">
        <template #label>
          <span>待派发 <el-badge :value="store.tabCounts.pending" :hidden="false" /></span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="整改中" name="ongoing">
        <template #label>
          <span>整改中 <el-badge :value="store.tabCounts.ongoing" :hidden="false" /></span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="待复核" name="review">
        <template #label>
          <span>待复核 <el-badge :value="store.tabCounts.review" :hidden="false" /></span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="已闭环" name="closed">
        <template #label>
          <span>已闭环 <el-badge :value="store.tabCounts.closed" :hidden="false" /></span>
        </template>
      </el-tab-pane>
    </el-tabs>

    <!-- Info bar -->
    <div class="info-bar">
      <span class="info-left">共 {{ pendingCount }} 项待处理</span>
      <span v-if="overdueCount > 0" class="info-right">⏰ 超时 {{ overdueCount }} 项</span>
    </div>

    <!-- Table -->
    <el-table
      :data="store.rectList"
      v-loading="store.loading"
      stripe
      style="width: 100%"
      @row-click="handleRowClick"
    >
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
          <el-descriptions-item label="隐患描述">{{ store.currentRect?.description }}</el-descriptions-item>
          <el-descriptions-item label="设备信息">{{ store.currentRect?.deviceCode }}</el-descriptions-item>
          <el-descriptions-item label="级别">
            <el-tag :type="store.currentRect?.levelType" effect="dark" size="small">{{ store.currentRect?.level }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发现时间">{{ store.currentRect?.foundTime }}</el-descriptions-item>
        </el-descriptions>

        <!-- Timeline -->
        <div class="section-title">处置时间线</div>
        <el-timeline class="rect-timeline">
          <el-timeline-item
            v-for="(item, index) in store.timeline"
            :key="index"
            :timestamp="item.timestamp"
            placement="top"
            :type="item.type"
          >
            {{ item.text }}
          </el-timeline-item>
          <el-timeline-item
            v-if="store.currentRect?.status === '待派发'"
            timestamp="待处理"
            placement="top"
            type="danger"
          >
            🔴 待派发至责任人
          </el-timeline-item>
        </el-timeline>

        <!-- Photo compare -->
        <div class="section-title">整改照片对比</div>
        <div class="photo-compare">
          <div class="photo-box">
            <div class="photo-label">整改前照片</div>
            <div
              v-if="store.photos?.before"
              class="photo-image"
              @click="handleUploadPhoto('before')"
            >
              <el-image :src="store.photos.before" fit="cover" style="width:100%;height:120px;border-radius:6px;" />
            </div>
            <div v-else class="photo-placeholder" @click="handleUploadPhoto('before')">
              <el-icon :size="32"><PictureFilled /></el-icon>
              <span>点击上传整改前照片</span>
            </div>
          </div>
          <div class="photo-box">
            <div class="photo-label">整改后照片</div>
            <div
              v-if="store.photos?.after"
              class="photo-image"
              @click="handleUploadPhoto('after')"
            >
              <el-image :src="store.photos.after" fit="cover" style="width:100%;height:120px;border-radius:6px;" />
            </div>
            <div v-else class="photo-placeholder" @click="handleUploadPhoto('after')">
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
          <el-button type="primary" :loading="submitting" @click="handleDispatch">派发工单</el-button>
          <el-button @click="drawerVisible = false">关闭</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- Hidden file input for photo upload -->
    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      style="display:none"
      @change="onPhotoSelected"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Close, PictureFilled } from '@element-plus/icons-vue'
import { useRectificationStore } from '@/stores/rectification'

const store = useRectificationStore()

const activeTab = ref('pending')
const drawerVisible = ref(false)
const selectedAssignee = ref('')
const submitting = ref(false)
const fileInputRef = ref(null)
const photoType = ref('before')

const pendingCount = computed(() => {
  return store.tabCounts.pending + store.tabCounts.ongoing + store.tabCounts.review
})

const overdueCount = computed(() => {
  if (!store.rectList || store.rectList.length === 0) return 0
  return store.rectList.filter((r) => r.status === '已超时').length
})

onMounted(() => {
  store.fetchRectifications({ tab: activeTab.value })
})

function handleTabChange(tab) {
  store.fetchRectifications({ tab })
}

async function handleRowClick(row) {
  selectedAssignee.value = ''
  await store.fetchRectById(row.id)
  drawerVisible.value = true
}

function handleUploadPhoto(type) {
  if (!store.currentRect) return
  photoType.value = type
  fileInputRef.value?.click()
}

async function onPhotoSelected(event) {
  const file = event.target.files?.[0]
  if (!file) return
  try {
    await store.uploadPhoto(store.currentRect.id, file, photoType.value)
    ElMessage.success('照片上传成功')
    // Refresh detail to show uploaded photo
    await store.fetchRectById(store.currentRect.id)
  } catch (err) {
    ElMessage.error('照片上传失败')
  }
  // Reset input so same file can be selected again
  event.target.value = ''
}

async function handleDispatch() {
  if (!selectedAssignee.value) {
    ElMessage.warning('请选择整改负责人')
    return
  }
  submitting.value = true
  try {
    await store.dispatchRect(store.currentRect.id, { assignee: selectedAssignee.value })
    ElMessage.success(`已派发给 ${selectedAssignee.value}`)
    drawerVisible.value = false
    // Refresh list
    await store.fetchRectifications({ tab: activeTab.value })
  } catch (err) {
    // Error already handled by interceptor
  } finally {
    submitting.value = false
  }
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
.photo-image {
  cursor: pointer;
  border-radius: 6px;
  overflow: hidden;
  transition: box-shadow 0.2s;
}
.photo-image:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
