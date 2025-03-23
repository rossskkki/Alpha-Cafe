import axios from 'axios'
import router from '@/router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

// 定义一个变量,记录公共的前缀,  baseURL: http://localhost:8080/api
const baseURL = '/api'
const instance = axios.create({ baseURL })

// 1.定义"请求"拦截器(前端给后端服务器的请求)
// api里每次调用request方法，都会触发一次请求拦截器
instance.interceptors.request.use(
  (config) => {
    // config配置对象（要请求后台的参数都在这个对象上）
    const userStore = useUserStore()
    const token = userStore.userInfo ? userStore.userInfo.token : null
    // 在发起时要统一携带请求头Authorization和token值
    if (token) {
      // 为请求头挂载 Authorization 字段
      config.headers.Authorization = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 2.定义"响应"拦截器(后端服务器给前端的响应)
instance.interceptors.response.use(
  // 响应http状态码为 2xx,3xx 时触发成功的回调，形参中的 response 是"成功的结果"
  (response) => {
    // 如果返回的data里有状态码code并且不是0，说明后端返回了错误信息（token过期等），这时候要给前端提示错误信息
    if ('code' in response.data && response.data.code != 0) {
      // 各种错误，后端有返回提示信息，此处在前端用ElMessage做统一拦截提示
      ElMessage.error(response.data.msg)
    }
    // 对响应的response先在上面拦截处理，最后再放行，返回response
    return response
  },
  // 响应状态码是 4xx,5xx 时触发失败的回调
  (error) => {
    // 对响应错误做点什么
    if (error.response && error.response.status === 401) {
      // 401 未授权（未登录或token过期）
      ElMessage.error('登录状态已过期，请重新登录')
      // 清除本地存储的token
      localStorage.removeItem('userInfo')
      // 跳转到登录页
      router.push('/login')
    } else {
      // 其他错误
      ElMessage.error(error.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export default instance