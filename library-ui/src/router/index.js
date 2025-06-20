import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// 布局组件
import Layout from '@/layouts/Layout.vue'

// 页面组件
import Login from '@/views/Login.vue'
import Dashboard from '@/views/Dashboard.vue'
import Test from '@/views/Test.vue'

// 管理员页面
import UserManagement from '@/views/admin/UserManagement.vue'
import BookManagement from '@/views/admin/BookManagement.vue'
import CategoryManagement from '@/views/admin/CategoryManagement.vue'
import BorrowManagement from '@/views/admin/BorrowManagement.vue'

// 读者页面
import BookList from '@/views/reader/BookList.vue'
import MyBorrows from '@/views/reader/MyBorrows.vue'
import Profile from '@/views/reader/Profile.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/test',
    name: 'Test',
    component: Test,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '仪表盘' }
      }
    ]
  },
  {
    path: '/admin',
    component: Layout,
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'users',
        name: 'UserManagement',
        component: UserManagement,
        meta: { title: '用户管理' }
      },
      {
        path: 'books',
        name: 'BookManagement',
        component: BookManagement,
        meta: { title: '图书管理' }
      },
      {
        path: 'categories',
        name: 'CategoryManagement',
        component: CategoryManagement,
        meta: { title: '分类管理' }
      },
      {
        path: 'borrows',
        name: 'BorrowManagement',
        component: BorrowManagement,
        meta: { title: '借阅管理' }
      }
    ]
  },
  {
    path: '/reader',
    component: Layout,
    meta: { requiresAuth: true, requiresReader: true },
    children: [
      {
        path: 'books',
        name: 'BookList',
        component: BookList,
        meta: { title: '图书浏览' }
      },
      {
        path: 'borrows',
        name: 'MyBorrows',
        component: MyBorrows,
        meta: { title: '我的借阅' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: Profile,
        meta: { title: '个人信息' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  console.log('路由导航:', {
    to: to.path,
    from: from.path,
    isAuthenticated: authStore.isAuthenticated,
    user: authStore.user,
    token: !!authStore.token
  })
  
  // 已登录用户访问登录页，重定向到仪表盘
  if (to.path === '/login' && authStore.isAuthenticated) {
    console.log('已登录用户访问登录页，重定向到仪表盘')
    next('/dashboard')
    return
  }
  
  // 检查是否需要认证
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    console.log('需要认证但未登录，重定向到登录页')
    next('/login')
    return
  }
  
  // 检查管理员权限
  if (to.meta.requiresAdmin && authStore.user?.role !== 'ADMIN') {
    console.log('需要管理员权限但权限不足，重定向到仪表盘')
    next('/dashboard')
    return
  }
  
  // 检查读者权限
  if (to.meta.requiresReader && authStore.user?.role !== 'READER') {
    console.log('需要读者权限但权限不足，重定向到仪表盘')
    next('/dashboard')
    return
  }
  
  console.log('路由守卫通过，继续导航')
  next()
})

export default router 