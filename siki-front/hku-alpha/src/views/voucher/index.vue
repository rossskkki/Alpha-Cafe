<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { getVoucherPageListAPI, updateVoucherStatusAPI } from '@/api/voucher'
import type { Voucher } from '@/api/voucher' // 引入定义好的Voucher接口
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { parseISO, format } from 'date-fns'; // 引入 date-fns 用于解析和格式化
import { deleteVoucherAPI } from '@/api/voucher' // 引入 deleteVoucherAPI
import { Plus, Delete } from '@element-plus/icons-vue' // 导入 Delete 图标

// ------ 数据 ------

// 当前页的优惠券列表
const voucherList = ref<Voucher[]>([])
// 带查询条件的分页参数
const pageData = reactive({
  title: '', // 按标题搜索
  page: 1,
  pageSize: 6,
  total: 0
})

const router = useRouter()

// ------ 方法 ------

// 获取优惠券列表
const loadVoucherList = async () => {
  const params = {
    page: pageData.page,
    pageSize: pageData.pageSize,
    title: pageData.title || undefined // 如果title为空则不传
  }
  try {
    const { data: res } = await getVoucherPageListAPI(params)
    if (res.code === 0) {
      voucherList.value = res.data.records
      pageData.total = res.data.total
    } else {
      ElMessage.error(res.msg || '获取优惠券列表失败')
    }
  } catch (error) {
    ElMessage.error('请求失败，请检查网络')
    console.error('Failed to fetch voucher list:', error)
  }
}

// 页面挂载时加载数据
onMounted(() => {
  loadVoucherList()
})

// 监听翻页和每页显示数量的变化
const handleCurrentChange = (val: number) => {
  pageData.page = val
  loadVoucherList()
}

const handleSizeChange = (val: number) => {
  pageData.pageSize = val
  loadVoucherList()
}

// 跳转到新增优惠券页面
const goToAddVoucher = () => {
  router.push('/voucher/add')
}

// 修改优惠券状态 (上架/下架)
const changeStatus = async (row: Voucher) => {
  const targetStatus = row.status === 1 ? 2 : 1 // 切换状态
  const actionText = row.status === 1 ? '下架' : '上架'

  ElMessageBox.confirm(
    `确定要${actionText}该优惠券吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const { data: res } = await updateVoucherStatusAPI(row.id, targetStatus)
        if (res.code === 0) {
          ElMessage.success(`${actionText}成功`)
          loadVoucherList() // 刷新列表
        } else {
          ElMessage.error(res.msg || `${actionText}失败`)
        }
      } catch (error) {
        ElMessage.error('操作失败，请检查网络')
        console.error(`Failed to ${actionText} voucher:`, error)
      }
    })
    .catch(() => {
      ElMessage.info(`已取消${actionText}`)
    })
}

// 格式化金额（分到元）
const formatAmount = (amountInFen: number): string => {
  if (amountInFen === null || amountInFen === undefined) return 'N/A'
  return (amountInFen / 100).toFixed(2)
}

// 格式化时间戳
const formatTimestamp = (timestamp: number): string => {
  if (!timestamp) return 'N/A'
  return new Date(timestamp).toLocaleString()}
const formatLocalDateTimeString = (dateTimeString: string): string => {
  if (!dateTimeString) return 'N/A';
  try {
    // 解析 ISO 格式的字符串 (假设后端返回的是类似 '2023-10-27T10:30:00' 的格式)
    const date = parseISO(dateTimeString);
    // 格式化为本地可读的日期时间格式
    return format(date, 'yyyy-MM-dd HH:mm:ss');
  } catch (error) {
    console.error('Error formatting date time string:', dateTimeString, error);
    return '无效日期'; // 或者返回原始字符串或其他提示
  }
}

// 添加 deleteVoucher 方法 (如果尚未定义)
const deleteVoucher = (id: number) => {
  // 在这里实现删除逻辑，例如弹出确认框并调用API
  console.log('Attempting to delete voucher with ID:', id);
  ElMessageBox.confirm(
    '确定要删除该优惠券吗？此操作不可撤销。',
    '警告',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      // 在这里调用删除API
      try {
        const { data: res } = await deleteVoucherAPI(id); // 假设有 deleteVoucherAPI
        if (res.code === 0) {
          ElMessage.success('删除成功');
          loadVoucherList(); // 刷新列表
        } else {
          ElMessage.error(res.msg || '删除失败');
        }
      } catch (error) {
        ElMessage.error('操作失败，请检查网络');
        console.error('Failed to delete voucher:', error);
      }
    })
    .catch(() => {
      ElMessage.info('已取消删除');
    });
}

</script>

<template>
  <el-card>
    <div class="horizontal">
      <el-input size="large" class="input" v-model="pageData.title" placeholder="请输入优惠券标题" clearable @clear="loadVoucherList" @keyup.enter="loadVoucherList" />
      <el-button size="large" class="btn" round type="success" @click="loadVoucherList">查询优惠券</el-button>
      <el-button size="large" class="btn" type="primary" @click="goToAddVoucher">
        <el-icon style="font-size: 15px; margin-right: 10px;">
          <Plus />
        </el-icon>添加优惠券
      </el-button>
    </div>
    <el-table :data="voucherList" stripe>
      <el-table-column prop="title" label="标题" align="center" />
      <el-table-column label="面值(元)" align="center">
        <template #default="scope">
          {{ formatAmount(scope.row.actualValue) }}
        </template>
      </el-table-column>
      <el-table-column label="售价(元)" align="center">
        <template #default="scope">
          {{ formatAmount(scope.row.payValue) }}
        </template>
      </el-table-column>
       <el-table-column prop="stock" label="库存" align="center" />
      <el-table-column label="有效期" align="center" width="320px">
        <template #default="scope">
          {{ formatLocalDateTimeString(scope.row.beginTime) }} - {{ formatLocalDateTimeString(scope.row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : (scope.row.status === 2 ? 'danger' : 'info')" round>
            {{ scope.row.status === 1 ? '上架' : (scope.row.status === 2 ? '下架' : '过期') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150px" align="center">
        <template #default="scope">
          <el-button @click="changeStatus(scope.row)" plain :type="scope.row.status === 1 ? 'danger' : 'success'" :disabled="scope.row.status === 3">
            {{ scope.row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <!-- 使用 Element Plus 图标组件 -->
          <el-button @click="deleteVoucher(scope.row.id)" type="danger" circle>
             <el-icon><Delete /></el-icon>
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description=" 没有数据" />
      </template>
    </el-table>

    <el-pagination class="page" background layout="total, sizes, prev, pager, next, jumper" :total="pageData.total"
      :page-sizes="[6, 10, 20, 50]" v-model:current-page="pageData.page" v-model:page-size="pageData.pageSize"
      @current-change="handleCurrentChange" @size-change="handleSizeChange" />
  </el-card>
</template>

<style lang="less" scoped>
.horizontal {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.input {
  width: 240px;
  margin-right: 15px;
}

.btn {
  margin-left: 10px;
}

.el-table {
  width: 100%; // 改为100%以适应容器
  margin-top: 20px; // 调整上边距
  text-align: center;
  border: 1px solid #e4e4e4;
}

:deep(.el-table tr) {
  font-size: 12px;
}

.el-button {
  margin: 0 5px; // 调整按钮间距
}

.page {
  margin-top: 30px;
  justify-content: center;
}

// 保持与 category/index.vue 相似的样式
.el-card {
  min-height: 600px;
}
</style>