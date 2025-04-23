<template>
  <div class="menu-container">    
    <!-- 悬浮购物车按钮 -->
    <div class="floating-cart" @click="showCartDrawer = true">
      <el-badge :value="cartStore.cartCount.value" :max="99">
        <el-icon><ShoppingCart /></el-icon>
      </el-badge>
      <div class="cart-price">¥{{ cartStore.cartTotal }}</div>
    </div>
    <!-- 顶部搜索栏 -->
    <!-- <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索菜品"
        prefix-icon="Search"
        clearable
        @clear="handleSearchClear"
        @input="handleSearch"
      />
    </div> -->

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
    
    <!-- 热点菜品区域 -->
    <div class="hot-dish-section" v-if="hotDishList.length > 0">
      <div class="section-title">
        <span class="title-text">热点菜品</span>
        <span class="title-tag">HOT</span>
      </div>
      <div class="hot-dish-list">
        <div v-for="dish in hotDishList" :key="dish.id" class="hot-dish-item" @click="handleHotDishClick(dish)">
          <div class="hot-dish-img">
            <img :src="dish.image" :alt="dish.name" />
            <div class="hot-tag">热门</div>
          </div>
          <div class="hot-dish-info">
            <div class="hot-dish-name">{{ dish.name }}</div>
            <div class="hot-dish-desc">{{ dish.description }}</div>
            <div class="hot-dish-price">¥{{ dish.price }}</div>
          </div>
          <div class="hot-dish-action">
            <el-button type="primary" circle @click.stop="addToCart(dish)">
              <el-icon><Plus /></el-icon>
            </el-button>
          </div>
        </div>
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
  
    
    <!-- 购物车抽屉 -->
    <el-drawer v-model="showCartDrawer" direction="rtl" size="90%">
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
        <div class="cart-footer drawer-cart-footer">
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
          <el-button type="primary" @click="showCartDrawer = false">去点餐</el-button>
        </el-empty>
      </div>
    </el-drawer>
    
    <!-- 结算弹窗 -->
    <el-dialog v-model="checkoutDialogVisible" title="订单确认" width="90%">
      <div class="checkout-dialog">
        <!-- 就餐方式 -->
        <div class="dining-section">
          <div class="section-title">就餐方式</div>
          <el-radio-group v-model="diningMethod">
            <el-radio :label="1">堂食</el-radio>
            <el-radio :label="2">外带</el-radio>
          </el-radio-group>
        </div>
      
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
    
    <!-- 取餐码弹窗 -->
    <el-dialog v-model="pickupCodeDialogVisible" title="取餐码" width="80%" center>
      <div class="pickup-code-container">
        <div class="pickup-code">{{ pickupCode }}</div>
        <p class="pickup-tip">请凭此取餐码到取餐处取餐</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="pickupCodeDialogVisible = false">确定</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 店铺打烊弹窗 -->
    <el-dialog
      v-model="closedDialogVisible"
      title="温馨提示"
      width="80%"
      center
      :show-close="false"
    >
      <div class="closed-dialog-content">
        <el-icon class="closed-icon" :size="50" color="#F56C6C"><WarningFilled /></el-icon>
        <h3 class="closed-title">店铺已打烊</h3>
        <p class="closed-message">很抱歉，店铺当前已打烊，暂不接受点餐</p>
        <p class="closed-tip">请在营业时间内再次光临</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { format } from 'date-fns'
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCategoryListAPI, getDishListAPI, getSetmealListAPI, getHotDishListAPI, getShopStatusAPI} from '@/api/menu'
import { addToCartAPI, getCartListAPI, clearCartAPI, deleteCartItemAPI, addCartItemAPI, subCartItemAPI} from '@/api/cart'
import { submitOrderAPI } from '@/api/order'
import { useCartStore } from '@/store/cart'
import { useUserStore } from '@/store/user' // <-- Add this import
import { Plus, Minus, ShoppingCart, WarningFilled } from '@element-plus/icons-vue'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore() // <-- Add this line

