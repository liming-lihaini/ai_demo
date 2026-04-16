import axios from 'axios'

const request = axios.create({
    baseURL: '/api',
    timeout: 30000
})

// Request interceptor
request.interceptors.request.use(
    config => config,
    error => Promise.reject(error)
)

// Response interceptor
request.interceptors.response.use(
    response => response.data,
    error => {
        const message = error.response?.data?.message || error.message
        return Promise.reject(new Error(message))
    }
)

export default request