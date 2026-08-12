import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
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
          meta: { title: '新增文章' },
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
          meta: { title: '编辑文章' },
        },
      ],
    },
  ],
})

export default router
