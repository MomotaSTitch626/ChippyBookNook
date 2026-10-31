import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '../views/login.vue'

const router = createRouter({
    history: createWebHashHistory(),
    routes: [
        { path:'/', component: () => import('../views/Home.vue') },
        { path:'/my', component: () => import('../views/MyBooks.vue') },
        { path:'/note/:bookId', component: () => import('../views/BookNotes.vue')} ,
        { path:'/upload', component: () => import('../views/Upload.vue') },
        { path:'/read/:bookId', component: () => import('../views/Reader.vue') },
        { path:'/library', component: () => import('../views/Library.vue') },
        { path:'/book/:id', component: () => import('../views/BookDetail.vue') },
        { path:'/profile', component: () => import('../views/Profile.vue') },
        { path:'/login', component: Login },
        { path:'/register', component: () => import('../views/Register.vue') }
    ]
})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    // 登录页和注册页都不需要 token，放行；其他页面没有 token 就踢去登录页
    if ((to.path !== '/login' && to.path !== '/register') && !token) {
        next('/login')
    } else {
        next()
    }
})

export default router