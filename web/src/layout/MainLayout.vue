<template>
  <el-container class="main-layout" style="height: 100vh;">
    <!-- Sidebar -->
    <el-aside :width="sidebarCollapsed ? '64px' : '220px'" class="app-sidebar">
      <div class="sidebar-header">
        <span v-if="!sidebarCollapsed" class="sidebar-logo">🔥 消防履职系统</span>
        <span v-else class="sidebar-logo-mini">🔥</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="sidebarCollapsed"
        :router="true"
        background-color="#001529"
        text-color="#ffffffb3"
        active-text-color="#fff"
        style="border-right: none;"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>
        <el-menu-item index="/devices">
          <el-icon><Monitor /></el-icon>
          <template #title>设备管理</template>
        </el-menu-item>
        <el-menu-item index="/tasks">
          <el-icon><List /></el-icon>
          <template #title>巡检任务</template>
        </el-menu-item>
        <el-menu-item index="/rectifications">
          <el-icon><WarningFilled /></el-icon>
          <template #title>隐患整改</template>
        </el-menu-item>
        <el-menu-item index="/statistics">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>统计报表</template>
        </el-menu-item>
        <el-sub-menu index="system">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/users">
            <el-icon><UserFilled /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/system/grids">
            <el-icon><Grid /></el-icon>
            <template #title>网格管理</template>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- Right area -->
    <el-container>
      <!-- Header -->
      <el-header class="app-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleSidebar" style="cursor:pointer;font-size:20px;">
            <Fold v-if="!sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="breadcrumbTitle">{{ breadcrumbTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-tooltip content="全屏" placement="bottom">
            <el-icon class="header-icon"><FullScreen /></el-icon>
          </el-tooltip>
          <el-tooltip content="消息通知" placement="bottom">
            <el-badge :value="3" class="header-badge">
              <el-icon class="header-icon"><Bell /></el-icon>
            </el-badge>
          </el-tooltip>
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="28" style="background:#1890ff;vertical-align:middle;">{{ user.name?.[0] || '管' }}</el-avatar>
              <span class="user-name">{{ user.name }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- Main content -->
      <el-main class="app-main">
        <router-view />
      </el-main>

      <!-- Footer -->
      <el-footer class="app-footer" height="36px">
        🔥 消防履职系统 v1.0 &copy; 2026
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const { toggleSidebar } = appStore
const user = computed(() => appStore.user)

const activeMenu = computed(() => route.path)

const breadcrumbTitle = computed(() => route.meta?.title || '')

function handleCommand(command) {
  if (command === 'logout') {
    appStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.app-sidebar {
  background-color: #001529;
  overflow-y: auto;
  overflow-x: hidden;
  transition: width 0.25s;
}
.sidebar-header {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  border-bottom: 1px solid rgba(255,255,255,0.08);
}
.sidebar-logo-mini {
  font-size: 22px;
}
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e8e8e8;
  padding: 0 20px;
  height: 56px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.breadcrumb {
  font-size: 13px;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.header-icon {
  font-size: 18px;
  color: #666;
  cursor: pointer;
}
.header-icon:hover {
  color: #1890ff;
}
.header-badge {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}
.user-name {
  font-size: 13px;
  color: #333;
  margin-left: 4px;
}
.app-main {
  background: #f0f2f5;
  padding: 16px;
  overflow-y: auto;
}
.app-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #999;
  background: #fff;
  border-top: 1px solid #e8e8e8;
}
</style>
