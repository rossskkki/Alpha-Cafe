<template>
  <div class="seckill-page-container">
    <el-button type="default" icon="ArrowLeft" circle class="back-button" @click="goBack"></el-button>
    <h2 class="page-title">限时秒杀专区</h2>
    <div v-if="loading" class="loading-state">加载中...</div>
    <div v-else-if="seckillVouchers.length === 0" class="empty-state">暂无秒杀活动，敬请期待！</div>
    <div v-else class="voucher-grid">
      <div v-for="voucher in seckillVouchers" :key="voucher.id" class="voucher-card">
        <img :src="loginBgImage" alt="代金券" class="voucher-image-full" />
        <div class="voucher-details">
          <div class="voucher-title-full">{{ voucher.title }}</div>
          <div class="voucher-value-container">
            <span class="voucher-value-full">¥{{ formatAmount(voucher.payValue) }}</span>
            <span class="original-price">¥{{ formatAmount(voucher.actualValue) }}</span>
            <span v-if="calculateDiscount(voucher.payValue, voucher.actualValue)" class="discount-tag">
              {{ calculateDiscount(voucher.payValue, voucher.actualValue) }}
            </span>
          </div>
          <div class="voucher-stock">库存: {{ voucher.stock }}</div>
          <div class="voucher-time">
            <div>开始时间: {{ formatDateTime(voucher.beginTime) }}</div>
            <div>结束时间: {{ formatDateTime(voucher.endTime) }}</div>
          </div>
          <div class="voucher-rules-container">
            <div class="rules-title">使用规则：</div>
            <div class="voucher-rules">{{ voucher.rules }}</div>
          </div>
          <el-button type="danger" size="medium" class="grab-btn-full" @click="handleGrab(voucher)" :disabled="isVoucherDisabled(voucher)">
            {{ getButtonText(voucher) }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ArrowLeft } from '@element-plus/icons-vue' // 引入返回图标
import loginBgImage from '@/assets/pics/OIP-C.jpg'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSeckillVouchersAPI } from '@/api/voucher'
import type { UserVoucher } from '@/api/voucher'
import { seckillOrderAPI } from '@/api/order' // 引入秒杀下单 API
import { computed } from 'vue' // 引入 computed

const router = useRouter()
const seckillVouchers = ref<UserVoucher[]>([])
const loading = ref(true)

// 计算折扣
const calculateDiscount = (payValue: number, actualValue: number): string | null => {
  if (actualValue && actualValue > 0 && payValue < actualValue) {
    const discount = (payValue / actualValue) * 10
    // 保留一位小数，如果小数点后为0则不显示
    const discountFormatted = discount.toFixed(1).replace(/\.0$/, '')
    return `${discountFormatted}折`
  }
  return null // 如果原价不存在或不大于支付价，则不显示折扣
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 格式化金额（分到元）
const formatAmount = (amountInFen: number): string => {
  if (amountInFen === null || amountInFen === undefined) return 'N/A'
  return (amountInFen / 100).toFixed(2)
}

// 格式化日期时间
const formatDateTime = (dateTimeString: string): string => {
  if (!dateTimeString) return 'N/A'
  const date = new Date(dateTimeString)
  return date.toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

// 获取秒杀代金券
const loadSeckillVouchers = async () => {
  loading.value = true
  try {
    const { data: res } = await getSeckillVouchersAPI()
    if (res.code === 0) {
      seckillVouchers.value = res.data
    } else {
      ElMessage.error(res.msg || '获取秒杀活动失败')
    }
  } catch (error) {
    console.error('获取秒杀代金券失败', error)
    ElMessage.error('获取秒杀活动失败')
  } finally {
    loading.value = false
  }
}

// 判断代金券是否可以抢购（时间、库存）
const isVoucherDisabled = (voucher: UserVoucher): boolean => {
  const now = new Date().getTime()
  const beginTime = new Date(voucher.beginTime).getTime()
  const endTime = new Date(voucher.endTime).getTime()
  return now < beginTime || now > endTime || voucher.stock <= 0
}

// 获取按钮文本
const getButtonText = (voucher: UserVoucher): string => {
  const now = new Date().getTime()
  const beginTime = new Date(voucher.beginTime).getTime()
  const endTime = new Date(voucher.endTime).getTime()
  if (now < beginTime) return '尚未开始'
  if (now > endTime) return '已结束'
  if (voucher.stock <= 0) return '已抢光'
  return '立即抢购'
}

// 处理抢购点击
const handleGrab = async (voucher: UserVoucher) => {
  if (isVoucherDisabled(voucher)) {
    ElMessage.warning('当前无法抢购该代金券')
    return
  }
  try {
    // 调用秒杀下单接口
    const { data: res } = await seckillOrderAPI(voucher.id)
    if (res.code === 0) {
      ElMessage.success('抢购成功！订单已生成')
      // 可以在这里跳转到订单详情页或刷新当前页数据
      // router.push(`/order/${res.data.orderId}`)
      loadSeckillVouchers() // 刷新列表，更新库存等状态
    } else {
      ElMessage.error(res.msg || '抢购失败，请稍后再试')
    }
  } catch (error: any) {
    console.error('抢购失败', error)
    ElMessage.error(error.response?.data?.msg || '抢购失败，请稍后再试')
  }
}

onMounted(() => {
  loadSeckillVouchers()
})
</script>

<style scoped lang="less">
.back-button {
  position: absolute;
  top: 70px;
  left: 20px;
  z-index: 10;
}

.seckill-page-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 50px); // 减去可能的底部导航栏高度
}

.page-title {
  text-align: center;
  font-size: 24px;
  margin-bottom: 25px;
  color: #333;
}

.loading-state,
.empty-state {
  text-align: center;
  color: #999;
  padding: 50px 0;
  font-size: 16px;
}

.voucher-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); // 响应式网格布局
  gap: 20px;
}

.voucher-card {
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12);
  }
}

.voucher-image-full {
  width: 100%;
  height: 200px; // 图片高度
  object-fit: cover;
}

.voucher-details {
  padding: 15px;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.voucher-title-full {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.voucher-value-full {
  font-size: 22px;
  font-weight: bold;
  color: #f56c6c;
  margin-bottom: 10px;
}

.voucher-value-container {
  display: flex;
  align-items: baseline; // 基线对齐
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  margin-left: 8px;
}

.discount-tag {
  background-color: #f56c6c;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: 8px;
  font-weight: bold;
}

.voucher-stock {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
  margin-left: 8px;
}

.discount-tag {
  background-color: #f56c6c;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: 8px;
  font-weight: bold;
}

.voucher-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
  line-height: 1.5;
}

.voucher-rules-container {
  border-top: 1px solid #eee;
}

.rules-title {
  font-size: 14px;
  font-weight: bold;
  color: #555;
  margin-bottom: 5px;
}

.voucher-rules {
  font-size: 13px;
  color: #777;
  line-height: 1.6;
  background-color: #f9f9f9;
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px dashed #ddd;
}

.grab-btn-full {
  width: 100%;
  margin-top: auto; // 将按钮推到底部
  font-weight: bold;

  &.is-disabled {
    background-color: #fab6b6;
    border-color: #fab6b6;
  }
}

// 响应式调整
@media (max-width: 768px) {
  .voucher-grid {
    grid-template-columns: 1fr; // 移动端单列显示
  }
  .page-title {
    font-size: 20px;
  }
}
</style>