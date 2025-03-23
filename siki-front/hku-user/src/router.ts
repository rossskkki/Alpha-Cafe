import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('./views/layout/index.vue'),
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('./views/home/index.vue')
        },
        {
          path: 'cart',
          name: 'cart',
          component: () => import('./views/cart/index.vue')
        },
        {
          path: 'order',
          name: 'order',
          component: () => import('./views/order/index.vue')
        },
        {
          path: 'user',
          name: 'user',
          component: () => import('./views/user/index.vue')
        }
      ]
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('./views/login/index.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('./views/register/index.vue')
    },
    {
      path: '/dish/:id',
      name: 'dish-detail',
      component: () => import('./views/dish/detail.vue')
    },
    {
      path: '/setmeal/:id',
      name: 'setmeal-detail',
      component: () => import('./views/setmeal/detail.vue')
    },
    {
      path: '/order/detail/:id',
      name: 'order-detail',
      component: () => import('./views/order/detail.vue')
    },
    {
      path: '/address',
      name: 'address',
      component: () => import('./views/address/index.vue')
    },
    {
      path: '/address/edit/:id?',
      name: 'address-edit',
      component: () => import('./views/address/edit.vue')
    }
  ]
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 需要登录权限的页面
  const authPages = ['/cart', '/order', '/user', '/address']
  // 当前页面是否需要登录
  const needLogin = authPages.some(path => to.path.startsWith(path))
  // 获取token
  const token = localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo') as string).token : null
  
  if (needLogin && !token) {
    // 需要登录但没有token，跳转到登录页
    next('/login')
  } else {
    next()
  }
})

export default router