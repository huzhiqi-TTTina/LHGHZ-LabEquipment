<template>
  <div class="my-reservation-page">
    <el-card class="search-form">
      <el-form :inline="true">
        <el-form-item>
          <el-button type="primary" :icon="Plus" @click="showAddDialog">新增预约</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="60" :index="myReservationIndexRange" />
        <el-table-column prop="reservationNo" label="预约编号" width="180" />
        <el-table-column prop="equipmentId" label="设备ID" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="danger" link size="small" @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryForm.current"
          v-model:page-size="queryForm.size"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="getMyList"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增预约" width="600px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="选择设备" prop="equipmentId">
          <el-select v-model="formData.equipmentId" placeholder="请选择设备" style="width: 100%">
            <el-option
              v-for="equipment in equipmentList"
              :key="equipment.id"
              :label="`${equipment.equipmentName} (${equipment.equipmentNo})`"
              :value="equipment.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="formData.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="formData.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="使用目的">
          <el-input v-model="formData.purpose" type="textarea" :rows="3" placeholder="请输入使用目的" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { reservationApi, equipmentApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref(null)
const equipmentList = ref([])

const queryForm = reactive({
  current: 1,
  size: 10
})

const formData = reactive({
  equipmentId: null,
  startTime: '',
  endTime: '',
  purpose: ''
})

const rules = {
  equipmentId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const getMyList = async () => {
  loading.value = true
  try {
    const res = await reservationApi.getMyList(queryForm)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取我的预约失败')
  } finally {
    loading.value = false
  }
}

const getEquipmentList = async () => {
  try {
    const res = await equipmentApi.getList({})
    equipmentList.value = res.data.records || []
  } catch (error) {
    ElMessage.error('获取设备列表失败')
  }
}

//自增序号
const myReservationIndexRange = (index) => (queryForm.current - 1) * queryForm.size + index + 1

const getStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info',
    COMPLETED: 'success'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    PENDING: '待审核',
    APPROVED: '已批准',
    REJECTED: '已拒绝',
    CANCELLED: '已取消',
    COMPLETED: '已完成'
  }
  return map[status] || status
}

const showAddDialog = () => {
  Object.assign(formData, {
    equipmentId: null,
    startTime: '',
    endTime: '',
    purpose: ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await reservationApi.create(formData)
        ElMessage.success('预约创建成功')
        dialogVisible.value = false
        getMyList()
getEquipmentList()
      } catch (error) {
        ElMessage.error('创建失败')
      }
    }
  })
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await reservationApi.cancel(row.id)
    ElMessage.success('取消成功')
    getMyList()
getEquipmentList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

getMyList()
getEquipmentList()
</script>

<style lang="scss" scoped>
.my-reservation-page {
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
