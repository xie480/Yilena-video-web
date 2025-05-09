import { createRouter, createWebHistory } from 'vue-router'
import IndexView from '@/views/index/index.vue'
import dataShow from '@/views/dataShow/dataShow.vue'
import log from '@/views/log/log.vue'
import manager from '@/views/manager/manager.vue'
import user from '@/views/user/user.vue'
import video from '@/views/video/video.vue'
import layout from '@/views/layout/layout.vue'
import login from '@/views/login/login.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/login', name: 'login', component: login},
    {path: '/', 
      name: 'layout', 
      component: layout,
      redirect: '/index',
      children: [
        {path: '/index', name: 'index', component: IndexView},
        {path: '/dataShow', name: 'dataShow', component: dataShow},
        {path: '/log', name: 'log', component: log},
        {path: '/manager', name: 'manager', component: manager},
        {path: '/user', name: 'user', component: user},
        {path: '/video', name: 'video', component: video}
      ]
    },  
]
})

export default router