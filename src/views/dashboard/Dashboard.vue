<template>
  <div class="dashboard">
    <!-- Top Stats Cards -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="12" :sm="12" :md="6" v-for="card in statCards" :key="card.label">
        <el-card shadow="hover" class="stat-card" :style="{ borderTop: `3px solid ${card.color}` }">
          <div class="stat-card-inner">
            <div class="stat-info">
              <div class="stat-label">{{ card.label }}</div>
              <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
            </div>
            <div class="stat-icon" :style="{ color: card.color, background: card.bgColor }">
              <el-icon :size="28"><component :is="card.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Middle Row: Map + Completion Ring -->
    <el-row :gutter="16" class="middle-row">
      <!-- Map Placeholder -->
      <el-col :xs="24" :md="16">
        <el-card shadow="hover" class="map-card">
          <template #header>
            <div class="card-header">
              <span>🗺️ 网格地图</span>
            </div>
          </template>
          <div class="map-placeholder">
            <div class="map-message">
              <div class="map-icon">🗺️</div>
              <p>高德地图集成区域</p>
              <p class="map-sub">展示消防设备分布与网格覆盖范围</p>
            </div>
            <!-- Simulated map dots -->
            <div class="map-dots">
              <span class="dot dot-green"></span>
              <span class="dot dot-red"></span>
              <span class="dot dot-yellow"></span>
              <span class="dot dot-green"></span>
              <span class="dot dot-green"></span>
              <span class="dot dot-yellow"></span>
              <span class="dot dot-red"></span>
              <span class="dot dot-green"></span>
              <span class="dot dot-green"></span>
              <span class="dot dot-yellow"></span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- Completion Ring -->
      <el-col :xs="24" :md="8">
        <el-card shadow="hover" class="completion-card">
          <template #header>
            <div class="card-header">
              <span>今日完成率</span>
            </div>
          </template>
          <div class="completion-body">
            <el-progress
              type="circle"
              :percentage="stats.completionRate"
              :stroke-width="10"
              :width="160"
              color="#1890ff"
              stroke-linecap="round"
            >
              <template #default>
                <span class="completion-value">{{ stats.completionRate }}%</span>
              </template>
            </el-progress>
            <div class="completion-stats">
              <div class="comp-stat">
                <span class="comp-label">计划任务</span>
                <span class="comp-num">{{ stats.plannedTasks }}</span>
              </div>
              <div class="comp-stat">
                <span class="comp-label">已完成</span>
                <span class="comp-num" style="color: #1890ff;">{{ stats.completedTasks }}</span>
              </div>
              <div class="comp-stat">
                <span class="comp-label">未完成</span>
                <span class="comp-num" style="color: #f56c6c;">{{ stats.overdueTasks }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Alert List -->
    <el-card shadow="hover" class="alert-card">
      <template #header>
        <div class="card-header">
          <span>⚠️ 预警信息</span>
          <el-tag type="danger" size="small" effect="dark">{{ alerts.length }} 条待处理</el-tag>
        </div>
      </template>
      <div class="alert-list">
        <div
          v-for="(item, index) in alerts"
          :key="index"
          class="alert-item"
          :class="{ 'no-border': index === alerts.length - 1 }"
        >
          <div class="alert-left">
            <el-tag
              :type="item.severity === 'P0' ? 'danger' : item.severity === 'P1' ? 'warning' : 'info'"
              size="small"
              effect="dark"
              class="alert-tag"
            >
              {{ item.severity }}
            </el-tag>
            <span class="alert-desc">{{ item.description }}</span>
          </div>
          <div class="alert-time">{{ item.time }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { Monitor, TopRight, Check, WarningFilled } from '@element-plus/icons-vue'
import { useDashboardStore } from '@/stores/dashboard'

const dashboardStore = useDashboardStore()
const { stats, alerts } = dashboardStore

const statCards = computed(() => [
  {
    label: '设备总数',
    value: String(1),
    color: '#1890ff',
    bgColor: '#e6f7ff',
    icon: Monitor,
  },
  {
    label: '在线率',
    value: 4 + '%',
    color: '#52c41a',
    bgColor: '#f6ffed',
    icon: TopRight,
  },
  {
    label: '今日巡检',
    value: String(2),
    color: '#1890ff',
    bgColor: '#e6f7ff',
    icon: Check,
  },
  {
    label: '待整改',
    value: String(3),
    color: '#f56c6c',
    bgColor: '#fff1f0',
    icon: WarningFilled,
  },
])

onMounted(async () => {
  await Promise.all([dashboardStore.fetchStats(), dashboardStore.fetchAlerts()])
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

/* ---- Stats Cards ---- */
.stat-cards {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 6px;
}

.stat-card-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 13px;
  color: #999;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* ---- Middle Row ---- */
.middle-row {
  margin-bottom: 16px;
}

/* Card headers */
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 600;
}

/* Map */
.map-placeholder {
  height: 260px;
  background: linear-gradient(135deg, #e6f7ff 0%, #f0faff 100%);
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  border: 1px dashed #91d5ff;
  color: #333;
}

.map-message {
  text-align: center;
  margin-bottom: 16px;
}

.map-icon {
  font-size: 40px;
  margin-bottom: 8px;
}

.map-sub {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.map-dots {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: center;
  max-width: 200px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}

.dot-green {
  background: #52c41a;
  box-shadow: 0 0 6px rgba(82, 196, 26, 0.5);
}

.dot-red {
  background: #f56c6c;
  box-shadow: 0 0 6px rgba(245, 108, 108, 0.5);
}

.dot-yellow {
  background: #faad14;
  box-shadow: 0 0 6px rgba(250, 173, 20, 0.5);
}

/* Completion Ring */
.completion-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 0;
}

.completion-value {
  font-size: 22px;
  font-weight: 700;
  color: #1890ff;
}

.completion-stats {
  width: 100%;
  margin-top: 20px;
}

.comp-stat {
  display: flex;
  justify-content: space-between;
  padding: 6px 4px;
  font-size: 13px;
  border-bottom: 1px solid #f0f0f0;
}

.comp-stat:last-child {
  border-bottom: none;
}

.comp-label {
  color: #999;
}

.comp-num {
  font-weight: 600;
}

/* ---- Alert List ---- */
.alert-card {
  border-radius: 6px;
}

.alert-list {
  display: flex;
  flex-direction: column;
}

.alert-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.alert-item.no-border {
  border-bottom: none;
}

.alert-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.alert-tag {
  flex-shrink: 0;
  font-weight: 600;
}

.alert-desc {
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.alert-time {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
  margin-left: 16px;
}
</style>
