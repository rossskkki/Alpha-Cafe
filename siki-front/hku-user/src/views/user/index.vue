<template>
  <div class="user-center-container">
    <el-card class="user-info-card">
      <div class="user-header">
        <div class="avatar-container" @click="showAvatarDialog = true">
          <el-avatar :size="80" :src="userInfo?.icon" icon="UserFilled" />
        </div>
        <div class="user-basic-info">
          <h2>{{ userInfo?.nickName || '未登录' }}</h2>
          <p>{{ userInfo?.phone || '' }}</p>
        </div>
      </div>
      
      <el-divider />
      
      <div class="menu-list">
        <div class="menu-item" @click="router.push('/order')">
          <el-icon><Tickets /></el-icon>
          <span>我的订单</span>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
        
        <div class="menu-item" @click="router.push('/address')">
          <el-icon><Location /></el-icon>
          <span>收货地址</span>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
        
        <div class="menu-item" @click="showEditDialog = true">
          <el-icon><Edit /></el-icon>
          <span>修改个人信息</span>
          <el-icon class="arrow-icon"><ArrowRight /></el-icon>
        </div>
        
        <div class="menu-item logout" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </div>
      </div>
    </el-card>
    
    <!-- 修改个人信息对话框 -->
    <el-dialog
      v-model="showEditDialog"
      title="修改个人信息"
      width="80%"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="updateUserInfo" :loading="updating">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  
  <!-- 头像预览对话框 -->
    <el-dialog
    v-model="showAvatarDialog"
    title="头像"
    width="80%"
    :close-on-click-modal="true"
  >
    <div class="avatar-preview-container">
      <img :src="userInfo?.icon" alt="用户头像" class="avatar-preview" v-if="userInfo?.icon" />
      <el-empty description="暂无头像" v-else></el-empty>
    </div>
    <div class="avatar-update-container">
      <el-upload
        class="avatar-uploader"
        action="#"
        :auto-upload="false"
        :show-file-list="false"
        :on-change="handleAvatarChange"
        accept="image/jpeg,image/png,image/jpg,image/gif"
      >
        <el-button type="primary">选择图片</el-button>
        <template #tip>
          <div class="el-upload__tip">只能上传JPG/PNG/GIF文件，且不超过10MB</div>
        </template>
      </el-upload>
      <div v-if="avatarForm.file" class="selected-file-info">
        已选择: {{ avatarForm.file.name }}
      </div>
      <el-button type="success" @click="updateAvatar" :loading="updatingAvatar" :disabled="!avatarForm.file">上传头像</el-button>
    </div>
  </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getUserInfoAPI, updateUserInfoAPI, updateAvatarAPI, logoutAPI } from '@/api/user'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const userInfo = ref(userStore.userInfo)

// 表单相关
const formRef = ref<FormInstance>()
const showEditDialog = ref(false)
const updating = ref(false)
const editForm = reactive({
  name: '',
  phone: ''
})

// 头像相关
const showAvatarDialog = ref(false)
const updatingAvatar = ref(false)
const avatarForm = reactive({
  avatarUrl: '',
  file: null as File | null
})

// 处理头像文件选择
const handleAvatarChange = (file: any) => {
  // 文件大小限制：2MB
  const isLt2M = file.size / 1024 / 1024 < 10
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!')
    return
  }
  
  // 设置选中的文件
  avatarForm.file = file.raw
  // 创建临时URL用于预览
  if (avatarForm.file) {
    avatarForm.avatarUrl = URL.createObjectURL(avatarForm.file)
  }
}

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 获取用户信息
// const getUserInfo = async () => {
//   try {
//     const res = await getUserInfoAPI()
//     userInfo.value = res.data
//     userStore.setUserInfo(res.data)
//   } catch (error) {
//     console.error('获取用户信息失败', error)
//   }
// }

// 打开编辑对话框
const openEditDialog = () => {
  editForm.name = userInfo.value?.name || ''
  editForm.phone = userInfo.value?.phone || ''
  showEditDialog.value = true
}

// 更新用户信息
const updateUserInfo = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      updating.value = true
      try {
        await updateUserInfoAPI(editForm)
        ElMessage.success('个人信息修改成功')
        
        showEditDialog.value = false
        // getUserInfo() // 刷新用户信息
      } catch (error) {
        ElMessage.error('修改个人信息失败')
        console.error(error)
      } finally {
        updating.value = false
      }
    }
  })
}

// 更新用户头像
const updateAvatar = async () => {
  if (!avatarForm.file) {
    ElMessage.warning('请选择头像图片')
    return
  }
  
  updatingAvatar.value = true
  try {
    // 创建FormData对象用于文件上传
    const formData = new FormData()
    formData.append('file', avatarForm.file)
    
    // 调用API上传头像
    const res = await updateAvatarAPI(formData)
    userStore.updateIcon(res.data.data)
    ElMessage.success('头像更新成功')
    showAvatarDialog.value = false
    // getUserInfo() // 刷新用户信息
    
    // 清空选择的文件
    avatarForm.file = null
  } catch (error) {
    ElMessage.error('头像更新失败')
    console.error(error)
  } finally {
    updatingAvatar.value = false
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // await logoutAPI()
    userStore.clearUserInfo()
    localStorage.removeItem('userInfo')
    ElMessage.success('退出登录成功')
    router.push('/login')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出登录失败', error)
    }
  }
}

// onMounted(() => {
//   // 如果已登录，获取最新的用户信息
//   if (userInfo.value) {
//     getUserInfo()
//   }
// })
</script>

<style scoped>
.user-center-container {
  padding: 20px;
}

.user-info-card {
  margin-bottom: 20px;
}

.user-header {
  display: flex;
  align-items: center;
  padding: 10px 0;
}

.avatar-container {
  margin-right: 20px;
}

.user-basic-info h2 {
  margin: 0 0 5px 0;
  font-size: 20px;
}

.user-basic-info p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.menu-list {
  margin-top: 10px;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
  position: relative;
  cursor: pointer;
}

.menu-item:last-child {
  border-bottom: none;
}

.menu-item .el-icon {
  font-size: 20px;
  margin-right: 10px;
  color: #409EFF;
}

.menu-item span {
  flex: 1;
  font-size: 16px;
}

.arrow-icon {
  color: #999 !important;
  margin-right: 0 !important;
}

.logout {
  margin-top: 20px;
}

.logout .el-icon {
  color: #F56C6C;
}

.logout span {
  color: #F56C6C;
}

.avatar-container {
  cursor: pointer;
}

.avatar-preview-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.avatar-preview {
  max-width: 100%;
  max-height: 300px;
  border-radius: 8px;
}

.avatar-update-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
  margin-top: 20px;
  align-items: center;
}

.avatar-uploader {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.selected-file-info {
  margin-top: 10px;
  color: #67c23a;
  font-size: 14px;
}
</style>