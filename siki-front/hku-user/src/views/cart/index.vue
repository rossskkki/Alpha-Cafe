<template>
  <div>
    <div v-if="cartStore.cartList.length > 0" class="cart-content">
      <!-- 购物车列表 -->
      <div class="cart-list">
        <div class="cart-header">
          <div class="cart-title">购物车</div>
          <div class="cart-clear" @click="handleClearCart">清空购物车</div>
        </div>
        <div class="cart-items">
          <div v-for="item in cartStore.cartList" :key="item.id" class="cart-item">
            <div class="item-info">
              <div class="item-name">{{ item.name }}</div>
              <div class="item-price">¥{{ item.amount }}</div>
            </div>
            <div class="item-action">
              <el-button type="primary" size="small" circle @click="handleDecreaseItem(item)">
                <el-icon><Minus /></el-icon>
              </el-button>
              <span class="item-count">{{ item.number }}</span>
              <el-button type="primary" size="small" circle @click="handleIncreaseItem(item)">
                <el-icon><Plus /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 结算区域 -->
      <div class="cart-footer">
        <div class="cart-total">
          <span>合计：</span>
          <span class="total-price">¥{{ cartStore.cartTotal }}</span>
        </div>
        <el-button type="primary" class="checkout-btn" @click="handleCheckout">去结算</el-button>
      </div>
    </div>
    
    <!-- 空购物车提示 -->
    <div v-else class="empty-cart">
      <el-empty description="购物车空空如也，去添加商品吧~">
        <el-button type="primary" @click="router.push('/')">去点餐</el-button>
      </el-empty>
    </div>
    
    <!-- 结算弹窗 -->
    <el-dialog v-model="checkoutDialogVisible" title="订单确认" width="90%">
      <div class="checkout-dialog">
        <!-- 地址选择 -->
        <!-- <div class="address-section">
          <div class="section-title">配送地址</div>
          <div v-if="selectedAddress" class="selected-address" @click="handleSelectAddress">
            <div class="address-info">
              <div class="address-name">{{ selectedAddress.consignee }} {{ selectedAddress.phone }}</div>
              <div class="address-detail">{{ selectedAddress.detail }}</div>
            </div>
            <el-icon><ArrowRight /></el-icon>
          </div>
          <div v-else class="no-address" @click="handleSelectAddress">
            <span>请选择配送地址</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div> -->
        
        <!-- 订单商品 -->
        <div class="order-items">
          <div class="section-title">订单商品</div>
          <div v-for="item in cartStore.cartList" :key="item.id" class="order-item">
            <div class="item-name">{{ item.name }}</div>
            <div class="item-count">x{{ item.number }}</div>
            <div class="item-price">¥{{ (item.amount * item.number).toFixed(2) }}</div>
          </div>
        </div>
        
        <!-- 备注 -->
        <div class="remark-section">
          <div class="section-title">订单备注</div>
          <el-input v-model="orderRemark" placeholder="请输入备注信息" type="textarea" :rows="2" />
        </div>
        
        <!-- 订单金额 -->
        <div class="order-total">
          <div class="total-label">订单金额</div>
          <div class="total-value">¥{{ cartStore.cartTotal }}</div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="checkoutDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitOrder">提交订单</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '@/store/cart'
import { updateCartItemAPI, deleteCartItemAPI, clearCartAPI } from '@/api/cart'
import { submitOrderAPI } from '@/api/order'

const router = useRouter()
const cartStore = useCartStore()

// 结算弹窗
const checkoutDialogVisible = ref(false)
// 订单备注
const orderRemark = ref('')
// 选中的地址
const selectedAddress = ref<any>(null)
// 地址列表
const addressList = ref<any[]>([])

// 获取地址列表
const getAddressList = async () => {
  try {
    const res = await getAddressListAPI()
    if (res.data.code === 0) {
      addressList.value = res.data.data
      // 如果有默认地址，则选中默认地址
      const defaultAddress = addressList.value.find(item => item.isDefault === 1)
      if (defaultAddress) {
        selectedAddress.value = defaultAddress
      } else if (addressList.value.length > 0) {
        // 否则选中第一个地址
        selectedAddress.value = addressList.value[0]
      }
    }
  } catch (error) {
    console.error('获取地址列表失败', error)
  }
}

// 增加商品数量
const handleIncreaseItem = async (item: any) => {
  try {
    const params = {
      id: item.id,
      number: item.number + 1
    }
    const res = await updateCartItemAPI(params)
    if (res.data.code === 0) {
      // 更新购物车数据
      cartStore.getCartList()
    }
  } catch (error) {
    console.error('增加商品数量失败', error)
  }
}

