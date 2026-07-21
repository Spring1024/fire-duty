<template>
  <div class="device-management">
    <div class="device-layout">
      <!-- Left Tree Panel -->
      <div class="device-tree-panel">
        <div class="tree-header">
          <span>设备位置</span>
        </div>
        <div class="tree-search">
          <el-input
            v-model="treeFilter"
            placeholder="搜索位置..."
            size="small"
            clearable
            prefix-icon="Search"
          />
        </div>
        <el-tree
          ref="treeRef"
          :data="buildingTree"
          :props="treeProps"
          node-key="id"
          :highlight-current="true"
          :filter-node-method="filterNode"
          default-expand-all
          @node-click="handleNodeClick"
        >
          <template #default="{ node, data }">
            <span class="custom-tree-node">
              <span class="tree-node-label">{{ data.label }}</span>
              <span class="tree-node-count">{{ data.count }}台</span>
            </span>
          </template>
        </el-tree>
      </div>

      <!-- Right Table Panel -->
      <div class="device-table-panel">
        <!-- Toolbar -->
        <div class="table-toolbar">
          <div class="toolbar-left">
            <el-input
              v-model="searchQuery"
              placeholder="搜索设备编码/名称"
              size="default"
              clearable
              style="width: 240px"
              prefix-icon="Search"
            />
            <el-button type="primary" @click="handleSearch">搜索</el-button>
          </div>
          <div class="toolbar-right">
            <el-button plain @click="handleImport">导入</el-button>
            <el-button type="primary" @click="handleAddDevice">新增设备</el-button>
          </div>
        </div>

        <!-- Table -->
        <el-table
          :data="filteredDeviceList"
          style="width: 100%"
          border
          stripe
          highlight-current-row
          :row-class-name="tableRowClassName"
          @row-click="handleRowClick"
          v-loading="loading"
        >
          <el-table-column prop="code" label="设备编码" width="130" />
          <el-table-column prop="name" label="名称" min-width="160" />
          <el-table-column prop="type" label="类型" width="120">
            <template #default="{ row }">
              <el-tag
                :type="typeTagType(row.type)"
                size="small"
                effect="plain"
              >
                {{ row.type }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="location" label="位置" min-width="140" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="{ row }">
              <span class="status-indicator">
                <span
                  class="status-dot"
                  :style="{ backgroundColor: statusColor(row.status) }"
                ></span>
                <span :style="{ color: statusColor(row.status) }">
                  {{ row.status }}
                </span>
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="lastCheck" label="最近检查" width="140" />
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="text" size="small" @click.stop="viewDetail(row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- Device Detail Drawer -->
    <el-drawer
      v-model="drawerVisible"
      title="设备详情"
      size="400px"
    >
      <template v-if="currentDevice">
        <div class="detail-item">
          <span class="detail-label">设备编码</span>
          <span class="detail-value">{{ currentDevice.code }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">名称</span>
          <span class="detail-value">{{ currentDevice.name }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">类型</span>
          <span class="detail-value">{{ currentDevice.type }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">位置</span>
          <span class="detail-value">{{ currentDevice.location }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">状态</span>
          <span class="detail-value">
            <span
              class="status-dot"
              :style="{ backgroundColor: statusColor(currentDevice.status) }"
            ></span>
            {{ currentDevice.status }}
          </span>
        </div>
        <div class="detail-item">
          <span class="detail-label">最近检查日期</span>
          <span class="detail-value">{{ currentDevice.lastCheck }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">制造商</span>
          <span class="detail-value">{{ currentDevice.manufacturer || '—' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">安装日期</span>
          <span class="detail-value">{{ currentDevice.installDate || '—' }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">最后维护日期</span>
          <span class="detail-value">{{ currentDevice.lastMaintenance || '—' }}</span>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useDeviceStore } from '@/stores/device'
import { createDeviceAPI, importDevicesAPI } from '@/api/device'

const deviceStore = useDeviceStore()

// ---------- Tree Data ----------
const treeFilter = ref('')
const treeRef = ref(null)

const treeProps = {
  children: 'children',
  label: 'label',
}

const buildingTree = computed(() => deviceStore.treeData)

const currentBuilding = ref('')

function filterNode(value, data) {
  if (!value) return data
  return data.label && data.label.includes(value)
}

function handleNodeClick(data) {
  currentBuilding.value = data.label || ''
  // If the node has a gridId, filter devices by it
  const params = {}
  if (data.gridId) {
    params.gridId = data.gridId
  }
  if (searchQuery.value.trim()) {
    params.keyword = searchQuery.value.trim()
  }
  deviceStore.fetchDevices(params)
}

// ---------- Device Table Data ----------
const searchQuery = ref('')
const loading = computed(() => deviceStore.loading)

const deviceList = computed(() => deviceStore.deviceList)

const filteredDeviceList = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()
  if (!query) return deviceList.value
  return deviceList.value.filter(
    (d) => (d.code && d.code.toLowerCase().includes(query)) || (d.name && d.name.toLowerCase().includes(query))
  )
})

function tableRowClassName({ row }) {
  if (row.status === '故障') {
    return 'fault-row'
  }
  return ''
}

function typeTagType(type) {
  const map = {
    '灭火器': '',
    '消火栓': 'success',
    '烟感': 'warning',
    '喷淋': '',
  }
  return map[type] || ''
}

function statusColor(status) {
  if (status === '正常') return '#67c23a'
  if (status === '故障') return '#f56c6c'
  if (status === '维护中') return '#e6a23c'
  return '#909399'
}

// ---------- Actions ----------
function handleSearch() {
  const params = {}
  if (searchQuery.value.trim()) {
    params.keyword = searchQuery.value.trim()
  }
  if (currentBuilding.value && currentBuilding.value !== '全部') {
    params.location = currentBuilding.value
  }
  deviceStore.fetchDevices(params)
}

async function handleImport() {
  // Create a file input element
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = '.xlsx,.xls,.csv'
  input.onchange = async (e) => {
    const file = e.target.files[0]
    if (!file) return
    try {
      await deviceStore.importDevices(file)
      ElMessage.success('导入成功')
      deviceStore.fetchDevices()
    } catch (err) {
      ElMessage.error('导入失败')
    }
  }
  input.click()
}

async function handleAddDevice() {
  // A simple prompt-style add — in production this would open a dialog
  ElMessage.info('新增设备功能 — 可通过 /devices POST 接口提交')
  // Example of how to call the API:
  // try {
  //   await deviceStore.createDevice({ name: '...', type: '...', ... })
  //   ElMessage.success('设备创建成功')
  //   deviceStore.fetchDevices()
  // } catch (err) {
  //   ElMessage.error('设备创建失败')
  // }
}

// ---------- Drawer ----------
const drawerVisible = ref(false)
const currentDevice = ref(null)

function handleRowClick(row) {
  currentDevice.value = row
  drawerVisible.value = true
}

function viewDetail(row) {
  currentDevice.value = row
  drawerVisible.value = true
}

// ---------- Lifecycle ----------
onMounted(() => {
  deviceStore.fetchDevices()
  deviceStore.fetchTree()
})
</script>

<style scoped>
.device-management {
  height: 100%;
}

.device-layout {
  display: flex;
  height: 100%;
  gap: 12px;
}

/* ---------- Left Tree Panel ---------- */
.device-tree-panel {
  width: 260px;
  min-width: 260px;
  background: #fff;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.tree-header {
  padding: 14px 16px 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  border-bottom: 1px solid #f0f0f0;
}

.tree-search {
  padding: 10px 12px;
}

.tree-search .el-input {
  --el-input-border-color: #e8e8e8;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  padding-right: 8px;
}

.tree-node-label {
  color: #333;
}

.tree-node-count {
  font-size: 12px;
  color: #999;
  background: #f5f5f5;
  padding: 0 8px;
  border-radius: 10px;
  line-height: 20px;
}

:deep(.el-tree-node__content) {
  height: 38px;
  padding: 0 8px;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #e6f7ff;
}

:deep(.el-tree-node.is-current > .el-tree-node__content .tree-node-label) {
  color: #1890ff;
  font-weight: 600;
}

/* ---------- Right Table Panel ---------- */
.device-table-panel {
  flex: 1;
  background: #fff;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
  flex-wrap: wrap;
  gap: 10px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ---------- Status indicators ---------- */
.status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

/* ---------- Table row highlight ---------- */
:deep(.fault-row) {
  --el-table-tr-bg-color: #fef0f0;
}

:deep(.fault-row:hover > td) {
  background-color: #fde2e2 !important;
}

/* ---------- Drawer details ---------- */
.detail-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}

.detail-label {
  width: 120px;
  font-size: 13px;
  color: #999;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  font-size: 13px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 6px;
}
</style>
