<template>
  <div class="maintenance-page">
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="维护计划" name="plans">
          <template #label>
            <el-icon><Calendar /></el-icon>
            维护计划
          </template>
          <div v-if="isAdminOrTeacher" style="margin-bottom: 20px">
            <el-button type="primary" :icon="Plus" @click="showAddPlanDialog">新增计划</el-button>
            <el-button type="success" :icon="User" @click="activeTab = 'myPlans'">我的任务</el-button>
          </div>
          <el-table :data="plans" v-loading="loading" border stripe>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="planName" label="计划名称" width="150" />
            <el-table-column prop="maintenanceType" label="维护类型" width="120">
              <template #default="{ row }">{{ getTypeText(row.maintenanceType) }}</template>
            </el-table-column>
            <el-table-column prop="maintenanceCycle" label="周期(天)" width="100" />
            <el-table-column prop="nextMaintenanceDate" label="下次维护日期" width="150" />
            <el-table-column label="负责人" width="120">
              <template #default="{ row }">
                {{ getResponsibleName(row.responsibleUserId) }}
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" min-width="150" />
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <el-button v-if="isAdminOrTeacher" type="primary" link size="small" @click="showEditPlanDialog(row)">编辑</el-button>
                <el-button v-if="isAdminOrTeacher" type="warning" link size="small" @click="showAssignDialog(row)">
                  {{ row.responsibleUserId ? '重新分配' : '分配' }}
                </el-button>
                <el-tooltip v-if="canCompleteMaintenance(row)" :content="getCanCompleteStatusText(row).text" placement="top">
                  <el-button
                    type="success"
                    link
                    size="small"
                    :disabled="isAdminOrTeacher.value ? false : !getCanCompleteStatusText(row).canComplete"
                    @click="showCompleteDialog(row)"
                  >
                    完成
                  </el-button>
                </el-tooltip>
                <el-button v-if="isAdminOrTeacher" type="danger" link size="small" @click="handleDeletePlan(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="分配给我的任务" name="myPlans">
          <template #label>
            <el-icon><User /></el-icon>
            分配给我的任务
          </template>
          <el-table :data="myPlans" v-loading="myPlansLoading" border stripe>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="planName" label="计划名称" width="150" />
            <el-table-column prop="maintenanceType" label="维护类型" width="120">
              <template #default="{ row }">{{ getTypeText(row.maintenanceType) }}</template>
            </el-table-column>
            <el-table-column prop="maintenanceCycle" label="周期(天)" width="100" />
            <el-table-column prop="nextMaintenanceDate" label="下次维护日期" width="150" />
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getCanCompleteStatusText(row).canComplete ? 'success' : 'warning'">
                  {{ getCanCompleteStatusText(row).text }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" min-width="150" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-tooltip v-if="row.responsibleUserId" :content="getCanCompleteStatusText(row).text" placement="top">
                  <el-button
                    type="success"
                    link
                    size="small"
                    :disabled="!getCanCompleteStatusText(row).canComplete"
                    @click="showCompleteDialog(row)"
                  >
                    完成
                  </el-button>
                </el-tooltip>
                <el-tag v-else type="info">未分配</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="维护记录" name="records">
          <template #label>
            <el-icon><Document /></el-icon>
            维护记录
          </template>
          <div v-if="isAdminOrTeacher" style="margin-bottom: 20px">
            <el-button type="primary" :icon="Plus" @click="showAddRecordDialog">添加记录</el-button>
          </div>
          <el-table :data="records" v-loading="loading" border stripe>
            <el-table-column type="index" label="序号" width="60" />
            <el-table-column prop="planName" label="计划名称" width="150" />
            <el-table-column prop="equipmentName" label="设备名称" width="150" />
            <el-table-column prop="maintenanceDate" label="维护时间" width="160" />
            <el-table-column prop="maintenanceType" label="维护类型" width="100">
              <template #default="{ row }">{{ getTypeText(row.maintenanceType) }}</template>
            </el-table-column>
            <el-table-column prop="maintenanceContent" label="维护内容" min-width="200" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 新增/编辑计划对话框 -->
    <el-dialog v-model="planDialogVisible" :title="planDialogTitle" width="600px">
      <el-form ref="planFormRef" :model="planForm" label-width="100px">
        <el-form-item label="计划名称" required>
          <el-input v-model="planForm.planName" placeholder="请输入计划名称" />
        </el-form-item>
        <el-form-item label="维护类型" required>
          <el-select v-model="planForm.maintenanceType" style="width: 100%">
            <el-option label="日常" value="DAILY" />
            <el-option label="每周" value="WEEKLY" />
            <el-option label="每月" value="MONTHLY" />
            <el-option label="每季度" value="QUARTERLY" />
            <el-option label="每年" value="YEARLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="维护周期" required>
          <el-input-number v-model="planForm.maintenanceCycle" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="下次维护日期" required>
          <el-date-picker v-model="planForm.nextMaintenanceDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="维护说明">
          <el-input v-model="planForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePlan">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配任务对话框 -->
    <el-dialog v-model="assignDialogVisible" title="分配维护任务" width="400px">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="选择老师">
          <el-select v-model="assignForm.assigneeId" placeholder="请选择老师" style="width: 100%" filterable>
            <el-option
              v-for="teacher in teacherList"
              :key="teacher.id"
              :label="`${teacher.realName} (${teacher.username})`"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssign">确定</el-button>
      </template>
    </el-dialog>

    <!-- 完成维护对话框 -->
    <el-dialog v-model="completeDialogVisible" title="完成维护" width="600px">
      <div v-if="currentPlan" style="margin-bottom: 20px; padding: 10px; background: #f5f7fa; border-radius: 4px;">
        <div><strong>计划名称：</strong>{{ currentPlan.planName }}</div>
        <div><strong>维护类型：</strong>{{ getTypeText(currentPlan.maintenanceType) }}</div>
        <div v-if="currentPlan.lastCompletedDate"><strong>上次完成：</strong>{{ currentPlan.lastCompletedDate }}</div>
        <div><strong>维护周期：</strong>{{ currentPlan.maintenanceCycle }} 天</div>
      </div>
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="选择设备" required>
          <el-select v-model="completeForm.equipmentId" placeholder="请选择设备" style="width: 100%" filterable>
            <el-option
              v-for="item in equipmentList"
              :key="item.id"
              :label="`${item.equipmentName} (${item.equipmentNo})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="维护内容">
          <el-input v-model="completeForm.content" type="textarea" :rows="3" placeholder="请描述维护内容" />
        </el-form-item>
        <el-form-item label="维护结果" required>
          <el-radio-group v-model="completeForm.result">
            <el-radio label="SUCCESS">成功</el-radio>
            <el-radio label="FAILED">失败</el-radio>
            <el-radio label="PARTIAL">部分完成</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false" :disabled="isCompleting">取消</el-button>
        <el-button type="primary" @click="handleComplete" :loading="isCompleting" :disabled="isCompleting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 添加记录对话框 -->
    <el-dialog v-model="recordDialogVisible" title="添加维护记录" width="600px">
      <el-form :model="recordForm" label-width="100px">
        <el-form-item label="维护计划">
          <el-select v-model="recordForm.planId" placeholder="请选择维护计划（可选）" style="width: 100%" clearable filterable>
            <el-option
              v-for="plan in plans"
              :key="plan.id"
              :label="plan.planName"
              :value="plan.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择设备" required>
          <el-select v-model="recordForm.equipmentId" placeholder="请选择设备" style="width: 100%" filterable>
            <el-option
              v-for="item in equipmentList"
              :key="item.id"
              :label="`${item.equipmentName} (${item.equipmentNo})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="维护类型" required>
          <el-select v-model="recordForm.maintenanceType" style="width: 100%">
            <el-option label="日常" value="DAILY" />
            <el-option label="每周" value="WEEKLY" />
            <el-option label="每月" value="MONTHLY" />
            <el-option label="每季度" value="QUARTERLY" />
            <el-option label="每年" value="YEARLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="维护内容">
          <el-input v-model="recordForm.maintenanceContent" type="textarea" :rows="3" placeholder="请描述维护内容" />
        </el-form-item>
        <el-form-item label="维护结果" required>
          <el-radio-group v-model="recordForm.maintenanceResult">
            <el-radio label="SUCCESS">成功</el-radio>
            <el-radio label="FAILED">失败</el-radio>
            <el-radio label="PARTIAL">部分完成</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="recordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRecord">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { maintenanceApi, userApi, equipmentApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Calendar, Document, User } from '@element-plus/icons-vue'

const userStore = useUserStore()
const activeTab = ref('plans')
const loading = ref(false)
const myPlansLoading = ref(false)
const plans = ref([])
const records = ref([])
const myPlans = ref([])
const teacherList = ref([])
const equipmentList = ref([])
const planDialogVisible = ref(false)
const recordDialogVisible = ref(false)
const assignDialogVisible = ref(false)
const completeDialogVisible = ref(false)
const planDialogTitle = ref('新增维护计划')
const planFormRef = ref(null)
const currentPlan = ref(null)

// 是否是管理员或老师
const isAdminOrTeacher = computed(() => userStore.isAdmin || userStore.role === 'TEACHER')

const planForm = reactive({
  id: null,
  planName: '',
  maintenanceType: 'MONTHLY',
  maintenanceCycle: 30,
  nextMaintenanceDate: '',
  description: ''
})

const assignForm = reactive({
  assigneeId: null
})

const completeForm = reactive({
  equipmentId: null,
  content: '',
  result: 'SUCCESS'
})

const recordForm = reactive({
  planId: null,
  equipmentId: null,
  maintenanceType: 'DAILY',
  maintenanceContent: '',
  maintenanceResult: 'SUCCESS'
})

// 判断是否可以完成维护
const canCompleteMaintenance = (row) => {
  if (!row.responsibleUserId) return false
  // 管理员和老师可以完成任何有负责人的任务
  if (isAdminOrTeacher.value) return true
  // 学生只能完成分配给自己的任务
  if (row.responsibleUserId !== userStore.userId) return false
  // 检查是否到了下次维护日期
  return canCompleteByDate(row)
}

// 判断是否可以按日期完成维护（检查维护周期）
const canCompleteByDate = (row) => {
  if (!row.nextMaintenanceDate) return true
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const nextDate = new Date(row.nextMaintenanceDate)
  return today >= nextDate
}

// 获取距离下次可完成的描述
const getCanCompleteStatusText = (row) => {
  if (!row.responsibleUserId) return { canComplete: false, text: '未分配负责人' }
  if (!isAdminOrTeacher.value && row.responsibleUserId !== userStore.userId) {
    return { canComplete: false, text: '非分配给我的任务' }
  }
  if (!row.nextMaintenanceDate) return { canComplete: true, text: '可以完成' }

  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const nextDate = new Date(row.nextMaintenanceDate)

  if (today >= nextDate) {
    return { canComplete: true, text: '可以完成' }
  }

  const diffTime = nextDate - today
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return { canComplete: false, text: `还需等待 ${diffDays} 天` }
}

const getPlans = async () => {
  loading.value = true
  try {
    const res = await maintenanceApi.getPlans({ current: 1, size: 100 })
    plans.value = res.data.records || []
  } catch (error) {
    ElMessage.error('获取维护计划失败')
  } finally {
    loading.value = false
  }
}

const getMyPlans = async () => {
  myPlansLoading.value = true
  try {
    const res = await maintenanceApi.getMyPlans({ current: 1, size: 100 })
    myPlans.value = res.data.records || []
  } catch (error) {
    ElMessage.error('获取我的任务失败')
  } finally {
    myPlansLoading.value = false
  }
}

const getRecords = async () => {
  loading.value = true
  try {
    const res = await maintenanceApi.getRecords({ current: 1, size: 100 })
    records.value = res.data.records || []
  } catch (error) {
    ElMessage.error('获取维护记录失败')
  } finally {
    loading.value = false
  }
}

const getTypeText = (type) => {
  const map = { DAILY: '日常', WEEKLY: '每周', MONTHLY: '每月', QUARTERLY: '每季度', YEARLY: '每年' }
  return map[type] || type
}

const getResultText = (result) => {
  const map = { SUCCESS: '成功', FAILED: '失败', PARTIAL: '部分完成' }
  return map[result] || result
}

// 获取负责人名称
const getResponsibleName = (userId) => {
  if (!userId) return '-'
  // 如果是当前用户，显示"我"
  if (userId === userStore.userId) {
    return '我'
  }
  // 管理员和老师可以从老师列表中查找
  const teacher = teacherList.value.find(t => t.id === userId)
  return teacher ? teacher.realName : '未知'
}

const showAddPlanDialog = () => {
  planDialogTitle.value = '新增维护计划'
  Object.assign(planForm, { id: null, planName: '', maintenanceType: 'MONTHLY', maintenanceCycle: 30, nextMaintenanceDate: '', description: '' })
  planDialogVisible.value = true
}

const showEditPlanDialog = (row) => {
  planDialogTitle.value = '编辑维护计划'
  Object.assign(planForm, row)
  planDialogVisible.value = true
}

const handleSavePlan = async () => {
  try {
    if (planForm.id) {
      await maintenanceApi.updatePlan(planForm.id, planForm)
      ElMessage.success('更新成功')
    } else {
      await maintenanceApi.createPlan(planForm)
      ElMessage.success('创建成功')
    }
    planDialogVisible.value = false
    getPlans()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDeletePlan = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该计划吗？', '提示', { type: 'warning' })
    await maintenanceApi.deletePlan(row.id)
    ElMessage.success('删除成功')
    getPlans()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

// 显示分配对话框
const showAssignDialog = (row) => {
  currentPlan.value = row
  assignForm.assigneeId = row.responsibleUserId || null
  assignDialogVisible.value = true
}

// 分配任务
const handleAssign = async () => {
  if (!assignForm.assigneeId) {
    ElMessage.warning('请选择老师')
    return
  }
  try {
    await maintenanceApi.assignPlan(currentPlan.value.id, assignForm.assigneeId)
    ElMessage.success('任务分配成功')
    assignDialogVisible.value = false
    getPlans()
  } catch (error) {
    ElMessage.error('分配失败')
  }
}

// 显示完成对话框
const showCompleteDialog = (row) => {
  currentPlan.value = row
  completeForm.equipmentId = null
  completeForm.content = ''
  completeForm.result = 'SUCCESS'
  completeDialogVisible.value = true
}

// 完成维护
const isCompleting = ref(false)
const handleComplete = async () => {
  if (isCompleting.value) {
    return
  }
  if (!completeForm.equipmentId) {
    ElMessage.warning('请选择设备')
    return
  }
  isCompleting.value = true
  try {
    await maintenanceApi.complete(currentPlan.value.id, {
      equipmentId: completeForm.equipmentId,
      content: completeForm.content,
      result: completeForm.result
    })
    ElMessage.success('维护已完成')
    completeDialogVisible.value = false

    // 等待一小段时间确保后端事务提交
    await new Promise(resolve => setTimeout(resolve, 200))

    // 刷新所有列表数据
    await Promise.all([getPlans(), getMyPlans(), getRecords()])
  } catch (error) {
    console.error('完成维护失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '未知错误'
    ElMessage.error('操作失败: ' + errorMsg)
  } finally {
    isCompleting.value = false
  }
}

const showAddRecordDialog = () => {
  Object.assign(recordForm, { planId: null, equipmentId: null, maintenanceType: 'DAILY', maintenanceContent: '', maintenanceResult: 'SUCCESS' })
  recordDialogVisible.value = true
}

const handleSaveRecord = async () => {
  try {
    await maintenanceApi.addRecord(recordForm)
    ElMessage.success('添加成功')
    recordDialogVisible.value = false
    getRecords()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

// 获取老师列表
const getTeacherList = async () => {
  try {
    const res = await userApi.getTeacherList()
    teacherList.value = res.data || []
  } catch (error) {
    ElMessage.error('获取老师列表失败')
  }
}

// 获取设备列表
const getEquipmentList = async () => {
  try {
    const res = await equipmentApi.getList({ current: 1, size: 1000 })
    equipmentList.value = res.data.records || []
  } catch (error) {
    ElMessage.error('获取设备列表失败')
  }
}

onMounted(() => {
  getPlans()
  getRecords()
  // 只有管理员和老师需要获取老师列表（用于分配任务）
  if (isAdminOrTeacher.value) {
    getTeacherList()
  }
  getEquipmentList()
  // 如果是学生，获取分配给自己的任务
  if (!isAdminOrTeacher.value) {
    getMyPlans()
  }
})
</script>

<style lang="scss" scoped>
.maintenance-page {
  :deep(.el-tabs__item) {
    display: flex;
    align-items: center;
    gap: 5px;
  }
}
</style>
