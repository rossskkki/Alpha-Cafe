<script lang="ts" setup>
import { reactive, ref, onMounted } from 'vue'
import { addVoucherAPI, getVoucherByIdAPI } from '@/api/voucher'
import type { VoucherAddDTO } from '@/api/voucher'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { format } from 'date-fns'; // 引入 date-fns 用于格式化

// ------ 配置 ------
const formLabelWidth = '100px' // 表单label宽度
const router = useRouter()
const route = useRoute()
const isEditMode = ref(false)
const voucherId = ref<number | null>(null)

// ------ 数据 ------
// Helper function to format Date to LocalDateTime string
const formatToLocalDateTimeString = (date: Date): string => {
  // 使用 date-fns 格式化，确保兼容性
  return format(date, "yyyy-MM-dd HH:mm:ss");
}

const initialBeginTime = new Date();
const initialEndTime = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);

const form = reactive<Omit<VoucherAddDTO, 'beginTime' | 'endTime'> & { id?: number, dateRange?: [Date, Date], beginTime: string, endTime: string }> ({
  title: '',
  subTitle: '',
  rules: '',
  actualValue: 0, // 存储分
  payValue: 0, // 存储分
  beginTime: formatToLocalDateTimeString(initialBeginTime), // 格式化为字符串
  endTime: formatToLocalDateTimeString(initialEndTime), // 格式化为字符串
  stock: 100,
  dateRange: [initialBeginTime, initialEndTime], // 保持 Date 对象用于选择器
})

// 用于表单展示和输入的金额（元）
const displayActualValue = ref<number | null>(null)
const displayPayValue = ref<number | null>(null)

// 日期时间选择器的值
// const dateRange = ref<[Date, Date]>([new Date(form.beginTime), new Date(form.endTime)]) // 删除这行

// 表单验证规则
const rules = {
  title: [{ required: true, message: '请输入代金券标题', trigger: 'blur' }],
  actualValue: [{ required: true, message: '请输入代金券面值', trigger: 'blur' }, { type: 'number', min: 0.01, message: '面值必须大于0', trigger: 'blur' }],
  payValue: [{ required: true, message: '请输入支付金额', trigger: 'blur' }, { type: 'number', min: 0, message: '支付金额不能为负', trigger: 'blur' }],
  dateRange: [{ required: true, message: '请选择有效期', trigger: 'change' }],
  stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }, { type: 'integer', min: 0, message: '库存必须为非负整数', trigger: 'blur' }]
}

const formRef = ref<any>(null) // 表单引用

// ------ 方法 ------

// 将元转换为分
const convertYuanToFen = (yuan: number | null): number => {
  if (yuan === null || yuan === undefined) return 0
  return Math.round(yuan * 100)
}

// 将分转换为元
const convertFenToYuan = (fen: number): number => {
  return fen / 100
}

// 处理金额输入变化，更新分单位的值
const handleActualValueChange = (value: number | null) => {
  form.actualValue = convertYuanToFen(value)
}

const handlePayValueChange = (value: number | null) => {
  form.payValue = convertYuanToFen(value)
}

// 处理日期范围变化
const handleDateRangeChange = (val: [Date, Date] | null) => {
  if (val) {
    form.beginTime = formatToLocalDateTimeString(val[0]) // 格式化为字符串
    form.endTime = formatToLocalDateTimeString(val[1]) // 格式化为字符串
    form.dateRange = val // 更新选择器的值
  } else {
    // 根据业务需求决定清空或设为默认值，这里暂时清空
    form.beginTime = ''
    form.endTime = ''
    form.dateRange = undefined
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      // 验证支付金额不大于面值
      if (form.payValue > form.actualValue) {
        ElMessage.error('支付金额不能大于代金券面值')
        return
      }

      // 准备提交的数据，确保类型匹配 VoucherAddDTO (string for times)
      const submitData: VoucherAddDTO = {
        title: form.title,
        subTitle: form.subTitle,
        rules: form.rules,
        actualValue: form.actualValue,
        payValue: form.payValue,
        beginTime: form.beginTime, // 已经是字符串
        endTime: form.endTime,   // 已经是字符串
        stock: form.stock,
      }

      try {
        // 根据是否是编辑模式调用不同API
        // if (isEditMode.value && voucherId.value) {
        //   // await updateVoucherAPI({ ...submitData, id: voucherId.value }); // 假设有更新API
        //   ElMessage.success('更新成功')
        // } else {
          await addVoucherAPI(submitData)
          ElMessage.success('添加成功')
        // }
        router.push('/voucher') // 成功后返回列表页
      } catch (error) {
        ElMessage.error('操作失败，请稍后重试')
        console.error('Submit voucher failed:', error)
      }
    } else {
      ElMessage.error('请检查表单填写是否正确')
      return false
    }
  })
}

