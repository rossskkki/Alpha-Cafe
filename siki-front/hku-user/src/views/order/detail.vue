<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOrderDetailAPI, cancelOrderAPI } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

// 订单状态
const orderStatus = {
  1: '待付款',
  2: '待接单',
  3: '待制作',
  4: '制作中',
  5: '已完成',
  6: '已取消'
}

// 订单详情数据
const orderDetail = ref<any>({})
const loading = ref(true)

// 获取路由参数
const route = useRoute()
const orderId = route.params.id as string

// 获取订单详情
const getOrderDetail = async () => {
  loading.value = true
  try {
    const res = await getOrderDetailAPI(orderId)
    orderDetail.value = res.data.data
  } catch (error) {
    ElMessage.error('获取订单详情失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 取消订单
const cancelOrder = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelOrderAPI(orderId)
    ElMessage.success('订单取消成功')
    getOrderDetail() // 刷新订单详情
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消订单失败')
      console.error(error)
    }
  }
}

// 格式化时间
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  getOrderDetail()
})
</script>

<template>
  <div class="order-detail-container">
    <div class="page-header">
      <el-page-header @back="$router.back()" title="返回" :content="`订单详情 #${orderDetail.number || ''}`" />
    </div>
    
    <el-card v-loading="loading" class="order-detail-card">
      <template v-if="orderDetail.id">
        <div class="order-status-section">
          <div class="status-icon" :class="`status-${orderDetail.status}`">
            <el-icon size="32"><component :is="orderDetail.status === 6 ? 'CircleClose' : (orderDetail.status === 5 ? 'CircleCheck' : 'Loading')" /></el-icon>
          </div>
          <div class="status-info">
            <h3>{{ orderStatus[orderDetail.status] }}</h3>
            <p v-if="orderDetail.status === 1">请尽快完成支付</p>
            <p v-else-if="orderDetail.status === 2">商家正在处理您的订单</p>
            <p v-else-if="orderDetail.status === 3">商家正在准备配送</p>
            <p v-else-if="orderDetail.status === 4">骑手正在配送中</p>
            <p v-else-if="orderDetail.status === 5">订单已完成，感谢您的惠顾</p>
            <p v-else-if="orderDetail.status === 6">订单已取消</p>
          </div>
        </div>
        
        <el-divider />
        
        <div class="order-info-section">
          <h3>订单信息</h3>
          <div class="info-item">
            <span class="label">订单编号：</span>
            <span class="value">{{ orderDetail.number }}</span>
          </div>
          <div class="info-item">
            <span class="label">下单时间：</span>
            <span class="value">{{ formatDate(orderDetail.orderTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">支付方式：</span>
            <span class="value">{{ orderDetail.payMethod === 1 ? '微信支付' : '支付宝支付' }}</span>
          </div>
          <div class="info-item">
            <span class="label">支付状态：</span>
            <span class="value">{{ orderDetail.payStatus === 1 ? '已支付' : '未支付' }}</span>
          </div>
        </div>
        
        <div class="order-items-section">
          <h3>订单内容</h3>
          <div class="order-items-list">
            <div v-for="(item, index) in orderDetail.orderDetailList" :key="index" class="order-item">
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-quantity">x{{ item.number }}</div>
                <div class="item-price">¥{{ item.amount }}</div>
              </div>
            </div>
          </div>
          
          <div class="order-summary">
            <div class="summary-item">
              <span>商品金额：</span>
              <span>¥{{ orderDetail.amount }}</span>
            </div>
            <div class="summary-item total">
              <span>实付金额：</span>
              <span>¥{{ orderDetail.amount }}</span>
            </div>
          </div>
        </div>
        
        <div class="order-actions" v-if="orderDetail.status === 2">
          <el-button type="danger" @click="cancelOrder">取消订单</el-button>
        </div>
      </template>
    </el-card>
  </div>
</template>

<style scoped>
.order-detail-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.order-detail-card {
  margin-bottom: 20px;
}

.order-status-section {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.status-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  color: white;
}

.status-1 {
  background-color: #e6a23c;
}

.status-2, .status-3, .status-4 {
  background-color: #409eff;
}

.status-5 {
  background-color: #67c23a;
}

.status-6 {
  background-color: #f56c6c;
}

.status-info h3 {
  margin: 0 0 10px 0;
  font-size: 20px;
}

.status-info p {
  margin: 0;
  color: #666;
}

.order-info-section,
.delivery-info-section,
.order-items-section {
  margin-bottom: 20px;
}

.order-info-section h3,
.delivery-info-section h3,
.order-items-section h3 {
  margin-bottom: 15px;
  font-size: 18px;
  color: #333;
}

.info-item {
  display: flex;
  margin-bottom: 10px;
}

.label {
  width: 100px;
  color: #666;
}

.value {
  flex: 1;
  color: #333;
}

.order-items-list {
  margin-bottom: 20px;
}

.order-item {
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.order-item:last-child {
  border-bottom: none;
}

.item-info {
  display: flex;
  align-items: center;
}

.item-name {
  flex: 1;
}

.item-quantity {
  width: 60px;
  text-align: center;
  color: #666;
}

.item-price {
  width: 80px;
  text-align: right;
  font-weight: bold;
}

.order-summary {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.total {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.order-actions {
  margin-top: 30px;
  display: flex;
  justify-content: flex-end;
}
</style>