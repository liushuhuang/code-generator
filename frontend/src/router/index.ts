import { createRouter, createWebHashHistory } from 'vue-router'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/Home.vue')
    },
    {
      path: '/sql-builder',
      name: 'sql-builder',
      component: () => import('@/views/SqlBuilder.vue')
    }
  ]
})

export default router