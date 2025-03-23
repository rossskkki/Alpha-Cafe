<template>
  <div class="address-container">
    <div class="page-header">
      <el-page-header @back="router.back()" title="" content="收货地址" />
    </div>
    
    <div class="address-list" v-if="addressList.length > 0">
      <el-card v-for="item in addressList" :key="item.id" class="address-item">
        <div class="address-info">
          <div class="address-header">
            <span class="name">{{ item.consignee }}</span>
            <span class="phone">{{ item.phone }}</span>
            <el-tag v-if="item.isDefault === 1" type="danger" size="small">默认</el-tag>
          </div>
          <div class="address-detail">
            {{ item.detail }}
          </div>
        </div>
        <div class="address-actions">
          <el-button type="primary" link @click="router.push(`/address/edit/${item.id}`)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(item.id)">删除</el-button>
          <el-button v-if="item.isDefault !== 1" type="success" link @click="handleSetDefault(item.id)">设为默认</el-button>
        </div>
      </el-card>
    </div>
    
    <el-empty v-else description="暂无收货地址" />
    
    <div class="add-address">
      <el-button type="primary" @click="router.push('/address/edit')" round>
        <el-icon><Plus /></el-icon>
        新增收货地址
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAddressListAPI, deleteAddressAPI, setDefaultAddressAPI } from '@/api/address'

const router = useRouter()
const addressList = ref<any[]>([])

// 获取地址列表
const getAddressList = async () => {
  try {
    const res = await getAddressListAPI()
    addressList.value = res.data
  } catch (error) {
    console.error('获取地址列表失败', error)
    ElMessage.error('获取地址列表失败')
  }
}

// 删除地址
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteAddressAPI(id)
    ElMessage.success('删除成功')
    getAddressList() // 刷新列表
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

// 设置默认地址
const handleSetDefault = async (id: number) => {
  try {
    await setDefaultAddressAPI(id)
    ElMessage.success('设置默认地址成功')
    getAddressList() // 刷新列表
  } catch (error) {
    ElMessage.error('设置默认地址失败')
    console.error(error)
  }
}