// 减少商品数量
const handleDecreaseItem = async (item: any) => {
  try {
    if (item.number === 1) {
      // 如果数量为1，则删除该商品
      await handleDeleteItem(item)
    } else {
      const params = {
        id: item.id,
        number: item.number - 1
      }
      const res = await updateCartItemAPI(params)
      if (res.data.code === 0) {
        // 更新购物车数据
        cartStore.getCartList()
      }
    }
  } catch (error) {
    console.error('减少商品数量失败', error)
  }
}

// 删除商品
const handleDeleteItem = async (item: any) => {
  try {
    const res = await deleteCartItemAPI(item.id)
    if (res.data.code === 0) {
      ElMessage.success('删除成功')
      // 更新购物车数据
      cartStore.getCartList()
    }
  } catch (error) {
    console.error('删除商品失败', error)
  }
}

// 清空购物车
const handleClearCart = async () => {
  try {
    ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      const res = await clearCartAPI()
      if (res.data.code === 0) {
        ElMessage.success('清空成功')
        // 更新购物车数据
        cartStore.getCartList()
      }
    }).catch(() => {
      // 取消操作
    })
  } catch (error) {
    console.error('清空购物车失败', error)
  }
}

// 去结算
const handleCheckout = () => {
  // 检查是否有地址
  // if (addressList.value.length === 0) {
  //   ElMessageBox.confirm('您还没有添加收货地址，是否前往添加？', '提示', {
  //     confirmButtonText: '去添加',
  //     cancelButtonText: '取消',
  //     type: 'warning'
  //   }).then(() => {
  //     router.push('/address/edit')
  //   }).catch(() => {
  //     // 取消操作
  //   })
  //   return
  // }
  
  // 打开结算弹窗
  checkoutDialogVisible.value = true
}

// 选择地址
// const handleSelectAddress = () => {
//   router.push('/address')
// }

// 提交订单
const handleSubmitOrder = async () => {
  try {
    if (!selectedAddress.value) {
      ElMessage.warning('请选择配送地址')
      return
    }
    
    const params = {
      addressId: selectedAddress.value.id,
      remark: orderRemark.value
    }
    
    const res = await submitOrderAPI(params)
    if (res.data.code === 0) {
      ElMessage.success('下单成功')
      checkoutDialogVisible.value = false
      // 清空购物车
      cartStore.clearCart()
      // 跳转到订单页面
      router.push('/order')
    }
  } catch (error) {
    console.error('提交订单失败', error)
  }
}

onMounted(() => {
  // 获取购物车数据
  cartStore.getCartList()
  // 获取地址列表
  getAddressList()
})
</script>

<style scoped lang="less">
.cart-container {
  padding: 10px;
  padding-bottom: 70px; /* 为底部导航栏留出空间 */
}

.cart-content {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 80px);
}

.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
  
  .cart-title {
    font-size: 18px;
    font-weight: bold;
  }
  
  .cart-clear {
    color: #999;
    font-size: 14px;
  }
}

.cart-items {
  flex: 1;
  overflow-y: auto;
  
  .cart-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 0;
    border-bottom: 1px solid #eee;
    
    .item-info {
      flex: 1;
      
      .item-name {
        font-size: 16px;
        margin-bottom: 5px;
      }
      
      .item-price {
        color: #f56c6c;
        font-weight: bold;
      }
    }
    
    .item-action {
      display: flex;
      align-items: center;
      
      .item-count {
        margin: 0 10px;
        min-width: 20px;
        text-align: center;
      }
    }
  }
}

.cart-footer {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  border-top: 1px solid #eee;
  position: fixed;
  bottom: 60px;
  left: 0;
  right: 0;
  z-index: 99;
  
  .cart-total {
    font-size: 16px;
    
    .total-price {
      color: #f56c6c;
      font-weight: bold;
      font-size: 20px;
    }
  }
  
  .checkout-btn {
    padding: 10px 30px;
  }
}

.empty-cart {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: calc(100vh - 80px);
}

.checkout-dialog {
  .section-title {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 10px;
  }
  
  .address-section {
    margin-bottom: 20px;
    
    .selected-address, .no-address {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 10px;
      background-color: #f5f5f5;
      border-radius: 5px;
      
      .address-info {
        .address-name {
          font-weight: bold;
          margin-bottom: 5px;
        }
        
        .address-detail {
          color: #666;
          font-size: 14px;
        }
      }
    }
  }
  
  .order-items {
    margin-bottom: 20px;
    
    .order-item {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #eee;
      
      .item-name {
        flex: 1;
      }
      
      .item-count {
        margin: 0 20px;
      }
      
      .item-price {
        color: #f56c6c;
        font-weight: bold;
      }
    }
  }
  
  .remark-section {
    margin-bottom: 20px;
  }
  
  .order-total {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    font-size: 16px;
    
    .total-label {
      font-weight: bold;
    }
    
    .total-value {
      color: #f56c6c;
      font-weight: bold;
      font-size: 20px;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>