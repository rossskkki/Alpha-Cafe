<template>
  <div class="app-container">
    <!-- 内容区域 -->
    <div class="main-content">
      <router-view />
    </div>
    
    <!-- 底部导航栏 -->
    <div class="footer-nav">
      <div class="nav-item" @click="router.push('/')" :class="{ active: route.path === '/' }">
        <el-icon><Menu /></el-icon>
        <span>菜单</span>
      </div>
      <div class="nav-item" @click="router.push('/cart')" :class="{ active: route.path === '/cart' }">
        <el-icon><ShoppingCart /></el-icon>
        <span>购物车</span>
        <span v-if="cartStore.cartCount > 0" class="cart-badge">{{ cartStore.cartCount }}</span>
      </div>
      <div class="nav-item" @click="router.push('/order')" :class="{ active: route.path === '/order' }">
        <el-icon><Tickets /></el-icon>
        <span>订单</span>
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

const router = useRouter()
const route = useRoute()
const cartStore = useCartStore()
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
  
  .cart-badge {
    position: absolute;
    top: -2px;
    right: -10px;
    background-color: #F56C6C;
    color: white;
    border-radius: 10px;
    padding: 0 6px;
    font-size: 12px;
    min-width: 16px;
    height: 16px;
    line-height: 16px;
    text-align: center;
  }
}
</style>