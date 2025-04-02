<template>
  <div class="app-container">
    <!-- 内容区域 -->
    <div class="main-content">
      <router-view />
    </div>
    
    <!-- 底部导航栏 -->
    <div class="footer-nav">
      <div class="nav-item" @click="router.push('/')" :class="{ active: route.path === '/' }">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </div>
      <div class="nav-item" @click="router.push('/menu')" :class="{ active: route.path === '/menu' }">
        <el-icon><Menu /></el-icon>
        <span>点单</span>
      </div>
      <div class="nav-item cart-nav-item" @click="router.push('/menu')" v-if="cartStore.cartCount > 0">
        <el-badge :value="cartStore.cartCount" :max="99">
          <el-icon><ShoppingCart /></el-icon>
        </el-badge>
        <span>购物车</span>
        <div class="cart-price">¥{{ cartStore.cartTotal }}</div>
      </div>
      <div class="nav-item" @click="router.push('/user')" :class="{ active: route.path === '/user' }">
        <el-icon><User /></el-icon>
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useCartStore } from '@/store/cart'
import { ShoppingCart, HomeFilled, Menu, User } from '@element-plus/icons-vue'
import { onMounted } from 'vue'

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()

onMounted(() => {
  cartStore.getCartList() // 获取购物车数据
})
</script>

<style scoped lang="less">
.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding-bottom: 60px;
}

.footer-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background-color: #fff;
  display: flex;
  justify-content: space-around;
  align-items: center;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 5px 0;
  position: relative;
  flex: 1;
  
  .el-icon {
    font-size: 22px;
    margin-bottom: 2px;
  }
  
  span {
    font-size: 12px;
  }
  
  &.active {
    color: #409EFF;
  }
  
  &.cart-nav-item {
    background-color: #409EFF;
    color: white;
    margin: 5px;
    border-radius: 20px;
    padding: 5px 15px;
    flex: 2;
    
    .cart-price {
      font-size: 12px;
      font-weight: bold;
      margin-top: 2px;
    }
    
    .el-badge {
      margin-bottom: 2px;
    }
  }
}
</style>