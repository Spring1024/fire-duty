<template>
  <div class="user-management">
    <!-- Toolbar -->
    <div class="user-toolbar">
      <span class="toolbar-title">用户列表 共 {{ total }} 人</span>
      <div class="toolbar-actions">
        <el-button plain>导入</el-button>
        <el-button type="primary" @click="handleAdd">新增用户</el-button>
      </div>
    </div>

    <!-- Table -->
    <el-table :data="userList" stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="username" label="用户名" width="130" />
      <el-table-column label="角色" width="150">
        <template #default="{ row }">
          <el-tag :type="roleTagType(row.roleName)" effect="plain" size="small">{{ row.roleName || '—' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <span class="status-cell">
            <span :class="['status-dot', row.status === 1 ? 'dot-green' : 'dot-gray']" />
            {{ row.status === 1 ? '正常' : '停用' }}
          </span>
        </template>
      </el-table-column>
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
      :title="isEditing ? '编辑用户' : '新增用户'"
      width="480px"
    >
      <el-form ref="formRef" :model="form" label-width="80px" size="default">
        <el-form-item label="姓名" prop="name" required>
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="用户名" prop="username" required>
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" :required="!isEditing">
          <el-input v-model="form.password" type="password" :placeholder="isEditing ? '留空则不修改' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId" required>
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属网格" prop="gridId">
          <el-input v-model="form.gridId" placeholder="请输入网格ID" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="正常" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
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
import { useUserStore } from '@/stores/user'
import { getRolesAPI, getUserRolesAPI } from '@/api/user'

const userStore = useUserStore()

const userList = computed(() => userStore.userList)
const loading = computed(() => userStore.loading)
const total = computed(() => userStore.total)

// ---------- Role Options ----------
const roleOptions = ref([])

async function fetchRoles() {
  try {
    const res = await getRolesAPI()
    roleOptions.value = res.data || []
  } catch (err) {
    console.error('获取角色列表失败:', err)
  }
}

function roleTagType(roleName) {
  const map = {
    '超级管理员': 'danger',
    '大网格负责人': '',
    '中网格组长': 'success',
    '小网格检查员': 'warning',
    '维保单位': 'info',
  }
  return map[roleName] || ''
}

// ---------- Dialog ----------
const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const submitting = ref(false)

const defaultForm = {
  name: '',
  username: '',
  password: '',
  roleId: null,
  gridId: null,
  phone: '',
  status: 1,
}

const form = ref({ ...defaultForm })

function resetForm() {
  form.value = { ...defaultForm }
}

function handleAdd() {
  isEditing.value = false
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

async function handleEdit(row) {
  isEditing.value = true
  editingId.value = row.id
  // 获取用户当前角色ID
  let roleId = null
  try {
    const res = await getUserRolesAPI(row.id)
    const roleNames = res.data || []
    // 根据角色名称找到对应的 roleId
    const matchedRole = roleOptions.value.find(r => roleNames.includes(r.name))
    roleId = matchedRole ? matchedRole.id : null
  } catch (err) {
    console.error('获取用户角色失败:', err)
  }

  form.value = {
    name: row.name,
    username: row.username,
    password: '',
    roleId: roleId,
    gridId: row.gridId,
    phone: row.phone,
    status: row.status,
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!form.value.name || !form.value.username || !form.value.roleId) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (!isEditing.value && !form.value.password) {
    ElMessage.warning('请输入密码')
    return
  }
  submitting.value = true
  try {
    if (isEditing.value) {
      await userStore.updateUser(editingId.value, form.value)
      ElMessage.success('用户更新成功')
    } else {
      await userStore.createUser(form.value)
      ElMessage.success('用户创建成功')
    }
    dialogVisible.value = false
    userStore.fetchUsers()
  } catch (err) {
    // 错误已在拦截器中统一处理
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除用户"${row.name}"？`, '删除确认', {
      confirmButtonText: '确认删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await userStore.deleteUser(row.id)
    ElMessage.success('删除成功')
    userStore.fetchUsers()
  } catch (err) {
    // cancel or error
  }
}

// ---------- Lifecycle ----------
onMounted(() => {
  userStore.fetchUsers()
  fetchRoles()
})
</script>

<style scoped>
.user-management {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.user-toolbar {
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
.toolbar-actions {
  display: flex;
  gap: 8px;
}

.status-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}
.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.dot-green {
  background: #67c23a;
}
.dot-gray {
  background: #c0c4cc;
}
</style>
