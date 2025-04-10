<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrderListAPI } from '@/api/order'
import { ElMessage } from 'element-plus'

// 订单状态
const orderStatus = {
  1: '待付款',
  2: '待接单',
  3: '待制作',
  4: '制作中',
  5: '已完成',
  6: '已取消'
}

// 订单列表数据
const orderList = ref([])
// 分页参数
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 最新订单标识
const isLatestOrder = (index: number) => index === 0 && page.value === 1

// 路由实例
const router = useRouter()

// 获取订单列表
const getOrderList = async () => {
  loading.value = true
  try {
    const res = await getOrderListAPI({
      page: page.value,
      pageSize: pageSize.value
    })
    orderList.value = res.data.data.records
    total.value = res.data.data.total
  } catch (error) {
    ElMessage.error('获取订单列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 查看订单详情
const viewOrderDetail = (id: string) => {
  router.push(`/order/detail/${id}`)
}

// 页码变化
const handleCurrentChange = (val: number) => {
  page.value = val
  getOrderList()
}

// 页容量变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  page.value = 1
  getOrderList()
}

// 格式化时间
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

// 响应式屏幕宽度
const screenWidth = ref(window.innerWidth)

// 监听窗口大小变化
const handleResize = () => {
  screenWidth.value = window.innerWidth
}

// 组件挂载时获取订单列表并添加窗口大小监听
onMounted(() => {
  getOrderList()
  window.addEventListener('resize', handleResize)
})

// 组件卸载时移除窗口大小监听
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

// 判断日期是否为当天
const isToday = (dateStr: string) => {
  if (!dateStr) return false
  const orderDate = new Date(dateStr)
  const today = new Date()
  return orderDate.getFullYear() === today.getFullYear() &&
         orderDate.getMonth() === today.getMonth() &&
         orderDate.getDate() === today.getDate()
}
// 返回上一页
const goBack = () => {
  router.back()
}
</script>

<template>
  <div class="order-container">
    <div class="page-header-container">
      <el-button icon="ArrowLeft" size="big" @click="goBack" class="back-button"></el-button>
      <h2 class="page-title">我的订单</h2>
    </div>
    
    <div v-if="orderList.length === 0 && !loading" class="empty-order">
      <el-empty description="暂无订单数据" />
    </div>
    
    <el-card v-else v-loading="loading" class="order-list">
      <div v-for="(order, index) in orderList" :key="order.id" class="order-item" :class="{ 'latest-order': isLatestOrder(index) }">
        <div class="order-header">
          <span class="order-number">订单号: {{ order.number }}</span>
          <span class="order-status" :class="`status-${order.status}`">{{ orderStatus[order.status] }}</span>
        </div>
        
        <div class="order-content">
          <div class="order-dishes">
            <div v-for="(item, index) in order.orderDetails" :key="index" class="dish-item">
              <span class="dish-name">{{ item.name }}</span>
              <span class="dish-count">x{{ item.number }}</span>
            </div>
          </div>
          
          <div class="order-info">
            <p>下单时间: {{ formatDate(order.orderTime) }}</p>
            <div class="pickup-code-container" v-if="order.pickupCode && isToday(order.orderTime)">
              <p class="pickup-code-label">取餐码:</p>
              <div class="pickup-code">{{ order.pickupCode }}</div>
            </div>
            <p class="order-amount">实付金额: <span>¥{{ order.amount }}</span></p>
          </div>
        </div>
        
        <div class="order-footer">
          <el-button type="primary" size="small" @click="viewOrderDetail(order.id)">查看详情</el-button>
        </div>
      </div>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :layout="screenWidth <= 768 ? 'prev, pager, next' : 'total, sizes, prev, pager, next, jumper'"
          :total="total"
          :small="screenWidth <= 768"
          :pager-count="screenWidth <= 768 ? 5 : 7"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}
.order-container {
  padding: 20px;
}

.page-header-container {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.back-button {
  margin-right: 10px;
}

.page-title {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.empty-order {
  margin-top: 50px;
}

.order-list {
  margin-bottom: 20px;
}

.order-item {
  border-bottom: 1px solid #eee;
  padding: 15px 0;
}

.order-item:last-child {
  border-bottom: none;
}

.order-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
}

.order-number {
  font-weight: bold;
  color: #666;
}

.order-status {
  font-weight: bold;
}

.status-1 {
  color: #e6a23c;
}

.status-2 {
  color: #409eff;
}

.status-3 {
  color: #409eff;
}

.status-4 {
  color: #409eff;
}

.latest-order .order-header {
  border-bottom: 1px solid #e1f3d8;
}

.latest-order .pickup-code {
  font-size: 24px;
  background-color: #f56c6c;
  animation: pulse 1.5s infinite;
}

.latest-order {
  background-color: #f0f9eb;
  border-left: 3px solid #67c23a;
  padding-left: 12px;
}

.status-5 {
  color: #67c23a;
}

.status-6 {
  color: #f56c6c;
}

.order-content {
  display: flex;
  margin-bottom: 15px;
}

.order-dishes {
  flex: 1;
}

.dish-item {
  margin-bottom: 5px;
}

.dish-name {
  margin-right: 10px;
}

.dish-count {
  color: #999;
}

.order-info {
  flex: 1;
  color: #666;
}

.order-info p {
  margin: 5px 0;
}

.pickup-code-container {
  display: flex;
  align-items: center;
  margin: 10px 0;
}

.pickup-code-label {
  margin-right: 10px;
  font-weight: bold;
}

.pickup-code {
  background-color: #f56c6c;
  color: white;
  font-size: 20px;
  font-weight: bold;
  padding: 5px 15px;
  border-radius: 4px;
  letter-spacing: 2px;
}

.order-amount {
  font-weight: bold;
  margin-top: 10px;
}

.order-amount span {
  color: #f56c6c;
  font-size: 16px;
}

.order-footer {
  display: flex;
  justify-content: flex-end;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 移动设备上的分页样式优化 */
@media screen and (max-width: 768px) {
  .pagination-container {
    margin-top: 15px;
  }
  
  /* 增大分页按钮的点击区域 */
  :deep(.el-pagination .el-pager li) {
    min-width: 32px;
    height: 32px;
    line-height: 32px;
    margin: 0 3px;
  }
  
  /* 增大前进后退按钮的点击区域 */
  :deep(.el-pagination .btn-prev),
  :deep(.el-pagination .btn-next) {
    padding: 0 10px;
    min-width: 32px;
    height: 32px;
  }
  
  /* 调整分页组件的整体间距 */
  :deep(.el-pagination) {
    padding: 10px 5px;
    font-size: 14px;
  }
}
</style>