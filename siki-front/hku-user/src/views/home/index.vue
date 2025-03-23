<template>
  <div class="home-container">
    <!-- 顶部搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索菜品"
        prefix-icon="Search"
        clearable
        @clear="handleSearchClear"
        @input="handleSearch"
      />
    </div>

    <!-- 分类列表 -->
    <div class="category-list">
      <div
        v-for="item in categoryList"
        :key="item.id"
        class="category-item"
        :class="{ active: activeCategory === item.id }"
        @click="handleCategoryClick(item.id)"
      >
        {{ item.name }}
      </div>
    </div>

    <!-- 菜品列表 -->
    <div class="menu-list" v-if="activeCategory">
      <div class="menu-title">{{ getCategoryName(activeCategory) }}</div>
      <div class="dish-list" v-if="dishList.length > 0">
        <div v-for="dish in dishList" :key="dish.id" class="dish-item" @click="handleDishClick(dish)">
          <div class="dish-img">
            <img :src="dish.image" :alt="dish.name" />
          </div>
          <div class="dish-info">
            <div class="dish-name">{{ dish.name }}</div>
            <div class="dish-desc">{{ dish.description }}</div>
            <div class="dish-price">¥{{ dish.price }}</div>
          </div>
          <div class="dish-action">
            <el-button type="primary" circle @click.stop="addToCart(dish)">
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 套餐列表 -->
      <div class="setmeal-list" v-if="setmealList.length > 0">
        <div v-for="setmeal in setmealList" :key="setmeal.id" class="setmeal-item" @click="handleSetmealClick(setmeal)">
          <div class="setmeal-img">
            <img :src="setmeal.image" :alt="setmeal.name" />
          </div>
          <div class="setmeal-info">
            <div class="setmeal-name">{{ setmeal.name }}</div>
            <div class="setmeal-desc">{{ setmeal.description }}</div>
            <div class="setmeal-price">¥{{ setmeal.price }}</div>
          </div>
          <div class="setmeal-action">
            <el-button type="primary" circle @click.stop="addToCart(setmeal, true)">
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 无数据提示 -->
      <el-empty v-if="dishList.length === 0 && setmealList.length === 0" description="暂无数据" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCategoryListAPI, getDishListAPI, getSetmealListAPI } from '@/api/menu'
import { addToCartAPI } from '@/api/cart'
import { useCartStore } from '@/store/cart'

const router = useRouter()
const cartStore = useCartStore()

// 搜索关键词
const searchKeyword = ref('')

// 分类列表
const categoryList = ref<any[]>([])
// 当前选中的分类
const activeCategory = ref<number | null>(null)

// 菜品列表
const dishList = ref<any[]>([])
// 套餐列表
const setmealList = ref<any[]>([])

// 获取分类列表
const getCategoryList = async () => {
  try {
    const res = await getCategoryListAPI()
    if (res.data.code === 0) {
      categoryList.value = res.data.data
      // 默认选中第一个分类
      if (categoryList.value.length > 0) {
        activeCategory.value = categoryList.value[0].id
        getMenuList(activeCategory.value)
      }
    }
  } catch (error) {
    console.error('获取分类列表失败', error)
  }
}

// 获取菜品和套餐列表
const getMenuList = async (categoryId: number) => {
  try {
    // 获取菜品列表
    const dishRes = await getDishListAPI(categoryId)
    if (dishRes.data.code === 0) {
      dishList.value = dishRes.data.data
    }

    // 获取套餐列表
    const setmealRes = await getSetmealListAPI(categoryId)
    if (setmealRes.data.code === 0) {
      setmealList.value = setmealRes.data.data
    }
  } catch (error) {
    console.error('获取菜品和套餐列表失败', error)
  }
}

// 处理分类点击
const handleCategoryClick = (categoryId: number) => {
  activeCategory.value = categoryId
  getMenuList(categoryId)
}

// 获取分类名称
const getCategoryName = (categoryId: number) => {
  const category = categoryList.value.find(item => item.id === categoryId)
  return category ? category.name : ''
}

// 处理菜品点击
const handleDishClick = (dish: any) => {
  router.push(`/dish/${dish.id}`)
}

// 处理套餐点击
const handleSetmealClick = (setmeal: any) => {
  router.push(`/setmeal/${setmeal.id}`)
}

// 添加到购物车
const addToCart = async (item: any, isSetmeal = false) => {
  try {
    const params = {
      dishId: isSetmeal ? null : item.id,
      setmealId: isSetmeal ? item.id : null,
      number: 1
    }
    const res = await addToCartAPI(params)
    if (res.data.code === 0) {
      ElMessage.success('添加成功')
      // 更新购物车数据
      cartStore.getCartList()
    }
  } catch (error) {
    console.error('添加到购物车失败', error)
  }
}

// 处理搜索
const handleSearch = () => {
  // 实现搜索功能
  console.log('搜索关键词:', searchKeyword.value)
  // TODO: 根据关键词搜索菜品
}

// 处理清空搜索
const handleSearchClear = () => {
  // 清空搜索结果，显示所有菜品
  if (activeCategory.value) {
    getMenuList(activeCategory.value)
  }
}

// 页面加载时获取分类列表
onMounted(() => {
  getCategoryList()
  // 获取购物车数据
  cartStore.getCartList()
})
</script>

<style scoped lang="less">
.home-container {
  padding: 10px;
}

.search-bar {
  position: sticky;
  top: 0;
  z-index: 10;
  padding: 10px 0;
  background-color: #f5f5f5;
}

.category-list {
  display: flex;
  overflow-x: auto;
  padding: 10px 0;
  margin-bottom: 10px;
  
  &::-webkit-scrollbar {
    display: none;
  }
  
  .category-item {
    flex-shrink: 0;
    padding: 8px 16px;
    margin-right: 10px;
    background-color: #f0f0f0;
    border-radius: 20px;
    font-size: 14px;
    
    &.active {
      background-color: #409EFF;
      color: white;
    }
  }
}

.menu-list {
  .menu-title {
    font-size: 18px;
    font-weight: bold;
    margin-bottom: 15px;
    padding-left: 5px;
    border-left: 3px solid #409EFF;
  }
}

.dish-list, .setmeal-list {
  .dish-item, .setmeal-item {
    display: flex;
    padding: 15px 0;
    border-bottom: 1px solid #eee;
    
    .dish-img, .setmeal-img {
      width: 80px;
      height: 80px;
      margin-right: 15px;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        border-radius: 5px;
      }
    }
    
    .dish-info, .setmeal-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      
      .dish-name, .setmeal-name {
        font-size: 16px;
        font-weight: bold;
      }
      
      .dish-desc, .setmeal-desc {
        font-size: 12px;
        color: #999;
        margin: 5px 0;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
      
      .dish-price, .setmeal-price {
        font-size: 16px;
        color: #f56c6c;
        font-weight: bold;
      }
    }
    
    .dish-action, .setmeal-action {
      display: flex;
      align-items: center;
    }
  }
}
</style>