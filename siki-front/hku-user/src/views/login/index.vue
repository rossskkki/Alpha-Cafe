<template>
  <div class="login-container">
    <!-- 将 :src 绑定到导入的变量 loginBgImage -->
    <el-image :src="loginBgImage" class="login-logo"></el-image>
    <el-card class="login-card">
      <div class="login-header">
        <h2>用户登录</h2>
      </div>
      
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入手机号"
            prefix-icon="Iphone"
            size="large"
            class="mobile-input"
          />
        </el-form-item>
        
        <el-form-item prop="code">
          <div class="code-input-container">
            <el-input 
              v-model="loginForm.code" 
              placeholder="请输入验证码"
              prefix-icon="Message"
              size="large"
              class="mobile-input"
            />
            <el-button 
              type="primary" 
              class="code-button" 
              @click="sendCode" 
              :disabled="isCounting"
              color="orange"
              size="large"
            >
              {{ codeButtonText }}
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" class="login-button" @click="handleLogin" :loading="loading" color="orange" size="large">
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
// 使用 import 导入图片资源
import loginBgImage from '@/assets/pics/regback.jpg'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { loginAPI } from '@/api/user'
import { sendSmsCodeAPI, loginBySmsAPI } from '@/api/sms'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)

// 验证码按钮状态
const isCounting = ref(false)
const countdown = ref(60)
const codeButtonText = ref('获取验证码')

// 登录表单数据
const loginForm = reactive({
  username: '',
  code: ''
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

// 发送验证码
const sendCode = async () => {
  // 验证手机号
  if (!loginForm.username) {
    ElMessage.warning('请先输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(loginForm.username)) {
    ElMessage.warning('请输入正确的手机号码')
    return
  }
  
  try {
    // 调用发送验证码API
    const res = await sendSmsCodeAPI({ phone: loginForm.username })
    if (res.data.code !== 0) {
      ElMessage.warning(res.msg || '验证码发送失败，请稍后再试')
      return 
    }
    ElMessage.success('验证码发送成功，请注意查收')
    
    // 开始倒计时
    isCounting.value = true
    countdown.value = 60
    codeButtonText.value = `${countdown.value}秒后重新获取`
    
    const timer = setInterval(() => {
      countdown.value--
      codeButtonText.value = `${countdown.value}秒后重新获取`
      
      if (countdown.value <= 0) {
        clearInterval(timer)
        isCounting.value = false
        codeButtonText.value = '获取验证码'
      }
    }, 1000)
  } catch (error: any) {
    ElMessage.error(error.response?.data?.msg || '验证码发送失败，请稍后再试')
  }
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 调用验证码登录API
        const res = await loginBySmsAPI({
          phone: loginForm.username,
          code: loginForm.code
        })
        
        // 检查响应状态码，确保登录成功
        if (res.data.code === 0 && res.status === 200) {
          // 保存用户信息到store和localStorage
          userStore.setUserInfo(res.data.data)
          console.log(res.data.data)
          localStorage.setItem('userInfo', JSON.stringify(res.data.data))
          
          // 只有在确认成功后才显示成功提示
          ElMessage.success('登录成功')
          
          // 跳转到首页或者来源页面
          const redirect = router.currentRoute.value.query.redirect as string
          router.replace(redirect || '/')
        } else {
          // API返回了错误状态码
          ElMessage.error(res.msg || '登录失败，请检查验证码是否正确')
        }
      } catch (error: any) {
        ElMessage.error(error.response?.data?.msg || '登录失败，请检查验证码是否正确')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100vh;
  background-color: #ca9f74;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background-image: url('@/assets/pics/regback.jpg');
  border-radius: 8px;
  /* box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1); */
  padding: 30px;
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h2 {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.login-button {
  width: 100%;
  margin-top: 10px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

.register-link {
  font: bolder;
  color: orange;
  text-decoration: none;
  margin-left: 5px;
}

.code-input-container {
  display: flex;
  align-items: center;
}

.code-input-container .el-input {
  flex: 1;
}

.code-button {
  margin-left: 10px;
  width: 120px;
  white-space: nowrap;
}

.login-logo {
  /* 确保图片有尺寸，否则可能不可见 */
  width: 100%; /* 或您需要的大小 */
  height: auto;
}
</style>