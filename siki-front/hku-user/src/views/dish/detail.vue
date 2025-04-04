<template>
  <div class="dish-detail-container">
    <div class="page-header">
      <el-page-header @back="handleBack" title="返回" content="菜品详情" />
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>
    
    <div v-else-if="dish" class="dish-content">
      <div class="dish-image">
        <el-image :src="dish.image" fit="cover" />
      </div>
      
      <div class="dish-info">
        <h2 class="dish-name">{{ dish.name }}</h2>
        <div class="dish-price">
          <span class="price">¥{{ dish.price }}</span>
          <span class="sold">月售 {{ dish.saleNum || 0 }}</span>
        </div>
        
        <div class="dish-description">
          <h3>菜品描述</h3>
          <p>{{ dish.description || '暂无描述' }}</p>
        </div>
        
        <div class="dish-flavors" v-if="dish.flavors && dish.flavors.length > 0">
          <h3>口味选择</h3>
          <div v-for="(flavor, index) in dish.flavors" :key="index" class="flavor-item">
            <span class="flavor-name">{{ flavor.name }}</span>
            <el-radio-group v-model="selectedFlavors[flavor.name]" v-if="flavor.value">
              <el-radio 
                v-for="value in flavor.value.split(',')" 
                :key="value" 
                :label="value"
              >
                {{ value }}
              </el-radio>
            </el-radio-group>
            <div v-else class="no-flavor">暂无口味选择</div>
          </div>
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
    
    <el-empty v-else description="菜品信息不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import {getDishDetailAPI} from '@/api/menu'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const dishId = route.params.id as string
const categoryId = route.query.categoryId // 获取URL中的分类ID参数

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

const dish = ref<any>(null)
const loading = ref(true)
const quantity = ref(1)
const addingToCart = ref(false)
const selectedFlavors = reactive<Record<string, string>>({})

// 获取菜品详情
const getDishDetail = async () => {
  try {
    loading.value = true
    const res = await getDishDetailAPI(dishId)
    dish.value = res.data.data
    
    // 初始化口味选择
    if (dish.value.flavors && dish.value.flavors.length > 0) {
      dish.value.flavors.forEach((flavor: any) => {
        // 添加对null值的检查
        if (flavor.value) {
          const values = flavor.value.split(',')
          selectedFlavors[flavor.name] = values[0] // 默认选择第一个口味
        } else {
          // 如果value为null，设置一个默认值或者跳过
          selectedFlavors[flavor.name] = '默认口味' // 或者其他合适的默认值
        }
      })
    }
  } catch (error) {
    console.error('获取菜品详情失败', error)
    ElMessage.error('获取菜品详情失败')
  } finally {
    loading.value = false
  }
}

// 获取选中的口味
const getSelectedFlavorsText = computed(() => {
  const flavors = []
  for (const key in selectedFlavors) {
    flavors.push(`${key}: ${selectedFlavors[key]}`)
  }
  return flavors.join('; ')
})

onMounted(() => {
  getDishDetail()
})

// 加入购物车
const addToCart = async () => {
  try {
    addingToCart.value = true
    
    // 构建购物车数据
    const cartData = {
      dishId: dishId,
      dishFlavor: getSelectedFlavorsText.value || '',
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

</script>

<style scoped>
.dish-detail-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.loading-container {
  padding: 20px;
}

.dish-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.dish-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 8px;
}

.dish-image .el-image {
  width: 100%;
  height: 100%;
}

.dish-info {
  padding: 10px 0;
}

.dish-name {
  font-size: 20px;
  font-weight: bold;
  margin: 0 0 10px 0;
}

.dish-price {
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

.dish-description h3,
.dish-flavors h3 {
  font-size: 16px;
  margin: 15px 0 10px 0;
}

.dish-description p {
  color: #666;
  line-height: 1.5;
  margin: 0;
}

.flavor-item {
  margin-bottom: 10px;
}

.flavor-name {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
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