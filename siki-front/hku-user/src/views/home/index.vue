<template>
  <div class="home-container">
    <!-- 顶部轮播图 -->
    <div class="banner-section">
      <el-carousel height="200px" indicator-position="outside" :interval="3000" arrow="hover">
        <el-carousel-item v-for="(dish, index) in hotDishes" :key="index">
          <div class="carousel-item" @click="handleDishClick(dish)">
            <img :src="dish.image" :alt="dish.name" class="carousel-image" />
            <div class="carousel-info">
              <h3>{{ dish.name }}</h3>
              <p class="price">¥{{ dish.price }}</p>
              <el-button type="primary" size="small" class="add-btn" @click.stop="addToCart(dish)">
                <el-icon><Plus /></el-icon> 加入购物车
              </el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>
    
    <!-- 快捷点单按钮 -->
    <div class="quick-order">
      <el-button type="primary" size="large" @click="router.push('/menu')">
        <el-icon><Menu /></el-icon>
        <span>立即点单</span>
      </el-button>
    </div>
    
    <!-- 团购套餐 -->
    <div class="group-buy-section">
      <div class="section-header">
        <h2>团购套餐</h2>
        <span class="view-more" @click="router.push('/menu')">查看更多</span>
      </div>
      
      <div class="setmeal-list">
        <div v-for="setmeal in groupBuySetmeals" :key="setmeal.id" class="setmeal-card" @click="handleSetmealClick(setmeal)">
          <div class="setmeal-image">
            <img :src="setmeal.image" :alt="setmeal.name" />
            <div class="setmeal-tag">团购</div>
          </div>
          <div class="setmeal-info">
            <h3 class="setmeal-name">{{ setmeal.name }}</h3>
            <p class="setmeal-desc">{{ setmeal.description }}</p>
            <div class="setmeal-price-row">
              <span class="setmeal-price">¥{{ setmeal.price }}</span>
              <el-button type="primary" size="small" class="cart-btn" @click.stop="addToCart(setmeal, true)">
                <el-icon><Plus /></el-icon> 加入购物车
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 热销菜品 -->
    <div class="hot-dish-section">
      <div class="section-header">
        <h2>热销菜品</h2>
        <span class="view-more" @click="router.push('/menu')">查看更多</span>
      </div>
      
      <div class="dish-grid">
        <div v-for="dish in popularDishes" :key="dish.id" class="dish-card" @click="handleDishClick(dish)">
          <div class="dish-image">
            <img :src="dish.image" :alt="dish.name" />
            <div class="dish-tag">热销</div>
          </div>
          <div class="dish-info">
            <h3 class="dish-name">{{ dish.name }}</h3>
            <div class="dish-price-row">
              <span class="dish-price">¥{{ dish.price }}</span>
              <el-button type="primary" size="small" class="cart-btn" @click.stop="addToCart(dish)">
                <el-icon><Plus /></el-icon> 加入购物车
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 购物车悬浮按钮 -->
    <div class="floating-cart" v-if="cartCount > 0" @click="router.push('/menu')">
      <el-badge :value="cartCount" :max="99">
        <el-icon class="cart-icon"><ShoppingCart /></el-icon>
      </el-badge>
      <span class="cart-total">¥{{ cartTotal }}</span>
    </div>
    
    <!-- 店铺信息 -->
    <div class="shop-info">
      <h3><el-icon><Shop /></el-icon> HKU Alpha Cafe</h3>
      <p><el-icon><Clock /></el-icon> 营业时间: 09:00 - 22:00</p>
      <p><el-icon><Location /></el-icon> 地址: 香港大学校园内</p>
      <p><el-icon><Phone /></el-icon> 电话: 123-456-7890</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Shop, Clock, Location, Phone, Plus, Menu, ShoppingCart } from '@element-plus/icons-vue'
import { getDishListAPI, getSetmealListAPI, getCategoryListAPI } from '@/api/menu'
import { addToCartAPI } from '@/api/cart'
import { useCartStore } from '@/store/cart'

const router = useRouter()
const cartStore = useCartStore()