// 搜索关键词
const searchKeyword = ref('')

// 购物车相关功能
const showCartDrawer = ref(false)

// 结算弹窗
const checkoutDialogVisible = ref(false)

// 就餐方式 1-堂食 2-外带
const diningMethod = ref(1)

// 订单备注
const orderRemark = ref('')

// 取餐码弹窗
const pickupCodeDialogVisible = ref(false)
const pickupCode = ref('')

// 分类列表
const categoryList = ref<any[]>([])
// 当前选中的分类
const activeCategory = ref<number | null>(null)

// 菜品列表
const dishList = ref<any[]>([])
// 套餐列表
const setmealList = ref<any[]>([])
// 热点菜品列表
const hotDishList = ref<any[]>([])

// 购物车详情显示控制
const showCartDetail = ref(false)

// 店铺状态 1-营业中 0-已打烊
const shopStatus = ref(1)
// 打烊弹窗
const closedDialogVisible = ref(false)

// 结算相关
// const checkoutDialogVisible = ref(false)
// const orderRemark = ref('')
const pickupMethod = ref(1) // 1-堂食 2-外带

// 取餐码相关
// const pickupCodeDialogVisible = ref(false)
// const pickupCode = ref('')

// 获取分类列表
const getCategoryList = async () => {
  try {
    const res = await getCategoryListAPI()
    categoryList.value = res.data.data
    if (categoryList.value.length > 0) {
      activeCategory.value = categoryList.value[0].id
      getMenuList()
    }
  } catch (error) {
    console.error('获取分类列表失败', error)
    ElMessage.error('获取分类列表失败')
  }
}

// 获取当前分类的类型
const getCategoryType = (id: number): number | undefined => {
  const category = categoryList.value.find(item => item.id === id)
  return category ? category.type : undefined
}

// 获取菜品和套餐列表
const getMenuList = async () => {
  if (!activeCategory.value) return
  
  try {
    const categoryType = getCategoryType(activeCategory.value)
    
    // 根据分类类型调用不同的API
    if (categoryType === 1) { // 菜品分类
      // 获取菜品列表
      const dishRes = await getDishListAPI(activeCategory.value)
      dishList.value = dishRes.data.data
      setmealList.value = [] // 清空套餐列表
    } else if (categoryType === 2) { // 套餐分类
      // 获取套餐列表
      const setmealRes = await getSetmealListAPI(activeCategory.value)
      setmealList.value = setmealRes.data.data
      dishList.value = [] // 清空菜品列表
    } else {
      // 如果类型未知，则同时获取菜品和套餐列表
      const dishRes = await getDishListAPI(activeCategory.value)
      dishList.value = dishRes.data.data
      
      const setmealRes = await getSetmealListAPI(activeCategory.value)
      setmealList.value = setmealRes.data.data
    }
  } catch (error) {
    console.error('获取菜品列表失败', error)
    ElMessage.error('获取菜品列表失败')
  }
}

// 获取分类名称
const getCategoryName = (id: number) => {
  const category = categoryList.value.find(item => item.id === id)
  return category ? category.name : ''
}

// 处理分类点击
const handleCategoryClick = (id: number) => {
  activeCategory.value = id
  localStorage.setItem('lastCategoryId', id.toString()) // 保存当前选中的分类ID
  getMenuList()
}

// 处理搜索
const handleSearch = () => {
  getMenuList()
}

// 处理清除搜索
const handleSearchClear = () => {
  searchKeyword.value = ''
  getMenuList()
}

// 处理菜品点击
const handleDishClick = (dish: any) => {
  // 判断店铺是否已打烊
  if (shopStatus.value === 0) {
    closedDialogVisible.value = true
    return
  }
  
  router.push({
    path: `/dish/${dish.id}`,
    query: { categoryId: activeCategory.value,
      isHot: dish.isHot
     }
  })
}

