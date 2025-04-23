<template>
  <router-view />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #333;
  background-color: #f5f5f5;
  height: 100%;
}

a {
  text-decoration: none;
  color: inherit;
}

ul, li {
  list-style: none;
}

/* 全局覆盖ElementUI的CSS变量 */
:root {
  --el-color-primary: #ff6600 !important; /* 修改主色为橙色 */
  --el-color-primary-light-3: #ff9966 !important; /* 修改主色的淡3级颜色 */
  --el-color-primary-light-5: #ffb380 !important; /* 修改主色的淡5级颜色 */
  --el-color-primary-light-7: #ffcc99 !important; /* 修改主色的淡7级颜色 */
  --el-color-primary-light-8: #ffe0b3 !important; /* 修改主色的淡8级颜色 */
  --el-color-primary-light-9: #fff0cc !important; /* 修改主色的淡9级颜色 */
  --el-color-primary-dark-2: #e65c00 !important; /* 修改主色的深2级颜色 */
}
</style>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

onMounted(() => {
  userStore.initUserInfo()
})
</script>
