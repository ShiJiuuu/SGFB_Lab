import { createRouter, createWebHistory } from 'vue-router'
import BorrowApplication from '../views/BorrowApplication.vue'
import BorrowGantt from '../views/BorrowGantt.vue'
import ClassroomManagement from '../views/ClassroomManagement.vue'
import RentDetails from '../views/RentDetails.vue'
import TimeSlotManagement from '../views/TimeSlotManagement.vue'
import Login from '../views/Login.vue'

const routes = [
  { path: '/login', name: 'Login', component: Login },
  { path: '/', redirect: '/borrow-application' },
  { path: '/borrow-application', name: 'BorrowApplication', component: BorrowApplication },
  { path: '/borrow-gantt', name: 'BorrowGantt', component: BorrowGantt },
  { path: '/classroom-management', name: 'ClassroomManagement', component: ClassroomManagement, meta: { requiresAuth: true } },
  { path: '/rent-details', name: 'RentDetails', component: RentDetails, meta: { requiresAuth: true } },
  { path: '/time-slot-management', name: 'TimeSlotManagement', component: TimeSlotManagement, meta: { requiresAuth: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const isAuthenticated = localStorage.getItem('token')
  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login') // 未登录跳转到登录页
  } else {
    next() // 已登录或无需验证
  }
})

export default router