// 热销菜品数据（用于轮播图）
const hotDishes = ref<any[]>([])
// 团购套餐数据
const groupBuySetmeals = ref<any[]>([])
// 热销菜品数据（用于网格展示）
const popularDishes = ref<any[]>([])

// 购物车数量
const cartCount = computed(() => cartStore.cartCount)
// 购物车总价
const cartTotal = computed(() => cartStore.cartTotal)

// 获取热销菜品
const getHotDishes = async () => {
  try {
    // 获取热销菜品分类的菜品
    const categoryRes = await getCategoryListAPI()
    if (categoryRes.data.code === 0) {
      // 找到热销菜品分类
      const hotCategory = categoryRes.data.data.find((item: any) => item.name.includes('热销') || item.name.includes('推荐'))
      const categoryId = hotCategory ? hotCategory.id : 1 // 如果找不到，默认使用ID为1的分类
      
      const res = await getDishListAPI(categoryId)
      if (res.data.code === 0) {
        hotDishes.value = res.data.data.slice(0, 5) // 取前5个作为轮播图
        popularDishes.value = res.data.data.slice(0, 6) // 取前6个作为热销菜品
      }
    }
  } catch (error) {
    console.error('获取热销菜品失败', error)
  }
}

// 获取团购套餐
const getGroupBuySetmeals = async () => {
  try {
    // 获取套餐分类的套餐
    const categoryRes = await getCategoryListAPI()
    if (categoryRes.data.code === 0) {
      // 找到套餐分类
      const setmealCategory = categoryRes.data.data.find((item: any) => item.name.includes('套餐') || item.name.includes('团购'))
      const categoryId = setmealCategory ? setmealCategory.id : 2 // 如果找不到，默认使用ID为2的分类
      
      const res = await getSetmealListAPI(categoryId)
      if (res.data.code === 0) {
        groupBuySetmeals.value = res.data.data.slice(0, 3) // 取前3个作为团购套餐
      }
    }
  } catch (error) {
    console.error('获取团购套餐失败', error)
  }
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
    await addToCartAPI({
      dishId: isSetmeal ? null : item.id,
      setmealId: isSetmeal ? item.id : null,
      dishFlavor: '',
      number: 1
    })
    ElMessage.success('添加成功')
    cartStore.getCartList() // 刷新购物车数据
  } catch (error) {
    console.error('添加到购物车失败', error)
    ElMessage.error('添加到购物车失败')
  }
}

onMounted(() => {
  getHotDishes()
  getGroupBuySetmeals()
  cartStore.getCartList() // 获取购物车数据
})
</script>

<style scoped lang="less">
.home-container {
  padding-bottom: 60px;
  
  .cart-btn {
    transition: all 0.3s ease;
    
    &:hover {
      transform: scale(1.05);
      background-color: #67c23a;
    }
  }
}

.banner-section {
  margin: 10px 15px 20px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  
  .carousel-item {
    position: relative;
    height: 100%;
    width: 100%;
    
    .carousel-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 8px;
    }
    
    .carousel-info {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 15px;
      background: linear-gradient(to top, rgba(0,0,0,0.8), transparent);
      color: white;
      border-radius: 0 0 8px 8px;
      
      h3 {
        margin: 0 0 5px;
        font-size: 20px;
        font-weight: bold;
        text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
      }
      
      .price {
        margin: 0 0 10px 0;
        font-size: 18px;
        font-weight: bold;
        color: #FFD700;
        text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
      }
      
      .add-btn {
        margin-top: 5px;
        opacity: 0.9;
        transition: all 0.3s ease;
        
        &:hover {
          opacity: 1;
          transform: scale(1.05);
        }
      }
    }
  }
}

.quick-order {
  display: flex;
  justify-content: center;
  margin: 15px 0 25px;
  
  .el-button {
    width: 80%;
    max-width: 300px;
    height: 50px;
    font-size: 16px;
    font-weight: bold;
    border-radius: 25px;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
    
    &:hover {
      transform: scale(1.02);
      box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
    }
    
    .el-icon {
      margin-right: 5px;
    }
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 15px;
  margin-bottom: 15px;
  
  h2 {
    font-size: 18px;
    margin: 0;
    position: relative;
    padding-left: 10px;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 4px;
      height: 18px;
      background-color: #409EFF;
      border-radius: 2px;
    }
  }
  
  .view-more {
    font-size: 14px;
    color: #909399;
  }
}

