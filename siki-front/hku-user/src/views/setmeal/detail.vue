<template>
  <div class="setmeal-detail-container">
    <div class="page-header">
      <el-page-header @back="handleBack" title="返回" content="套餐详情" />
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="setmeal" class="setmeal-content">
      <div class="setmeal-image">
        <el-image :src="setmeal.image" fit="cover" />
      </div>
      
      <div class="setmeal-info">
        <h2 class="setmeal-name">{{ setmeal.name }}</h2>
        <div class="setmeal-price">
          <span class="price">¥{{ setmeal.price }}</span>
          <span class="sold">月售 {{ setmeal.saleNum || 0 }}</span>
        </div>
        
        <div class="setmeal-description">
          <h3>套餐描述</h3>
          <p>{{ setmeal.description || '暂无描述' }}</p>
        </div>
        
        <div class="setmeal-dishes" v-if="setmeal.setmealDishes && setmeal.setmealDishes.length > 0">
          <h3>套餐内容</h3>
          <el-table :data="setmeal.setmealDishes" style="width: 100%">
            <el-table-column prop="name" label="菜品名称" />
            <el-table-column prop="copies" label="份数" width="80" />
            <el-table-column prop="price" label="单价" width="80">
              <template #default="scope">
                ¥{{ scope.row.price }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <div class="action-bar">
        <div class="quantity-control">
          <el-input-number 
            v-model="quantity" 
            :min="1" 
            :max="99" 
            size="small"
          />
        </div>
        <el-button type="primary" @click="addToCart" :loading="addingToCart">
          加入购物车
        </el-button>
      </div>
    </div>
    
    <el-empty v-else description="套餐信息不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSetmealDetailAPI } from '@/api/menu'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const setmealId = route.params.id as string

const setmeal = ref<any>(null)
const loading = ref(true)
const quantity = ref(1)
const addingToCart = ref(false)

// 处理返回按钮点击
const handleBack = () => {
  // 从localStorage获取上次选中的分类ID
  const lastCategoryId = localStorage.getItem('lastCategoryId')
  
  if (lastCategoryId) {
    console.log('从localStorage获取的分类ID:', lastCategoryId)
    router.push({
      path: '/menu',
      query: { activeCategory: lastCategoryId }
    })
  } else {
    router.back()
  }
}

// 获取套餐详情
const getSetmealDetail = async () => {
  try {
    loading.value = true
    const res = await getSetmealDetailAPI(setmealId)
    setmeal.value = res.data.data
  } catch (error) {
    console.error('获取套餐详情失败', error)
    ElMessage.error('获取套餐详情失败')
  } finally {
    loading.value = false
  }
}

// 加入购物车
const addToCart = async () => {
  try {
    addingToCart.value = true
    
    // 构建购物车数据
    const cartData = {
      setmealId: setmealId,
      number: quantity.value
    }
    
    await axios.post('/cart/add', cartData)
    ElMessage.success('加入购物车成功')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.msg || '加入购物车失败')
  } finally {
    addingToCart.value = false
  }
}

onMounted(() => {
  getSetmealDetail()
})
</script>

<style scoped>
.setmeal-detail-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.loading-container {
  padding: 20px;
}

.setmeal-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.setmeal-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 8px;
}

.setmeal-image .el-image {
  width: 100%;
  height: 100%;
}

.setmeal-info {
  padding: 10px 0;
}

.setmeal-name {
  font-size: 20px;
  font-weight: bold;
  margin: 0 0 10px 0;
}

.setmeal-price {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.price {
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
  margin-right: 10px;
}

.sold {
  font-size: 14px;
  color: #999;
}

.setmeal-description h3,
.setmeal-dishes h3 {
  font-size: 16px;
  margin: 15px 0 10px 0;
}

.setmeal-description p {
  color: #666;
  line-height: 1.5;
  margin: 0;
}

.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  background-color: #f8f8f8;
  border-radius: 8px;
  margin-top: 20px;
}

.quantity-control {
  display: flex;
  align-items: center;
}
</style>