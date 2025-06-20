import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const { success, message, data } = response.data
    if (success) {
      return { data, message }
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message || '请求失败'))
    }
  },
  (error) => {
    let errorMessage = '网络错误'
    
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        errorMessage = '登录已过期，请重新登录'
        // 清除本地存储的认证信息
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        // 重定向到登录页
        window.location.href = '/login'
      } else if (status === 403) {
        errorMessage = '权限不足'
      } else if (data?.message) {
        errorMessage = data.message
      }
    }
    
    ElMessage.error(errorMessage)
    return Promise.reject(error)
  }
)

// 认证相关API
export const authApi = {
  login: (credentials) => request.post('/auth/login', credentials),
  register: (userData) => request.post('/auth/register', userData),
  registerAdmin: (userData) => request.post('/auth/register-admin', userData)
}

// 用户管理API
export const userApi = {
  getUsers: (params) => request.get('/admin/users', { params }),
  getUserById: (id) => request.get(`/admin/users/${id}`),
  updateUser: (id, userData) => request.put(`/admin/users/${id}`, userData),
  deleteUser: (id) => request.delete(`/admin/users/${id}`),
  toggleUserStatus: (id) => request.put(`/admin/users/${id}/toggle-status`)
}

// 图书管理API
export const bookApi = {
  // 公共接口
  searchBooks: (params) => request.get('/books/search', { params }),
  getBookById: (id) => request.get(`/books/${id}`),
  getCategories: () => request.get('/books/categories'),
  
  // 管理员接口
  getBooks: (params) => request.get('/admin/books', { params }),
  createBook: (bookData) => request.post('/admin/books', bookData),
  updateBook: (id, bookData) => request.put(`/admin/books/${id}`, bookData),
  deleteBook: (id) => request.delete(`/admin/books/${id}`),
  
  // 读者接口
  getAvailableBooks: (params) => request.get('/reader/books', { params })
}

// 分类管理API
export const categoryApi = {
  getCategories: (params) => request.get('/admin/categories', { params }),
  getCategoryById: (id) => request.get(`/admin/categories/${id}`),
  createCategory: (categoryData) => request.post('/admin/categories', categoryData),
  updateCategory: (id, categoryData) => request.put(`/admin/categories/${id}`, categoryData),
  deleteCategory: (id) => request.delete(`/admin/categories/${id}`)
}

// 借阅管理API
export const borrowApi = {
  // 管理员接口
  getBorrowRecords: (params) => request.get('/admin/borrow-records', { params }),
  getPendingRequests: (params) => request.get('/admin/borrow-records/pending', { params }),
  approveBorrow: (id, remarks) => request.put(`/admin/borrow-records/${id}/approve`, { remarks }),
  rejectBorrow: (id, remarks) => request.put(`/admin/borrow-records/${id}/reject`, { remarks }),
  returnBook: (id, remarks) => request.put(`/admin/borrow-records/${id}/return`, { remarks }),
  getOverdueRecords: () => request.get('/admin/borrow-records/overdue'),
  getStatistics: () => request.get('/admin/statistics'),
  
  // 读者接口
  createBorrowRequest: (bookId, remarks) => request.post('/reader/borrow-requests', null, {
    params: { bookId, remarks }
  }),
  getMyBorrowRecords: (params) => request.get('/reader/borrow-records', { params }),
  getBorrowRecordById: (id) => request.get(`/reader/borrow-records/${id}`),
  getActiveBorrows: (params) => request.get('/reader/borrow-records/active', { params }),
  getBorrowHistory: (params) => request.get('/reader/borrow-records/history', { params })
}

// 个人信息API
export const profileApi = {
  getProfile: () => request.get('/reader/profile'),
  updateProfile: (userData) => request.put('/reader/profile', userData)
}

export default request 