.group-buy-section {
  margin-bottom: 20px;
  
  .setmeal-list {
    padding: 0 15px;
    
    .setmeal-card {
      display: flex;
      margin-bottom: 15px;
      background-color: white;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
      transition: transform 0.3s ease, box-shadow 0.3s ease;
      cursor: pointer;
      
      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
      }
      
      .setmeal-image {
        width: 120px;
        height: 120px;
        position: relative;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
        
        .setmeal-tag {
          position: absolute;
          top: 8px;
          left: 8px;
          background-color: #ff9800;
          color: white;
          padding: 2px 6px;
          border-radius: 4px;
          font-size: 12px;
          font-weight: bold;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
          z-index: 1;
        }
      }
      
      .setmeal-info {
        flex: 1;
        padding: 10px 15px;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        
        .setmeal-name {
          font-size: 16px;
          margin: 0 0 5px;
        }
        
        .setmeal-desc {
          font-size: 12px;
          color: #909399;
          margin: 0;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }
        
        .setmeal-price-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-top: 5px;
          
          .setmeal-price {
            font-size: 16px;
            color: #f56c6c;
            font-weight: bold;
          }
        }
      }
    }
  }
}

.hot-dish-section {
  margin-bottom: 20px;
  
  .dish-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
    padding: 0 15px;
    margin-bottom: 20px;
    
    .dish-card {
      background-color: white;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
      transition: transform 0.3s ease, box-shadow 0.3s ease;
      cursor: pointer;
      
      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
      }
      
      .dish-image {
        height: 120px;
        position: relative;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
        
        .dish-tag {
          position: absolute;
          top: 8px;
          right: 8px;
          background-color: #f56c6c;
          color: white;
          padding: 2px 6px;
          border-radius: 4px;
          font-size: 12px;
          font-weight: bold;
          box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
          z-index: 1;
        }
      }
      
      .dish-info {
        padding: 10px;
        
        .dish-name {
          font-size: 14px;
          margin: 0 0 5px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        
        .dish-price-row {
          display: flex;
          justify-content: space-between;
          align-items: center;
          
          .dish-price {
            font-size: 14px;
            color: #f56c6c;
            font-weight: bold;
          }
        }
      }
    }
  }
}

// 悬浮购物车按钮
.floating-cart {
  position: fixed;
  right: 20px;
  bottom: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  background-color: #409EFF;
  color: white;
  padding: 10px;
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  z-index: 10;
  cursor: pointer;
  transition: transform 0.3s ease;
  
  &:hover {
    transform: scale(1.1);
  }
  
  .cart-icon {
    font-size: 24px;
    margin-bottom: 5px;
  }
  
  .cart-total {
    font-size: 12px;
    font-weight: bold;
  }
}

.shop-info {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 12px;
  margin: 0 15px 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-left: 4px solid #409EFF;
  
  h3 {
    font-size: 18px;
    margin: 0 0 15px;
    color: #303133;
    display: flex;
    align-items: center;
    
    .el-icon {
      margin-right: 8px;
      color: #409EFF;
      font-size: 20px;
    }
  }
  
  p {
    margin: 8px 0;
    font-size: 14px;
    color: #606266;
    display: flex;
    align-items: center;
    
    .el-icon {
      margin-right: 8px;
      color: #909399;
      font-size: 16px;
    }
  }
}

// 移动端适配优化
@media (max-width: 768px) {
  .banner-section {
    margin: 10px 10px 15px;
  }
  
  .quick-order .el-button {
    width: 90%;
  }
  
  .section-header {
    padding: 0 10px;
  }
  
  .group-buy-section .setmeal-list,
  .hot-dish-section .dish-grid {
    padding: 0 10px;
  }
  
  .hot-dish-section .dish-grid {
    gap: 10px;
  }
  
  .shop-info {
    margin: 0 10px 15px;
    padding: 15px;
  }
  
  .floating-cart {
    right: 15px;
    bottom: 70px;
  }
}
</style>