// 返回列表页
const goBack = () => {
  router.push('/voucher')
}

// // 如果是编辑模式，加载现有数据 (暂未实现编辑功能)
// onMounted(async () => {
//   const id = route.query.id
//   if (id) {
//     isEditMode.value = true
//     voucherId.value = Number(id)
//     try {
//       // 注意：getVoucherByIdAPI 返回的 Voucher 类型现在时间也是 string
//       const { data: res } = await getVoucherByIdAPI(voucherId.value)
//       if (res.code === 0) {
//         const voucherData = res.data
//         form.id = voucherData.id
//         form.title = voucherData.title
//         form.subTitle = voucherData.subTitle
//         form.rules = voucherData.rules
//         form.actualValue = voucherData.actualValue
//         form.payValue = voucherData.payValue
//         // 直接使用后端返回的字符串时间
//         form.beginTime = voucherData.beginTime
//         form.endTime = voucherData.endTime
//         form.stock = voucherData.stock

//         // 更新用于显示的元单位金额
//         displayActualValue.value = convertFenToYuan(voucherData.actualValue)
//         displayPayValue.value = convertFenToYuan(voucherData.payValue)
//         // 更新日期范围选择器 - 需要将字符串转回 Date 对象
//         // 注意：这里假设后端返回的字符串是 'yyyy-MM-ddTHH:mm:ss' 格式
//         // 如果格式不同，需要调整解析方式
//         try {
//           form.dateRange = [new Date(voucherData.beginTime), new Date(voucherData.endTime)]
//         } catch (e) {
//            console.error("Error parsing date strings for date picker:", e);
//            // 可以设置一个默认值或留空
//            form.dateRange = undefined;
//         }

//       } else {
//         ElMessage.error(res.msg || '加载优惠券信息失败')
//         router.push('/voucher')
//       }
//     } catch (error) {
//       ElMessage.error('请求失败，请检查网络')
//       router.push('/voucher')
//     }
//   }
// })

</script>

<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>{{ isEditMode ? '编辑优惠券' : '新增优惠券' }}</span>
      </div>
    </template>

    <el-form :model="form" :rules="rules" ref="formRef" :label-width="formLabelWidth" style="max-width: 600px; margin: 20px auto;">
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入代金券标题" />
      </el-form-item>
      <el-form-item label="副标题" prop="subTitle">
        <el-input v-model="form.subTitle" placeholder="请输入代金券副标题（选填）" />
      </el-form-item>
      <el-form-item label="面值(元)" prop="actualValue">
         <el-input-number v-model="displayActualValue" :precision="2" :step="1" :min="0.01" @change="handleActualValueChange" placeholder="请输入面值" style="width: 100%;"/>
      </el-form-item>
       <el-form-item label="售价(元)" prop="payValue">
         <el-input-number v-model="displayPayValue" :precision="2" :step="1" :min="0" @change="handlePayValueChange" placeholder="请输入售价" style="width: 100%;"/>
      </el-form-item>
      <el-form-item label="有效期" prop="dateRange">
        <el-date-picker
          v-model="form.dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始日期时间"
          end-placeholder="结束日期时间"
          @change="handleDateRangeChange"
          style="width: 100%;"
        />
      </el-form-item>
      <el-form-item label="库存" prop="stock">
        <el-input-number v-model="form.stock" :min="0" :step="10" placeholder="请输入库存数量" style="width: 100%;"/>
      </el-form-item>
       <el-form-item label="使用规则" prop="rules">
        <el-input v-model="form.rules" type="textarea" :rows="3" placeholder="请输入详细的使用规则（选填）" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="submitForm">{{ isEditMode ? '保存修改' : '立即创建' }}</el-button>
        <el-button @click="goBack">取消</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style lang="less" scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-card {
  min-height: 600px;
}

// 可以根据需要添加更多样式
</style>