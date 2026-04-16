import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/Home.vue')
    },
    {
        path: '/files',
        name: 'Files',
        component: () => import('../views/Files.vue')
    },
    {
        path: '/editor/:id?',
        name: 'Editor',
        component: () => import('../views/Editor.vue')
    },
    {
        path: '/trash',
        name: 'Trash',
        component: () => import('../views/Trash.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router