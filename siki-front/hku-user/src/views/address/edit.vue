<template>
  <div class="address-edit-container">
    <div class="page-header">
      <el-page-header @back="router.back()" title="" :content="isEdit ? '编辑地址' : '新增地址'" />
    </div>
    
    <el-card class="address-form-card">
      <el-form :model="addressForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="收货人" prop="consignee">
          <el-input v-model="addressForm.consignee" placeholder="请输入收货人姓名" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        
        <el-form-item label="详细地址" prop="detail">
          <el-input 
            v-model="addressForm.detail" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入详细地址信息"
          />
        </el-form-item>
        
        <el-form-item label="标签" prop="label">
          <el-radio-group v-model="addressForm.label">
            <el-radio label="家">家</el-radio>
            <el-radio label="公司">公司</el-radio>
            <el-radio label="学校">学校</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault" :true-label="1" :false-label="0">
            设为默认地址
          </el-checkbox>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveAddress" :loading="loading">
            保存
          </el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addAddressAPI, updateAddressAPI, getAddressListAPI } from '@/api/address'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const loading = ref(false)

// 判断是编辑还是新增
const isEdit = ref(!!route.params.id)
const addressId = route.params.id as string

// 地址表单数据
const addressForm = reactive({
  id: 0,
  consignee: '',
  phone: '',
  detail: '',
  label: '家',
  isDefault: 0
})

// 表单验证规则
const rules = {
  consignee: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' },
    { min: 5, message: '地址长度不能少于5个字符', trigger: 'blur' }
  ]
}

// 获取地址详情
const getAddressDetail = async () => {
  if (!isEdit.value) return
  
  try {
    const res = await getAddressListAPI()
    const addressList = res.data
    const currentAddress = addressList.find((item: any) => item.id === Number(addressId))
    
    if (currentAddress) {
      // 填充表单数据
      Object.assign(addressForm, currentAddress)
    } else {
      ElMessage.error('地址信息不存在')
      router.replace('/address')
    }
  } catch (error) {
    console.error('获取地址详情失败', error)
    ElMessage.error('获取地址详情失败')
    router.replace('/address')
  }
}

// 保存地址
const saveAddress = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        if (isEdit.value) {
          // 编辑地址
          await updateAddressAPI(addressForm)
          ElMessage.success('地址修改成功')
        } else {
          // 新增地址
          await addAddressAPI(addressForm)
          ElMessage.success('地址添加成功')
        }
        router.replace('/address')
      } catch (error: any) {
        ElMessage.error(error.response?.data?.msg || '操作失败，请稍后再试')
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  getAddressDetail()
})
</script>

<style scoped>
.address-edit-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.address-form-card {
  margin-bottom: 20px;
}
</style>