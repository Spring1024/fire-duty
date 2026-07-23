<template>
  <div class="statistics-page">
    <!-- Toolbar -->
    <div class="stat-toolbar">
      <span class="toolbar-title">合规率趋势 近6个月</span>
      <el-button type="primary" size="default">导出报表</el-button>
    </div>

    <!-- Chart Row -->
    <div class="chart-row">
      <!-- Line Chart -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <span class="card-title">月度合规率</span>
        </template>
        <div class="chart-container">
          <svg viewBox="0 0 400 200" class="line-chart-svg">
            <!-- Grid lines -->
            <line x1="50" y1="40" x2="380" y2="40" stroke="#f0f0f0" stroke-dasharray="4,2" />
            <line x1="50" y1="80" x2="380" y2="80" stroke="#f0f0f0" stroke-dasharray="4,2" />
            <line x1="50" y1="120" x2="380" y2="120" stroke="#f0f0f0" stroke-dasharray="4,2" />
            <line x1="50" y1="160" x2="380" y2="160" stroke="#f0f0f0" stroke-dasharray="4,2" />

            <!-- Y-axis labels -->
            <text x="45" y="44" text-anchor="end" fill="#999" font-size="10">100%</text>
            <text x="45" y="84" text-anchor="end" fill="#999" font-size="10">75%</text>
            <text x="45" y="124" text-anchor="end" fill="#999" font-size="10">50%</text>
            <text x="45" y="164" text-anchor="end" fill="#999" font-size="10">25%</text>

            <!-- Polyline -->
            <polyline
              :points="linePoints"
              fill="none"
              stroke="#409eff"
              stroke-width="2.5"
              stroke-linejoin="round"
              stroke-linecap="round"
            />

            <!-- Area under line -->
            <polygon
              :points="areaPoints"
              fill="url(#lineGradient)"
              opacity="0.15"
            />

            <!-- Gradient def -->
            <defs>
              <linearGradient id="lineGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#409eff" />
                <stop offset="100%" stop-color="#409eff" stop-opacity="0" />
              </linearGradient>
            </defs>

            <!-- Data points -->
            <template v-for="(item, i) in compliance" :key="i">
              <circle
                :cx="getLineX(i)"
                :cy="getLineY(item.rate)"
                r="4"
                fill="#409eff"
                stroke="#fff"
                stroke-width="2"
              />
              <text
                :x="getLineX(i)"
                y="170"
                text-anchor="middle"
                fill="#666"
                font-size="10"
              >{{ item.month }}</text>
              <text
                :x="getLineX(i)"
                :y="getLineY(item.rate) - 4"
                text-anchor="middle"
                fill="#409eff"
                font-size="11"
                font-weight="bold"
              >{{ item.rate }}%</text>
            </template>
          </svg>
        </div>
      </el-card>

      <!-- Bar Chart -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <span class="card-title">隐患类型分布</span>
        </template>
        <div class="chart-container">
          <svg viewBox="0 0 400 200" class="bar-chart-svg">
            <template v-for="(item, i) in hazardDistribution" :key="i">
              <!-- Bar -->
              <rect
                :x="getBarX(i)"
                :y="getBarY(item.percentage)"
                width="50"
                :height="getBarHeight(item.percentage)"
                rx="3"
                :fill="item.color || barColors[i % barColors.length]"
              />
              <!-- Percentage label -->
              <text
                :x="getBarX(i) + 25"
                :y="getBarY(item.percentage) - 4"
                text-anchor="middle"
                :fill="item.color || barColors[i % barColors.length]"
                font-size="11"
                font-weight="bold"
              >{{ item.percentage }}%</text>
              <!-- Category label -->
              <text
                :x="getBarX(i) + 25"
                y="173"
                text-anchor="middle"
                fill="#666"
                font-size="10"
              >{{ item.name }}</text>
            </template>
          </svg>
        </div>
      </el-card>
    </div>

    <!-- Summary -->
    <div class="summary-row">
      <el-card shadow="never" class="summary-card">
        <div class="summary-content">
          <span class="summary-item">📌 {{ summary.maxHazardType }}相关问题占比最高（{{ summary.maxHazardPercentage }}%）</span>
          <span class="summary-divider" />
          <span class="summary-item highlight">✅ 整体合规率: {{ summary.overallComplianceRate }}%</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useStatisticsStore } from '@/stores/statistics'

const statisticsStore = useStatisticsStore()
const { compliance, hazardDistribution, summary } = storeToRefs(statisticsStore)

const barColors = ['#f56c6c', '#e6a23c', '#409eff', '#67c23a', '#909399']

// Line chart: X range 50..380, N points evenly spaced
function getLineX(index) {
  const n = compliance.value.length
  if (n <= 1) return 215
  return 50 + (index * 330) / (n - 1)
}

// Y range: 100% → 40 (top), 0% → 170 (bottom), scale = 130px
function getLineY(rate) {
  return 170 - (rate / 100) * 130
}

const linePoints = computed(() =>
  compliance.value.map((d, i) => `${getLineX(i)},${getLineY(d.rate)}`).join(' ')
)

const areaPoints = computed(() => {
  const pts = compliance.value.map((d, i) => `${getLineX(i)},${getLineY(d.rate)}`).join(' ')
  if (!compliance.value.length) return ''
  const firstX = getLineX(0)
  const lastX = getLineX(compliance.value.length - 1)
  return `${firstX},170 ${pts} ${lastX},170`
})

// Bar chart: bars centered at x-offsets: 65, 135, 205, 275, 345
// Each bar is 50px wide, so rect.x = center - 25
function getBarX(index) {
  const centers = [40, 110, 180, 250, 320]
  return (centers[index] !== undefined ? centers[index] : 320) + 0
}

function getBarY(pct) {
  // Map 100% → y=30 (top), 0% → y=170 (bottom), height 140px
  return 170 - Math.min(pct, 100) * 1.4
}

function getBarHeight(pct) {
  return Math.min(pct, 100) * 1.4
}

onMounted(async () => {
  await Promise.all([
    statisticsStore.fetchCompliance({ months: 6 }),
    statisticsStore.fetchHazardDistribution(),
    statisticsStore.fetchSummary(),
  ])
})
</script>

<style scoped>
.statistics-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
}
.toolbar-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.chart-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chart-card :deep(.el-card__header) {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}
.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.chart-container {
  width: 100%;
  padding: 8px 0;
}
.line-chart-svg,
.bar-chart-svg {
  width: 100%;
  height: 220px;
}

.summary-row {
  width: 100%;
}
.summary-card {
  background: #f0f5ff;
  border: 1px solid #d6e4ff;
  border-radius: 8px;
}
.summary-content {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
}
.summary-item {
  color: #333;
}
.summary-item.highlight {
  color: #52c41a;
  font-weight: 600;
  font-size: 15px;
}
.summary-divider {
  width: 1px;
  height: 20px;
  background: #d6e4ff;
}
</style>
