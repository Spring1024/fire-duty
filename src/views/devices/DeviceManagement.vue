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
import { ref, computed } from 'vue'

// ---------- Tree Data ----------
const treeFilter = ref('')
const treeRef = ref(null)

const treeProps = {
  children: 'children',
  label: 'label',
}

const buildingTree = [
  {
    id: 'root',
    label: '未来科技产业园',
    count: 568,
    children: [
      {
        id: 'building-a',
        label: 'A栋',
        count: 180,
        children: [
          { id: 'a-1', label: '1层', count: 32 },
          { id: 'a-2', label: '2层', count: 28 },
          { id: 'a-3', label: '3层', count: 35 },
          { id: 'a-4', label: '4层', count: 30 },
          { id: 'a-5', label: '5层', count: 25 },
          { id: 'a-6', label: '6层', count: 30 },
        ],
      },
      {
        id: 'building-b',
        label: 'B栋',
        count: 205,
        children: [
          { id: 'b-1', label: '1层', count: 40 },
          { id: 'b-2', label: '2层', count: 35 },
          { id: 'b-3', label: '3层', count: 38 },
          { id: 'b-4', label: '4层', count: 32 },
          { id: 'b-5', label: '5层', count: 30 },
          { id: 'b-6', label: '6层', count: 30 },
        ],
      },
      {
        id: 'building-c',
        label: 'C栋',
        count: 183,
        children: [
          { id: 'c-1', label: '1层', count: 35 },
          { id: 'c-2', label: '2层', count: 30 },
          { id: 'c-3', label: '3层', count: 32 },
          { id: 'c-4', label: '4层', count: 28 },
          { id: 'c-5', label: '5层', count: 28 },
          { id: 'c-6', label: '6层', count: 30 },
        ],
      },
    ],
  },
]

const currentBuilding = ref('')

function filterNode(value, data) {
  if (!value) return true
  return data.label.includes(value)
}

function handleNodeClick(data) {
  currentBuilding.value = data.label
}

// ---------- Device Table Data ----------
const searchQuery = ref('')
const loading = ref(false)

const deviceList = ref([
  {
    code: 'EXT-001',
    name: '3层走廊灭火器',
    type: '灭火器',
    location: 'A栋-3层',
    status: '正常',
    lastCheck: '2026-07-10',
    manufacturer: '中消安科',
    installDate: '2025-03-15',
    lastMaintenance: '2026-06-15',
  },
  {
    code: 'HYD-008',
    name: '东侧消火栓',
    type: '消火栓',
    location: 'A栋-1层',
    status: '正常',
    lastCheck: '2026-07-09',
    manufacturer: '天广消防',
    installDate: '2025-01-20',
    lastMaintenance: '2026-06-20',
  },
  {
    code: 'SEN-023',
    name: 'B栋烟感',
    type: '烟感',
    location: 'B栋-3层',
    status: '故障',
    lastCheck: '2026-07-08',
    manufacturer: '泰和安',
    installDate: '2025-06-10',
    lastMaintenance: '2026-05-12',
  },
  {
    code: 'SPR-015',
    name: '地下车库喷淋',
    type: '喷淋',
    location: 'B1层',
    status: '正常',
    lastCheck: '2026-07-07',
    manufacturer: '瑞泰消防',
    installDate: '2025-02-28',
    lastMaintenance: '2026-06-28',
  },
  {
    code: 'EXT-003',
    name: '大厅灭火器',
    type: '灭火器',
    location: 'A栋-1层',
    status: '正常',
    lastCheck: '2026-07-06',
    manufacturer: '中消安科',
    installDate: '2025-03-15',
    lastMaintenance: '2026-06-15',
  },
  {
    code: 'HYD-012',
    name: '西侧消火栓',
    type: '消火栓',
    location: 'C栋-2层',
    status: '正常',
    lastCheck: '2026-07-05',
    manufacturer: '天广消防',
    installDate: '2025-04-10',
    lastMaintenance: '2026-06-10',
  },
  {
    code: 'SEN-031',
    name: 'C栋温感',
    type: '烟感',
    location: 'C栋-5层',
    status: '正常',
    lastCheck: '2026-07-04',
    manufacturer: '泰和安',
    installDate: '2025-06-10',
    lastMaintenance: '2026-05-20',
  },
  {
    code: 'SPR-022',
    name: 'A栋车库喷淋',
    type: '喷淋',
    location: 'A栋-B1层',
    status: '正常',
    lastCheck: '2026-07-03',
    manufacturer: '瑞泰消防',
    installDate: '2025-02-28',
    lastMaintenance: '2026-06-28',
  },
])

const filteredDeviceList = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()
  if (!query) return deviceList.value
  return deviceList.value.filter(
    (d) => d.code.toLowerCase().includes(query) || d.name.toLowerCase().includes(query)
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
  // table is already filtered via computed
}

function handleImport() {
  ElMessage.info('导入功能开发中')
}

function handleAddDevice() {
  ElMessage.info('新增设备功能开发中')
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
