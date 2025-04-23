<template>
  <div class="app-container">
    <!-- 固定标题栏 -->
    <div class="header-title">
      <h1>HKU Alpha Cafe</h1>
    </div>
    
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
      <div class="nav-item" @click="router.push('/user')" :class="{ active: route.path === '/user' }">
        <el-icon><User /></el-icon>
        <span>我的</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { HomeFilled, Menu, User } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
</script>

<style scoped lang="less">
.app-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  position: relative;
}

.header-title {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 50px;
  background-color: #e65c00;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.header-title h1 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding-top: 50px;
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
    color: #ff9966;
  }
}


</style>