//处理热点菜品点击
const handleHotDishClick = (dish: any) => {
  // 判断店铺是否已打烊
  if (shopStatus.value === 0) {
    closedDialogVisible.value = true
    return
  }
  
  router.push({
    path: `/dish/${dish.id}`,
    query: {categoryId: activeCategory.value,
            isHot: dish.isHot
     }
  })
}

// 处理套餐点击
const handleSetmealClick = (setmeal: any) => {
  // 判断店铺是否已打烊
  if (shopStatus.value === 0) {
    closedDialogVisible.value = true
    return
  }
  
  router.push({
    path: `/setmeal/${setmeal.id}`,
    query: { categoryId: activeCategory.value }
  }) 
}

// 添加到购物车
const addToCart = async (item: any, isSetmeal = false) => {
  // 判断店铺是否已打烊
  if (shopStatus.value === 0) {
    closedDialogVisible.value = true
    return
  }
  
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

// 增加商品数量
const handleIncreaseItem = async (item: any) => {
  try {
    const params = {
      id: item.id,
      dishFlavor: item.dishFlavor
    }
    const res = await addCartItemAPI(params)
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
      const params = {
        id: item.id,
        dishFlavor: item.dishFlavor
      }
      const res = await subCartItemAPI(params)
      if (res.data.code === 0) {
        // 更新购物车数据
        cartStore.getCartList()
      }
  } catch (error) {
    console.error('减少商品数量失败', error)
  }
}

// 删除商品
// const handleDeleteItem = async (item: any) => {
//   try {
//     const res = await deleteCartItemAPI(item.id)
//     if (res.data.code === 0) {
//       ElMessage.success('删除成功')
//       // 更新购物车数据
//       cartStore.getCartList()
//     }
//   } catch (error) {
//     console.error('删除商品失败', error)
//   }
// }

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
  // 判断店铺是否已打烊
  if (shopStatus.value === 0) {
    closedDialogVisible.value = true
    showCartDrawer.value = false
    return
  }
  
  // 打开结算弹窗
  checkoutDialogVisible.value = true
  // 关闭购物车抽屉
  showCartDrawer.value = false
}

// 结算弹窗

// 生成随机取餐码
const generatePickupCode = () => {
  // 生成6位数字取餐码
  return Math.floor(100000 + Math.random() * 900000).toString()
}

// 提交订单
const handleSubmitOrder = async () => {
  try {
    const params = {
      payMethod: 1, // 1微信支付 2支付宝支付
      remark: orderRemark.value,
      diningMethod: diningMethod.value, // 1堂食 2外带
      amount: cartStore.cartTotal.value,
      userName: userStore.userInfo?.nickName || '',
      estimatedFinishedTime: format(new Date(Date.now() + 30 * 60 * 1000), "yyyy-MM-dd HH:mm:ss")// 预估完成时间
    }
    
    const res = await submitOrderAPI(params)
    if (res.data.code === 0) {
      ElMessage.success('下单成功')
      checkoutDialogVisible.value = false
      cartStore.clearCart()
      
      // 显示取餐码
      pickupCode.value = res.data.data.pickupCode
      pickupCodeDialogVisible.value = true
    }
  } catch (error) {
    console.error('提交订单失败', error)
    ElMessage.error('提交订单失败')
  }
}


