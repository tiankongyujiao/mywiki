import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/users/Login.vue'),
      meta: { title: '登录' },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/users/Register.vue'),
      meta: { title: '注册' },
    },
    {
      path: '/',
      component: MainLayout,
      redirect: '/articles',
      children: [
        {
          path: 'articles',
          name: 'ArticleList',
          component: () => import('../views/article/ArticleList.vue'),
          meta: { title: '文章列表' },
        },
        {
          path: 'articles/create',
          name: 'ArticleCreate',
          component: () => import('../views/article/ArticleForm.vue'),
          meta: { title: '新增文章', requiresAuth: true },
        },
        {
          path: 'articles/:id',
          name: 'ArticleDetail',
          component: () => import('../views/article/ArticleDetail.vue'),
          meta: { title: '文章详情' },
        },
        {
          path: 'articles/:id/edit',
          name: 'ArticleEdit',
          component: () => import('../views/article/ArticleForm.vue'),
          meta: { title: '编辑文章', requiresAuth: true },
        },
        {
          path: 'categories',
          name: 'CategoryList',
          component: () => import('../views/category/CategoryList.vue'),
          meta: { title: '分类管理', requiresAuth: true },
        },
      ],
    },
  ],
})

export default router

// 无需登录即可访问的页面
const whiteList = ['/login', '/register']

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  // 1. 有 token，但刷新页面后 user 信息还没有恢复
  if (authStore.isLogin && !authStore.user) {
    try {
      await authStore.fetchUser()
    } catch (error) {
      // token 无效 / 过期
      authStore.logout()
    }
  }

  // 2. 当前页面明确不需要登录
  if (whiteList.includes(to.path)) {
    return true
  }

  // 3. 当前页面明确需要登录，但没有 token
  if (to.meta.requiresAuth && !authStore.isLogin) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    }
  }

  // 4. 当前页面不要求登录，直接放行
  return true
})
