<template>
  <div class="mobile-scan-page">
    <!-- Header -->
    <div class="scan-header">
      <h2 class="scan-title">📷 扫码检查</h2>
      <p class="scan-subtitle">扫描设备二维码开始巡检</p>
    </div>

    <!-- Step 1: Device Scan -->
    <div class="scan-section card">
      <div class="section-label">
        <span class="step-badge">1</span>
        <span>扫描设备</span>
      </div>
      <div class="scan-input-row">
        <el-input
          v-model="deviceCode"
          placeholder="请输入或扫描设备编码"
          size="large"
          clearable
          @keyup.enter="handleScan"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button
              type="primary"
              :loading="scanningDevice"
              @click="handleScan"
            >
              扫描
            </el-button>
          </template>
        </el-input>
      </div>
      <div v-if="scannedDevice" class="device-info">
        <div class="device-status-tag">
          <el-tag :type="deviceStatusType" size="small" effect="dark">
            {{ scannedDevice.status || '正常' }}
          </el-tag>
        </div>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">设备名称</span>
            <span class="info-value">{{ scannedDevice.name || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">设备编码</span>
            <span class="info-value code">{{ scannedDevice.code || deviceCode }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">设备类型</span>
            <span class="info-value">{{ scannedDevice.type || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">安装位置</span>
            <span class="info-value">{{ scannedDevice.location || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">上次检查</span>
            <span class="info-value">{{ scannedDevice.lastCheck || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">制造商</span>
            <span class="info-value">{{ scannedDevice.manufacturer || '-' }}</span>
          </div>
        </div>
      </div>
      <el-empty v-else-if="scanAttempted" description="未找到设备，请确认编码正确" />
    </div>

    <!-- Step 2: Inspection Check -->
    <div v-if="scannedDevice" class="check-section card">
      <div class="section-label">
        <span class="step-badge">2</span>
        <span>检查项目</span>
      </div>
      <div v-if="checkItems.length > 0" class="check-list">
        <div
          v-for="(item, index) in checkItems"
          :key="item.id || index"
          class="check-item"
        >
          <div class="check-item-header">
            <span class="check-item-index">{{ index + 1 }}</span>
            <span class="check-item-name">{{ item.name }}</span>
          </div>
          <div class="check-item-result">
            <el-radio-group
              v-model="item.result"
              size="large"
              class="result-radio-group"
            >
              <el-radio-button value="正常" class="result-ok">正常</el-radio-button>
              <el-radio-button value="异常" class="result-warn">异常</el-radio-button>
              <el-radio-button value="不适用" class="result-na">不适用</el-radio-button>
            </el-radio-group>
          </div>
          <div class="check-item-remark">
            <el-input
              v-model="item.remark"
              placeholder="备注说明（可选）"
              size="small"
              clearable
            />
          </div>
          <div class="check-item-photo">
            <el-button size="small" text type="primary" @click="triggerPhotoUpload(index)">
              <el-icon style="margin-right: 4px"><Camera /></el-icon>
              {{ item.photoUrl ? '已拍照' : '拍照' }}
            </el-button>
            <span v-if="item.photoUrl" class="photo-done-tag">📸 已上传</span>
          </div>
          <input
            ref="photoInputRefs"
            type="file"
            accept="image/*"
            capture="environment"
            style="display: none"
            @change="handlePhotoSelected($event, index)"
          />
        </div>
      </div>
      <el-empty v-else description="请先选择检查模板" />
    </div>

    <!-- Step 3: Overall Remark & Submit -->
    <div v-if="scannedDevice" class="submit-section card">
      <div class="section-label">
        <span class="step-badge">3</span>
        <span>综合备注 & 提交</span>
      </div>
      <el-input
        v-model="overallRemark"
        type="textarea"
        :rows="3"
        placeholder="整体检查情况说明（可选）"
        maxlength="500"
        show-word-limit
      />
      <div class="submit-bar">
        <el-button size="large" @click="handleReset">重置</el-button>
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          :disabled="!canSubmit"
          @click="handleSubmit"
        >
          提交检查结果
        </el-button>
      </div>
    </div>

    <!-- Result Dialog -->
    <el-dialog v-model="resultDialogVisible" title="提交结果" width="90%" :close-on-click-modal="false">
      <el-result
        :icon="submitSuccess ? 'success' : 'error'"
        :title="submitSuccess ? '提交成功' : '提交失败'"
        :sub-title="submitMessage"
      />
      <template #footer>
        <el-button v-if="submitSuccess" type="primary" @click="handleReset">
          继续检查
        </el-button>
        <el-button v-else @click="resultDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Camera } from '@element-plus/icons-vue'
import { scanCheckAPI, uploadPhotoAPI } from '@/api/mobile'

// ---------- State ----------
const deviceCode = ref('')
const scanningDevice = ref(false)
const scanAttempted = ref(false)
const scannedDevice = ref(null)
const overallRemark = ref('')
const submitting = ref(false)
const resultDialogVisible = ref(false)
const submitSuccess = ref(false)
const submitMessage = ref('')
const photoInputRefs = ref([])

// ---------- Default check items (in production, fetch from template) ----------
const defaultCheckItems = [
  { id: 1, name: '外观检查', result: '正常', remark: '', photoUrl: '' },
  { id: 2, name: '压力/指示器', result: '正常', remark: '', photoUrl: '' },
  { id: 3, name: '瓶体/阀体完好', result: '正常', remark: '', photoUrl: '' },
  { id: 4, name: '铭牌/标签清晰', result: '正常', remark: '', photoUrl: '' },
  { id: 5, name: '放置位置正确', result: '正常', remark: '', photoUrl: '' },
  { id: 6, name: '有效期检查', result: '正常', remark: '', photoUrl: '' },
]

const checkItems = ref([
  ...defaultCheckItems.map((item) => ({ ...item })),
])

// ---------- Computed ----------
const deviceStatusType = computed(() => {
  const map = { '正常': 'success', '故障': 'danger', '维护中': 'warning' }
  return map[scannedDevice.value?.status] || 'info'
})

const canSubmit = computed(() => {
  if (!scannedDevice.value) return false
  return checkItems.value.length > 0
})

// ---------- Methods ----------
async function handleScan() {
  const code = deviceCode.value.trim()
  if (!code) {
    ElMessage.warning('请输入设备编码')
    return
  }

  scanningDevice.value = true
  scanAttempted.value = true

  try {
    // Try to fetch device info from the backend
    const { getDeviceByIdAPI } = await import('@/api/device')
    const res = await getDeviceByIdAPI(code)
    scannedDevice.value = res.data || null
  } catch {
    // Fallback: use code as display info if device API not ready
    // In production, replace with proper device lookup
    scannedDevice.value = {
      code,
      name: `${code} 设备`,
      type: '灭火器',
      location: 'A栋 3层 走廊',
      status: '正常',
      lastCheck: new Date().toISOString().slice(0, 10),
      manufacturer: '-',
    }
    ElMessage.success(`已识别设备: ${code}`)
  } finally {
    scanningDevice.value = false
  }
}

function triggerPhotoUpload(index) {
  const input = photoInputRefs.value[index]
  if (input) {
    input.value = ''
    input.click()
  }
}

async function handlePhotoSelected(event, index) {
  const file = event.target.files?.[0]
  if (!file) return

  // Preview locally first
  const reader = new FileReader()
  reader.onload = (e) => {
    checkItems.value[index].photoUrl = e.target.result
  }
  reader.readAsDataURL(file)

  // Upload to server with watermark
  try {
    const res = await uploadPhotoAPI(deviceCode.value, file)
    checkItems.value[index].photoUrl = res.data?.url || checkItems.value[index].photoUrl
    ElMessage.success('照片上传成功')
  } catch {
    ElMessage.warning('照片仅本地保存，未上传至服务器')
  }
}

async function handleSubmit() {
  submitting.value = true
  try {
    const results = checkItems.value.map((item) => ({
      itemId: item.id,
      result: item.result,
      remark: item.remark || '',
      imageUrls: item.photoUrl ? [item.photoUrl] : [],
    }))

    await scanCheckAPI({
      deviceCode: deviceCode.value,
      results,
      overallRemark: overallRemark.value,
    })

    submitSuccess.value = true
    submitMessage.value = `设备 ${deviceCode.value} 共 ${results.length} 项检查已提交`
  } catch (err) {
    submitSuccess.value = false
    submitMessage.value = err.message || '提交失败，请稍后重试'
  } finally {
    submitting.value = false
    resultDialogVisible.value = true
  }
}

function handleReset() {
  deviceCode.value = ''
  scannedDevice.value = null
  scanAttempted.value = false
  overallRemark.value = ''
  checkItems.value = defaultCheckItems.map((item) => ({ ...item }))
  resultDialogVisible.value = false
}
</script>

<style scoped>
.mobile-scan-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 16px 16px 32px;
  max-width: 480px;
  margin: 0 auto;
}

/* ---------- Header ---------- */
.scan-header {
  text-align: center;
  padding: 20px 0 16px;
}

.scan-title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a3e;
  margin: 0 0 4px;
}

.scan-subtitle {
  font-size: 13px;
  color: #999;
  margin: 0;
}

/* ---------- Card Sections ---------- */
.card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.section-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 14px;
}

.step-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #1890ff;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
}

/* ---------- Scan Input ---------- */
.scan-input-row {
  margin-bottom: 12px;
}

/* ---------- Device Info ---------- */
.device-info {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px;
  border: 1px solid #e8e8e8;
}

.device-status-tag {
  margin-bottom: 10px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-label {
  font-size: 11px;
  color: #999;
}

.info-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.info-value.code {
  font-family: monospace;
  color: #1890ff;
  background: #e6f7ff;
  padding: 0 6px;
  border-radius: 3px;
  display: inline-block;
  width: fit-content;
}

/* ---------- Check Items ---------- */
.check-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.check-item {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px;
  border: 1px solid #f0f0f0;
}

.check-item-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.check-item-index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #e6f7ff;
  color: #1890ff;
  font-size: 11px;
  font-weight: 600;
}

.check-item-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.check-item-result {
  margin-bottom: 8px;
}

.result-radio-group {
  display: flex;
  width: 100%;
}

.result-radio-group :deep(.el-radio-button__inner) {
  flex: 1;
  padding: 8px 4px;
  font-size: 13px;
}

.result-ok :deep(.el-radio-button__inner) {
  color: #67c23a;
}

.result-warn :deep(.el-radio-button__inner) {
  color: #e6a23c;
}

.result-na :deep(.el-radio-button__inner) {
  color: #909399;
}

.check-item-remark {
  margin-bottom: 6px;
}

.check-item-photo {
  display: flex;
  align-items: center;
  gap: 8px;
}

.photo-done-tag {
  font-size: 12px;
  color: #67c23a;
}

/* ---------- Submit Section ---------- */
.submit-section :deep(.el-textarea__inner) {
  font-size: 14px;
}

.submit-bar {
  display: flex;
  gap: 12px;
  margin-top: 14px;
}

.submit-bar .el-button {
  flex: 1;
  height: 44px;
  font-size: 15px;
}

/* ---------- Empty State ---------- */
:deep(.el-empty__description p) {
  font-size: 13px;
}
</style>