// 获取店铺状态
const getShopStatus = async () => {
  try {
    const res = await getShopStatusAPI()
    shopStatus.value = res.data.data
    
    // 判断店铺状态，如果已打烊则显示弹窗
    if (shopStatus.value === 0) {
      closedDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取店铺状态失败', error)
    ElMessage.error('获取店铺状态失败')
  }
}

// 获取热点菜品列表
const getHotDishList = async () => {
  try {
    const res = await getHotDishListAPI()
    hotDishList.value = res.data.data || []
  } catch (error) {
    console.error('获取热点菜品失败', error)
    ElMessage.error('获取热点菜品失败')
  }
}

// 组件挂载时获取分类列表和购物车数据
onMounted(() => {

  // 获取店铺状态
  getShopStatus() 

  // 获取热点菜品
  getHotDishList()
  
  getCategoryList().then(() => {
    // 检查URL中是否有分类ID参数
    const urlCategoryId = router.currentRoute.value.query.activeCategory // 确保参数名称一致
    if (urlCategoryId && categoryList.value.some(cat => cat.id == urlCategoryId)) {
      // 如果URL中有分类ID且该分类存在，则选中它
      activeCategory.value = Number(urlCategoryId)
    } else if (categoryList.value.length > 0) {
      // 否则选中第一个分类
      activeCategory.value = categoryList.value[0].id
    }
    getMenuList()
  })
  
  cartStore.getCartList()
})
</script>

<style scoped lang="less">
.menu-container {
  padding: 10px;
  padding-bottom: 70px;
  
  /* 店铺打烊弹窗样式 */
  .closed-dialog-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20px 0;
    
    .closed-icon {
      margin-bottom: 15px;
    }
    
    .closed-title {
      font-size: 20px;
      font-weight: bold;
      color: #F56C6C;
      margin-bottom: 15px;
    }
    
    .closed-message {
      font-size: 16px;
      color: #606266;
      margin-bottom: 10px;
    }
    
    .closed-tip {
      font-size: 14px;
      color: #909399;
    }
  }
  
  /* 购物车抽屉样式 */
  .cart-content {
    display: flex;
    flex-direction: column;
    height: calc(100vh - 120px);
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
  
  .drawer-cart-footer {
    position: sticky;
    bottom: 0;
    left: 0;
    right: 0;
    background-color: #fff;
    z-index: 10;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  }
  
  .empty-cart {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    height: calc(100vh - 120px);
  }
  
  // 购物车底部栏
  .cart-bottom-bar {
    position: fixed;
    bottom: 60px;
    left: 0;
    right: 0;
    height: 50px;
    background-color: #fff;
    display: flex;
    align-items: center;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
    z-index: 99;
    
    .cart-info {
      flex: 1;
      display: flex;
      align-items: center;
      padding: 0 15px;
      height: 100%;
      
      .cart-icon {
        font-size: 24px;
        color: #ff9966;
        margin-right: 10px;
      }
      
      .cart-summary {
        display: flex;
        flex-direction: column;
        justify-content: center;
        
        .cart-items-count {
          font-size: 12px;
          color: #606266;
        }
        
        .cart-total-price {
          font-size: 16px;
          font-weight: bold;
          color: #f56c6c;
        }
      }
    }
    
    .checkout-btn {
      height: 100%;
      width: 120px;
      border-radius: 0;
      font-size: 16px;
    }
  }

.search-bar {
  margin-bottom: 15px;
}

.category-list {
  display: flex;
  overflow-x: auto;
  padding: 5px 0;
  margin-bottom: 15px;
  
  &::-webkit-scrollbar {
    display: none;
  }
  
  .category-item {
    flex-shrink: 0;
    padding: 8px 15px;
    margin-right: 10px;
    background-color: #f5f5f5;
    border-radius: 20px;
    font-size: 14px;
    
    &.active {
      background-color: #ff9966;
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
    border-left: 3px solid #ff9966;
  }
}

.dish-list, .setmeal-list {
  .dish-item, .setmeal-item {
    display: flex;
    padding: 15px 0;
    border-bottom: 1px solid #f5f5f5;
    
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
      align-items: flex-end;
      padding-bottom: 5px;
    }
  }
}

.bottom-cart {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 60px;
  background-color: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.cart-summary {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  height: 50px;
}

.cart-icon {
  font-size: 24px;
  color: #409EFF;
  margin-right: 10px;
}

// 热点菜品区域样式
.hot-dish-section {
  margin-bottom: 20px;
  background-color: #fff8f8;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  
  .section-title {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    
    .title-text {
      font-size: 18px;
      font-weight: bold;
      color: #f56c6c;
    }
    
    .title-tag {
      margin-left: 8px;
      background-color: #f56c6c;
      color: white;
      font-size: 12px;
      padding: 2px 6px;
      border-radius: 4px;
    }
  }
  
  .hot-dish-list {
    .hot-dish-item {
      display: flex;
      padding: 15px 0;
      border-bottom: 1px solid #ffeeee;
      
      &:last-child {
        border-bottom: none;
      }
      
      .hot-dish-img {
        position: relative;
        width: 90px;
        height: 90px;
        margin-right: 15px;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          border-radius: 8px;
        }
        
        .hot-tag {
          position: absolute;
          top: 0;
          right: 0;
          background-color: #f56c6c;
          color: white;
          font-size: 12px;
          padding: 2px 6px;
          border-radius: 0 8px 0 8px;
        }
      }
      
      .hot-dish-info {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        
        .hot-dish-name {
          font-size: 16px;
          font-weight: bold;
          color: #333;
        }
        
        .hot-dish-desc {
          font-size: 12px;
          color: #999;
          margin: 5px 0;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
        }
        
        .hot-dish-price {
          font-size: 16px;
          color: #f56c6c;
          font-weight: bold;
        }
      }
      
      .hot-dish-action {
        display: flex;
        align-items: flex-end;
        padding-bottom: 5px;
      }
    }
  }
}

.cart-info {
  flex: 1;
}

.cart-total {
  font-weight: bold;
  color: #F56C6C;
  font-size: 16px;
}

.checkout-btn {
  height: 36px;
}

.cart-detail {
  padding: 10px 15px;
  max-height: 300px;
  overflow-y: auto;
  border-top: 1px solid #f0f0f0;
}

.cart-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.cart-list {
  flex: 1;
  overflow-y: auto;
  
  .cart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 15px;
    border-bottom: 1px solid #f5f5f5;
    
    .cart-title {
      font-size: 16px;
      font-weight: bold;
    }
    
    .cart-clear {
      font-size: 14px;
      color: #999;
    }
  }
  
  .cart-items {
    padding: 0 15px;
    
    .cart-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px 0;
      border-bottom: 1px solid #f5f5f5;
      
      .item-info {
        .item-name {
          font-size: 14px;
          margin-bottom: 5px;
        }
        
        .item-price {
          font-size: 14px;
          color: #f56c6c;
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
}

.cart-footer {
  padding: 15px;
  border-top: 1px solid #f5f5f5;
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .cart-total-info {
    .total-price {
      font-size: 18px;
      color: #f56c6c;
      font-weight: bold;
    }
  }
  
  .checkout-btn {
    padding: 10px 20px;
  }
}

.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.checkout-dialog {
  .dining-section,
  .table-section,
  .order-items,
  .remark-section,
  .order-total {
    margin-bottom: 20px;
  }
  
  .section-title {
    font-size: 16px;
    font-weight: bold;
    margin-bottom: 10px;
  }
}

/* 悬浮购物车按钮样式 */
.floating-cart {
  position: fixed;
  right: 20px;
  bottom: 80px;
  width: 60px;
  height: 60px;
  background-color: #ff9966;
  color: white;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 99;
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: scale(1.05);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
  }
  
  .el-icon {
    font-size: 24px;
    margin-bottom: 2px;
  }
  
  .cart-price {
    font-size: 12px;
    font-weight: bold;
  }
  
  .el-badge {
    margin-bottom: 2px;
  }
  
  .table-section {
    .el-input-number {
      width: 120px;
    }
  }
  
  .order-items {
    .order-item {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #f5f5f5;
      
      .item-name {
        flex: 1;
      }
      
      .item-count {
        margin: 0 15px;
        color: #999;
      }
      
      .item-price {
        color: #f56c6c;
      }
    }
  }
  
  .order-total {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    
    .total-value {
      font-size: 18px;
      color: #f56c6c;
      font-weight: bold;
    }
  }
}

.pickup-code-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
  
  .pickup-code {
    font-size: 36px;
    font-weight: bold;
    color: #ff9966;
    margin-bottom: 15px;
    letter-spacing: 5px;
  }
  
  .pickup-tip {
    font-size: 14px;
    color: #999;
  }
}}
</style>