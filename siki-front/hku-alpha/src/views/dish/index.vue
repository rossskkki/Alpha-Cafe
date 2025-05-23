<script setup lang="ts">

import { reactive, ref } from 'vue'
import { getDishPageListAPI, updateDishStatusAPI, deleteDishesAPI, deleteHotDishesAPI} from '@/api/dish'
import { getCategoryPageListAPI } from '@/api/category'
import { ElMessage, ElMessageBox, ElTable } from 'element-plus'
import { useRouter } from 'vue-router'

// ------ .d.ts 属性类型接口 ------
// 接收到不在接口中定义的属性的数据，ts会报错，但是类型推断错误不会妨碍接收，控制台还是能打印的
interface dish {
  id: number
  name: string
  image: string
  description: string
  price: number
  status: string
  categoryId: number
  updateTime: string
  isHot?: number // 是否为热点菜品，1是，0否
}
interface Category {
  id: number
  name: string
}


// ------ 数据 ------

// 当前页的菜品列表
const dishList = ref<dish[]>([])
// 菜品id对应的分类列表，即categoryId字段不能只展示id值，应该根据id查询到对应的分类名进行回显
const categoryList = ref<Category[]>([])
// 分页参数
const pageData = reactive({
  name: '',
  categoryId: '',
  status: '',
  page: 1,
  pageSize: 6,
})
const total = ref(0)
const options = [
  {
    value: '1',
    label: '起售',
  },
  {
    value: '0',
    label: '停售',
  }
]



// ------ 方法 ------

// 页面初始化
const init = async () => {
  const { data: res_category } = await getCategoryPageListAPI({ page: 1, pageSize: 100, type: 1 })
  console.log('分类列表')
  console.log(res_category.data)
  categoryList.value = res_category.data.records
  console.log('categoryList: ', categoryList.value)
}
// 刷新页面数据
const showPageList = async () => {
  const { data: res } = await getDishPageListAPI(pageData)
  console.log('菜品列表')
  console.log(res.data)
  // 对菜品列表进行排序，热点菜品置顶显示
  const records = res.data.records
  records.sort((a, b) => {
    // 如果a是热点菜品而b不是，a排在前面
    if (a.isHot === 1 && b.isHot !== 1) return -1
    // 如果b是热点菜品而a不是，b排在前面
    if (a.isHot !== 1 && b.isHot === 1) return 1
    // 其他情况保持原有顺序
    return 0
  })
  dishList.value = records
  total.value = res.data.total
}
init() // 页面初始化，写在这里时的生命周期是beforecreated/created的时候
showPageList() // 页面一开始就要展示分页菜品列表

// 监听翻页和每页显示数量的变化
const handleCurrentChange = (val: number) => {
  pageData.page = val
  // 根据输入框是否有值/进行了查询，来决定是所有歌曲还是查询后的列表
  showPageList()
}

const handleSizeChange = (val: number) => {
  pageData.pageSize = val
  // 根据输入框是否有值/进行了查询，来决定是所有歌曲还是查询后的列表
  showPageList()
}

const multiTableRef = ref<InstanceType<typeof ElTable>>()
const multiSelection = ref<dish[]>([])

const handleSelectionChange = (val: dish[]) => {
  multiSelection.value = val
  console.log('value', val)
  console.log('multiSelection.value', multiSelection.value)
}

// 设置表格行的类名，为热点菜品添加特殊样式
const tableRowClassName = ({ row }: { row: dish }) => {
  return row.isHot === 1 ? 'hot-dish-row' : ''
}

// 新增和修改菜品都是同一个页面，不过要根据路径传参的方式来区分
const router = useRouter()
const to_add_update = (row?: any, isHot?: number) => {
  console.log('看有没有传过来，来判断要add还是update', row)
  if (row && row.id) {
    router.push({
      path: '/dish/add',
      query: { id: row.id }
    })
  } else {
    router.push({
      path: '/dish/add',
      query: isHot ? { isHot: isHot } : {}
    })
  }
}

// 添加热点菜品
const to_add_hot_dish = () => {
  to_add_update(undefined, 1)
}

// 修改菜品状态
const change_btn = async (row: any) => {
  console.log('要修改的行数据')
  console.log(row.id, row.status)
  if (row.isHot === 1) {
    ElMessage({
      type: 'warning',
      message: '热点菜品不能修改状态',
    })
    return
  }
  await updateDishStatusAPI(row.id, row.status === 1 ? 0 : 1)
  // 修改后刷新页面，更新数据
  showPageList()
  ElMessage({
    type: 'success',
    message: '修改成功',
  })
}

// 删除菜品
const deleteBatch = (row?: any) => {
  console.log('要删除的行数据')
  console.log(row)
  ElMessageBox.confirm(
    '该操作会永久删除菜品，是否继续？',
    'Warning',
    {
      confirmButtonText: 'OK',
      cancelButtonText: 'Cancel',
      type: 'warning',
    }
  )
    .then(async () => {
      // 1. 没传入行数据，批量删除
      if (row == undefined) {
        console.log(multiSelection.value)
        if (multiSelection.value.length == 0) {
          ElMessage({
            type: 'warning',
            message: '请先选择要删除的菜品',
          })
          return
        }
        // 拿到当前 multiSelection.value 的所有id，然后调用批量删除接口
        let ids: any = []
        let hotids: any = []
        multiSelection.value.map(item => {
          if (item.isHot === 1) {
            hotids.push(item.id)
          } else {
            ids.push(item.id)
          }
        })
        ids = ids.join(',')
        hotids = hotids.join(',')
        console.log('ids', ids)
        console.log('hotids', hotids)
        let res;
        if (ids) {
          res = await deleteDishesAPI(ids)
          if (res.data.code != 0) return
        }
        if (hotids) {
          res = await deleteHotDishesAPI(hotids)
          if (res.data.code != 0) return
        }
      }
      // 2. 传入行数据，单个删除
      else {
        console.log('id包装成数组，然后调用批量删除接口')
        console.log(row.id)
        if (row.isHot === 1) {
          let res = await deleteHotDishesAPI(row.id)
          if (res.data.code!= 0) return 
        }else {
          let res = await deleteDishesAPI(row.id)
          if (res.data.code!= 0) return
        }
      }
      // 删除后刷新页面，更新数据
      showPageList()
      ElMessage({
        type: 'success',
        message: '删除成功',
      })
    })
    .catch(() => {
      ElMessage({
        type: 'info',
        message: '取消删除',
      })
    })
}
</script>

