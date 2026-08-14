import { createRouter, createWebHistory } from 'vue-router'
import Login from './views/Login.vue'
import Register from './views/Register.vue'
import Dashboard from './views/Dashboard.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/login', component: Login, meta: { requiresGuest: true } },
        { path: '/register', component: Register, meta: { requiresGuest: true } },
        { path: '/', component: Dashboard, meta: { requiresAuth: true } }
    ]
})

// Navigation Guard
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('jwt_token')

    if (to.meta.requiresAuth && !token) {
        next('/login') // Redirect to login if unauthenticated
    } else if (to.meta.requiresGuest && token) {
        next('/') // Prevent going to login page if already logged in
    } else {
        next()
    }
})

export default router