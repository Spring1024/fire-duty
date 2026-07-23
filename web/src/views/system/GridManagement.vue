<template>
  <div class="grid-management">
    <!-- Toolbar -->
    <div class="grid-toolbar">
      <span class="toolbar-title">网格列表</span>
      <el-button type="primary" @click="handleAdd">新增网格</el-button>
    </div>

    <!-- Table -->
    <el-table :data="gridList" stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="name" label="网格名称" min-width="160" />
      <el-table-column label="级别" width="110">
        <template #default="{ row }">
          <el-tag :type="row.levelType" size="small">{{ row.level }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="leader" label="负责人" width="100" />
      <el-table-column prop="deviceCount" label="设备数量" width="100" />
      <el-table-column prop="contact" label="联系人" width="100" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="scope" label="管辖范围" min-width="180" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Add / Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? '编辑网格' : '新增网格'"
      width="480px"
    >
      <el-form ref="formRef" :model="form" label-width="80px" size="default">
        <el-form-item label="网格名称" prop="name" required>
          <el-input v-model="form.name" placeholder="请输入网格名称" />
        </el-form-item>
        <el-form-item label="级别" prop="level" required>
          <el-select v-model="form.level" placeholder="请选择级别" style="width: 100%">
            <el-option label="大网格" value="大网格" />
            <el-option label="中网格" value="中网格" />
            <el-option label="小网格" value="小网格" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人" prop="leader">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="form.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="管辖范围" prop="scope">
          <el-input v-model="form.scope" type="textarea" placeholder="请输入管辖范围" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useGridStore } from '@/stores/grid'

const gridStore = useGridStore()

const gridList = computed(() => gridStore.gridList)
const loading = computed(() => gridStore.loading)

// ---------- Dialog ----------
const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const submitting = ref(false)

const defaultForm = {
  name: '',
  level: '',
  levelType: '',
  leader: '',
  deviceCount: 0,
  contact: '',
  phone: '',
  scope: '',
}

const form = ref({ ...defaultForm })

const levelTypeMap = {
  '大网格': 'primary',
  '中网格': 'success',
  '小网格': 'warning',
}

function resetForm() {
  form.value = { ...defaultForm }
}

function handleAdd() {
  isEditing.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEditing.value = true
  editingId.value = row.id
  form.value = {
    name: row.name,
    level: row.level,
    levelType: row.levelType,
    leader: row.leader,
    deviceCount: row.deviceCount,
    contact: row.contact,
    phone: row.phone,
    scope: row.scope,
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.name || !form.value.level) {
    ElMessage.warning('请填写完整信息')
    return
  }
  submitting.value = true
  try {
    const payload = {
      ...form.value,
      levelType: levelTypeMap[form.value.level] || '',
    }
    if (isEditing.value) {
      await gridStore.updateGrid(editingId.value, payload)
      ElMessage.success('网格更新成功')
    } else {
      await gridStore.createGrid(payload)
      ElMessage.success('网格创建成功')
    }
    dialogVisible.value = false
    gridStore.fetchGrids()
  } catch (err) {
    // 错误已在拦截器中统一处理
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除网格"${row.name}"？`, '删除确认', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await gridStore.deleteGrid(row.id)
    ElMessage.success('删除成功')
    gridStore.fetchGrids()
  } catch (err) {
    // cancel or error
  }
}

// ---------- Lifecycle ----------
onMounted(() => {
  gridStore.fetchGrids()
})
</script>

<style scoped>
.grid-management {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.grid-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}
.toolbar-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}
</style>
