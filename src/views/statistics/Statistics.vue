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
              points="85,152 145,128 205,88 265,96 325,80"
              fill="none"
              stroke="#409eff"
              stroke-width="2.5"
              stroke-linejoin="round"
              stroke-linecap="round"
            />

            <!-- Area under line -->
            <polygon
              points="85,152 85,170 145,170 145,128 205,170 205,88 265,170 265,96 325,170 325,80"
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

            <!-- Dots -->
            <circle cx="85" cy="152" r="4" fill="#409eff" stroke="#fff" stroke-width="2" />
            <text x="85" y="170" text-anchor="middle" fill="#666" font-size="10">5月</text>
            <text x="85" y="148" text-anchor="middle" fill="#409eff" font-size="11" font-weight="bold">60%</text>

            <circle cx="145" cy="128" r="4" fill="#409eff" stroke="#fff" stroke-width="2" />
            <text x="145" y="170" text-anchor="middle" fill="#666" font-size="10">6月</text>
            <text x="145" y="124" text-anchor="middle" fill="#409eff" font-size="11" font-weight="bold">70%</text>

            <circle cx="205" cy="88" r="4" fill="#409eff" stroke="#fff" stroke-width="2" />
            <text x="205" y="170" text-anchor="middle" fill="#666" font-size="10">7月</text>
            <text x="205" y="84" text-anchor="middle" fill="#409eff" font-size="11" font-weight="bold">85%</text>

            <circle cx="265" cy="96" r="4" fill="#409eff" stroke="#fff" stroke-width="2" />
            <text x="265" y="170" text-anchor="middle" fill="#666" font-size="10">8月</text>
            <text x="265" y="92" text-anchor="middle" fill="#409eff" font-size="11" font-weight="bold">80%</text>

            <circle cx="325" cy="80" r="4" fill="#409eff" stroke="#fff" stroke-width="2" />
            <text x="325" y="170" text-anchor="middle" fill="#666" font-size="10">9月</text>
            <text x="325" y="76" text-anchor="middle" fill="#409eff" font-size="11" font-weight="bold">88%</text>
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
            <!-- Bars -->
            <!-- 灭火器 - red -->
            <rect x="40" y="40" width="50" height="120" rx="3" fill="#f56c6c" />
            <text x="65" y="36" text-anchor="middle" fill="#f56c6c" font-size="11" font-weight="bold">60%</text>
            <text x="65" y="173" text-anchor="middle" fill="#666" font-size="10">灭火器</text>

            <!-- 消火栓 - amber -->
            <rect x="110" y="60" width="50" height="100" rx="3" fill="#e6a23c" />
            <text x="135" y="56" text-anchor="middle" fill="#e6a23c" font-size="11" font-weight="bold">40%</text>
            <text x="135" y="173" text-anchor="middle" fill="#666" font-size="10">消火栓</text>

            <!-- 烟感 - blue -->
            <rect x="180" y="70" width="50" height="90" rx="3" fill="#409eff" />
            <text x="205" y="66" text-anchor="middle" fill="#409eff" font-size="11" font-weight="bold">30%</text>
            <text x="205" y="173" text-anchor="middle" fill="#666" font-size="10">烟感</text>

            <!-- 喷淋 - green -->
            <rect x="250" y="80" width="50" height="80" rx="3" fill="#67c23a" />
            <text x="275" y="76" text-anchor="middle" fill="#67c23a" font-size="11" font-weight="bold">20%</text>
            <text x="275" y="173" text-anchor="middle" fill="#666" font-size="10">喷淋</text>

            <!-- 其他 - gray -->
            <rect x="320" y="85" width="50" height="75" rx="3" fill="#909399" />
            <text x="345" y="81" text-anchor="middle" fill="#909399" font-size="11" font-weight="bold">15%</text>
            <text x="345" y="173" text-anchor="middle" fill="#666" font-size="10">其他</text>
          </svg>
        </div>
      </el-card>
    </div>

    <!-- Summary -->
    <div class="summary-row">
      <el-card shadow="never" class="summary-card">
        <div class="summary-content">
          <span class="summary-item">📌 灭火器相关问题占比最高（35.0%）</span>
          <span class="summary-divider" />
          <span class="summary-item highlight">✅ 整体合规率: 93.5%</span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
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