<template>
  <el-card>
    <div class="horizontal">
      <el-input size="large" class="input" v-model="pageData.name" placeholder="请输入菜品名" />
      <el-select size="large" class="input" clearable v-model="pageData.categoryId" placeholder="选择分类类型">
        <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-select class="input" clearable v-model="pageData.status" placeholder="选择菜品状态" size="large">
        <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button size="large" class="btn" round type="success" @click="showPageList()">查询菜品</el-button>
      <el-button size="large" class="btn" round type="danger" @click="deleteBatch()">批量删除</el-button>
      <el-button size="large" class="btn" type="primary" @click="to_add_update()">
        <el-icon style="font-size: 15px; margin-right: 10px;">
          <Plus />
        </el-icon>添加菜品
      </el-button>
      <el-button size="large" class="btn" type="warning" @click="to_add_hot_dish()">
        <el-icon style="font-size: 15px; margin-right: 10px;">
          <Plus />
        </el-icon>添加热点菜品
      </el-button>
    </div>
    <el-table class="table_box" ref="multiTableRef" :data="dishList" stripe @selection-change="handleSelectionChange" :row-class-name="tableRowClassName">
      <el-table-column type="selection" width="55" />
      <!-- <el-table-column prop="id" label="id" /> -->
      <el-table-column prop="name" label="菜名" align="center" />
      <el-table-column prop="image" label="图片" align="center">
        <template #default="scope">
          <img v-if="scope.row.image" :src="scope.row.image" alt="" />
          <img v-else src="/src/assets/image/user_default.png" alt="" />
        </template>
      </el-table-column>
      <el-table-column prop="description" label="详情" width="200px" align="center" />
      <el-table-column prop="price" label="价格" align="center" />
      <el-table-column prop="status" label="状态" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" round>
            {{ scope.row.status === 1 ? '启售' : '停售' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="categoryId" label="所属分类" align="center">
        <!-- scope 的父组件是 el-table -->
        <template #default="scope">
          <!-- 遍历categoryList，找到categoryId对应的name   ?.防止找不到对应关系而报错 -->
          {{ categoryList.find(item => item.id === scope.row.categoryId)?.name }}
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="上次操作时间" width="180px" align="center" />
      <el-table-column label="操作" width="200px" align="center">
        <template #default="scope">
          <el-button @click="to_add_update(scope.row)" type="primary">修改</el-button>
          <el-button @click="change_btn(scope.row)" plain :type="scope.row.status === 1 ? 'danger' : 'primary'">
            {{ scope.row.status === 1 ? '停售' : '起售' }}</el-button>
          <el-button @click="deleteBatch(scope.row)" type="danger">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description=" 没有数据" />
      </template>
    </el-table>

    <!-- element ui 官方推荐使用 v-model 双向绑定 而不是使用事件监听 -->
    <!-- 但是为了监听后还要调用相关函数，看来只能用事件了... -->
    <!-- 有没有办法让v-model的值发生改变时自动触发更新函数？ -->
    <el-pagination class="page" background layout="total, sizes, prev, pager, next, jumper" :total="total"
      :page-sizes="[2, 4, 6, 8]" v-model:current-page="pageData.page" v-model:page-size="pageData.pageSize"
      @current-change="handleCurrentChange" @size-change="handleSizeChange" />
  </el-card>
</template>


<style lang="less" scoped>
// element-plus的样式修改
.el-table {
  width: 90%;
  height: 500px;
  margin: 3rem auto;
  text-align: center;
  border: 1px solid #e4e4e4;
}

:deep(.el-table tr) {
  font-size: 12px;
}

.el-button {
  width: 45px;
  font-size: 12px;
}

.el-pagination {
  justify-content: center;
}

// 自定义样式
body {
  background-color: #c91c1c;
}

.horizontal {
  display: flex;
  justify-content: space-around;
  align-items: center;
  margin: 0 80px;

  .input {
    width: 160px;
  }

  .btn {
    width: 120px;
  }
}

img {
  width: 50px;
  height: 50px;
  border-radius: 10px;
}

.add_btn {
  width: 100px;
  height: 40px;
  margin-left: 900px;
}

.el-card{
  background-color: #ffe3b0;
}

/* 热点菜品行样式 */
:deep(.hot-dish-row) {
  background-color: #ffcccc !important;
}

/* 确保热点菜品样式覆盖斑马纹样式 */
:deep(.el-table--striped .hot-dish-row),
:deep(.el-table--striped .hot-dish-row.el-table__row--striped) {
  background-color: #ffcccc !important;
}

/* 添加更高优先级选择器确保热点菜品样式生效 */
:deep(.el-table__row.hot-dish-row) {
  background-color: #ffcccc !important;
}

/* 确保热点菜品行的所有单元格都有红色背景 */
:deep(.el-table__row.hot-dish-row > td) {
  background-color: #ffcccc !important;
}

/* 鼠标悬停时的样式 */
:deep(.el-table__row.hot-dish-row:hover > td) {
  background-color: #ffaaaa !important;
}

</style>