import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        children: [
            {
                path: '',
                name: 'HomeIndex',
                component: () => import('../views/Welcome.vue')
            },
            {
                path: 'files',
                name: 'Files',
                component: () => import('../views/FileManager.vue')
            },
            {
                path: 'trash',
                name: 'Trash',
                component: () => import('../views/Trash.vue')
            }
        ]
    },
    {
        path: '/editor/:id?',
        name: 'Editor',
        component: () => import('../views/Editor.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router