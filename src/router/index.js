import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录', noAuth: true },
  },
  {
    path: '/mobile/scan',
    name: 'MobileScan',
    component: () => import('@/views/mobile/MobileScan.vue'),
    meta: { title: '扫码检查', noAuth: true },
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '工作台', icon: 'Odometer' },
      },
      {
        path: 'devices',
        name: 'Devices',
        component: () => import('@/views/devices/DeviceManagement.vue'),
        meta: { title: '设备管理', icon: 'Monitor' },
      },
      {
        path: 'tasks',
        name: 'Tasks',
        component: () => import('@/views/tasks/TaskManagement.vue'),
        meta: { title: '巡检任务', icon: 'List' },
      },
      {
        path: 'rectifications',
        name: 'Rectifications',
        component: () => import('@/views/rectifications/RectificationManagement.vue'),
        meta: { title: '隐患整改', icon: 'WarningFilled' },
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/Statistics.vue'),
        meta: { title: '统计报表', icon: 'DataAnalysis' },
      },
      {
        path: 'system/users',
        name: 'SystemUsers',
        component: () => import('@/views/system/UserManagement.vue'),
        meta: { title: '用户管理', icon: 'UserFilled' },
      },
      {
        path: 'system/grids',
        name: 'SystemGrids',
        component: () => import('@/views/system/GridManagement.vue'),
        meta: { title: '网格管理', icon: 'Grid' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Simple auth guard: redirect to login if not authenticated
router.beforeEach((to, from, next) => {
  if (to.meta.noAuth) {
    next()
    return
  }
  const token = sessionStorage.getItem('token') || localStorage.getItem('token')
  if (!token && to.path !== '/login') {
    next('/login')
  } else {
    next()
  }
})

export default router
