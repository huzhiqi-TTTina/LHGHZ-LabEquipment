<template>
  <div class="reservation-page">
    <!-- 搜索表单 -->
    <el-card class="search-form">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="关键字">
          <el-input v-model="queryForm.keyword" placeholder="设备名称/id" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="60" :index="reservationIndexRange" />
        <el-table-column prop="reservationNo" label="预约编号" width="150" />
        <el-table-column prop="equipmentName" label="设备名称" width="100" />
        <el-table-column prop="equipmentId" label="设备ID" width="70" />
        <el-table-column prop="userName" label="申请者名字" width="130"></el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="purpose" label="使用目的" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
            <el-button v-if="userStore.isAdmin && row.status === 'PENDING'" type="primary" link size="small" @click="showApproveDialog(row)">审核</el-button>
            <el-button v-if="row.status === 'PENDING' && row.userId === userStore.userId" type="danger" link size="small" @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryForm.current"
          v-model:page-size="queryForm.size"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="getReservationList"
        />
      </div>
    </el-card>
    <!-- 审核对话框 -->
    <el-dialog v-model="approveDialogVisible" title="审核预约" width="500px">
      <el-form :model="approveForm" label-width="100px">
        <el-form-item label="预约编号">
          <span>{{ currentReservation?.reservationNo }}</span>
        </el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="approveForm.status">
            <el-radio label="APPROVED">批准</el-radio>
            <el-radio label="REJECTED">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核备注">
          <el-input v-model="approveForm.remark" type="textarea" placeholder="请输入审核备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleApprove">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { reservationApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const approveDialogVisible = ref(false)
const currentReservation = ref(null)

const queryForm = reactive({
  current: 1,
  size: 10,
  keyword: ''
})

const approveForm = reactive({
  status: 'APPROVED',
  remark: ''
})

const getReservationList = async () => {
  loading.value = true
  try {
    const res = await reservationApi.getList(queryForm)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取预约列表失败')
  } finally {
    loading.value = false
  }
}

//自增序号
const reservationIndexRange = (index) => (queryForm.current - 1) * queryForm.size + index + 1

const handleSearch = () => {
  queryForm.current = 1
  getReservationList()
}

const handleReset = () => {
  queryForm.keyword = ''
  handleSearch()
}

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

const showDetail = (row) => {
  ElMessageBox.alert(`
    <div style="line-height: 2;">
      <p><strong>预约编号：</strong>${row.reservationNo}</p>
      <p><strong>设备ID：</strong>${row.equipmentId}</p>
      <p><strong>开始时间：</strong>${row.startTime}</p>
      <p><strong>结束时间：</strong>${row.endTime}</p>
      <p><strong>使用目的：</strong>${row.purpose || '-'}</p>
      <p><strong>状态：</strong>${getStatusText(row.status)}</p>
    </div>
  `, '预约详情', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '关闭'
  })
}

const showApproveDialog = (row) => {
  currentReservation.value = row
  approveForm.status = 'APPROVED'
  approveForm.remark = ''
  approveDialogVisible.value = true
}

const handleApprove = async () => {
  try {
    await reservationApi.approve(currentReservation.value.id, approveForm)
    ElMessage.success('审核成功')
    approveDialogVisible.value = false
    getReservationList()
  } catch (error) {
    ElMessage.error('审核失败')
  }
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
    getReservationList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

getReservationList()
</script>

<style lang="scss" scoped>
.reservation-page {
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
