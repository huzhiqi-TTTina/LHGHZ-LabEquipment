<template>
  <div class="repair-page">
    <el-card class="search-form">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="关键字">
          <el-input v-model="queryForm.keyword" placeholder="报修编号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="primary" :icon="Plus" @click="showAddDialog">提交报修</el-button>
          <el-button v-if="isAdminOrTeacher" type="success" :icon="User" @click="showMyTasksDialog">我的任务</el-button>
          <el-button v-if="!isAdminOrTeacher" type="success" :icon="User" @click="showMyTasksDialog">我的任务</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="repairNo" label="报修编号" width="180" />
        <el-table-column label="设备" width="200">
          <template #default="{ row }">
            {{ getEquipmentName(row.equipmentId) }}
          </template>
        </el-table-column>

        <el-table-column prop="faultDescription" label="故障描述" min-width="200" />
        <el-table-column prop="reportTime" label="报修时间" width="180" />
        <el-table-column label="处理人" width="120">
          <template #default="{ row }">
            {{ getHandlerName(row.handlerId) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)">{{ getPriorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
            <el-button v-if="isAdminOrTeacher && row.status === 'PENDING'" type="success" link size="small" @click="showAssignDialog(row)">分配</el-button>
            <el-button v-if="isAdminOrTeacher && row.status === 'PENDING'" type="primary" link size="small" @click="handleStart(row)">开始处理</el-button>
            <el-button v-if="canComplete(row)" type="success" link size="small" @click="showCompleteDialog(row)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryForm.current"
          v-model:page-size="queryForm.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <!-- 提交报修对话框 -->
    <el-dialog v-model="dialogVisible" title="提交报修" width="600px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="选择设备" prop="equipmentId">
          <el-select v-model="formData.equipmentId" placeholder="请选择设备" style="width: 100%" filterable>
            <el-option
              v-for="item in equipmentList"
              :key="item.id"
              :label="`${item.equipmentName} (${item.equipmentNo})`"
              :value="item.id"
              :disabled="item.status !== 'NORMAL'"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="故障描述" prop="faultDescription">
          <el-input v-model="formData.faultDescription" type="textarea" :rows="4" placeholder="请描述故障现象" />
        </el-form-item>
        <el-form-item label="故障图片">
          <div class="fault-image-upload">
            <!-- 已上传的图片列表 -->
            <div class="uploaded-images" v-if="uploadedImages.length > 0">
              <div v-for="(url, index) in uploadedImages" :key="index" class="image-item">
                <el-image
                  :src="getImageUrl(url)"
                  fit="cover"
                  style="width: 80px; height: 80px; border-radius: 4px;"
                />
                <div class="image-mask">
                  <el-icon @click="removeImage(index)"><delete /></el-icon>
                </div>
              </div>
            </div>
            <!-- 上传按钮 -->
            <el-upload
              class="fault-uploader"
              :show-file-list="false"
              :before-upload="beforeImageUpload"
              :http-request="handleImageUpload"
              accept="image/*"
            >
              <div class="upload-placeholder">
                <el-icon><upload-filled /></el-icon>
                <span>上传图片</span>
              </div>
            </el-upload>
          </div>
          <div class="upload-tip">支持多张图片，最多 5 张，每张最大 10MB</div>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="formData.priority">
            <el-radio label="LOW">低</el-radio>
            <el-radio label="NORMAL">中</el-radio>
            <el-radio label="HIGH">高</el-radio>
            <el-radio label="URGENT">紧急</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 分配任务对话框 -->
    <el-dialog v-model="assignDialogVisible" title="分配报修任务" width="400px">
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

    <!-- 完成报修对话框 -->
    <el-dialog v-model="completeDialogVisible" title="完成报修" width="600px">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="维修描述">
          <el-input v-model="completeForm.repairDescription" type="textarea" :rows="3" placeholder="请描述维修情况" />
        </el-form-item>
        <el-form-item label="使用配件">
          <el-input v-model="completeForm.partsUsed" placeholder="请输入使用的配件" />
        </el-form-item>
        <el-form-item label="维修费用">
          <el-input-number v-model="completeForm.repairCost" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleComplete">确定</el-button>
      </template>
    </el-dialog>

    <!-- 我的任务对话框 -->
    <el-dialog v-model="myTasksDialogVisible" title="分配给我的任务" width="900px">
      <el-table :data="myTasksData" v-loading="myTasksLoading" border stripe max-height="400">
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="repairNo" label="报修编号" width="160" />
        <el-table-column label="设备" width="180">
          <template #default="{ row }">
            {{ getEquipmentName(row.equipmentId) }}
          </template>
        </el-table-column>
        <el-table-column prop="faultDescription" label="故障描述" min-width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PROCESSING'" type="success" link size="small" @click="showCompleteDialog(row)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { repairApi, equipmentApi, userApi, fileApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, User, Picture as IconPicture, UploadFilled, Delete } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const assignDialogVisible = ref(false)
const completeDialogVisible = ref(false)
const myTasksDialogVisible = ref(false)
const currentRepair = ref(null)
const formRef = ref(null)
const equipmentList = ref([])
const teacherList = ref([])
const myTasksData = ref([])
const myTasksLoading = ref(false)

// 是否是管理员或老师
const isAdminOrTeacher = computed(() => userStore.isAdmin || userStore.role === 'TEACHER')

const queryForm = reactive({
  current: 1,
  size: 10,
  keyword: ''
})

const formData = reactive({
  equipmentId: null,
  faultDescription: '',
  faultImageUrls: '',  // 故障图片URLs（逗号分隔）
  priority: 'NORMAL'
})

const assignForm = reactive({
  assigneeId: null
})

const completeForm = reactive({
  repairDescription: '',
  partsUsed: '',
  repairCost: 0
})

const rules = {
  equipmentId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  faultDescription: [{ required: true, message: '请描述故障现象', trigger: 'blur' }]
}

// 判断是否可以完成任务
const canComplete = (row) => {
  if (row.status !== 'PROCESSING') return false
  // 管理员和老师可以完成任何处理中的任务
  if (isAdminOrTeacher.value) return true
  // 学生只能完成分配给自己的任务
  return row.handlerId === userStore.userId
}

const getRepairList = async () => {
  loading.value = true
  try {
    const res = await repairApi.getList(queryForm)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取报修列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.current = 1
  getRepairList()
}

const handleReset = () => {
  queryForm.keyword = ''
  handleSearch()
}

const getStatusType = (status) => {
  const map = { PENDING: 'warning', PROCESSING: 'primary', COMPLETED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { PENDING: '待处理', PROCESSING: '处理中', COMPLETED: '已完成', REJECTED: '已驳回' }
  return map[status] || status
}

const getPriorityType = (priority) => {
  const map = { LOW: 'info', NORMAL: 'warning', HIGH: 'danger', URGENT: 'danger' }
  return map[priority] || 'info'
}

const getPriorityText = (priority) => {
  const map = { LOW: '低', NORMAL: '中', HIGH: '高', URGENT: '紧急' }
  return map[priority] || priority
}

// 获取设备名称
const getEquipmentName = (equipmentId) => {
  const equipment = equipmentList.value.find(e => e.id === equipmentId)
  return equipment ? `${equipment.equipmentName} (${equipment.equipmentNo})` : `设备ID: ${equipmentId}`
}

// 获取处理人名称
const getHandlerName = (handlerId) => {
  if (!handlerId) return '-'
  // 如果是当前用户，显示"我"
  if (handlerId === userStore.userId) {
    return '我'
  }
  // 管理员和老师可以从老师列表中查找
  const teacher = teacherList.value.find(t => t.id === handlerId)
  return teacher ? teacher.realName : '未知'
}

const showAddDialog = () => {
  Object.assign(formData, { equipmentId: null, faultDescription: '', faultImageUrls: '', priority: 'NORMAL' })
  dialogVisible.value = true
}

const showDetail = (row) => {
  const equipment = equipmentList.value.find(e => e.id === row.equipmentId)
  const equipmentName = equipment ? `${equipment.equipmentName} (${equipment.equipmentNo})` : `设备ID: ${row.equipmentId}`
  const handlerName = getHandlerName(row.handlerId)
  
  // 生成图片HTML
  let imagesHtml = ''
  if (row.faultImageUrls) {
    const urls = row.faultImageUrls.split(',')
    if (urls.length > 0) {
      imagesHtml = '<div style="margin: 15px 0; text-align: center;">'
      urls.forEach(url => {
        const fullUrl = getImageUrl(url.trim())
        imagesHtml += `<img src="${fullUrl}" style="max-width: 200px; max-height: 150px; margin: 5px; border-radius: 4px; border: 1px solid #ddd;" />`
      })
      imagesHtml += '</div>'
    }
  }
  
  ElMessageBox.alert(`
    <div style="line-height: 2;">
      ${imagesHtml}
      <p><strong>报修编号：</strong>${row.repairNo}</p>
      <p><strong>设备：</strong>${equipmentName}</p>
      <p><strong>故障描述：</strong>${row.faultDescription}</p>
      <p><strong>报修时间：</strong>${row.reportTime}</p>
      <p><strong>处理人：</strong>${handlerName}</p>
      <p><strong>状态：</strong>${getStatusText(row.status)}</p>
      <p><strong>优先级：</strong>${getPriorityText(row.priority)}</p>
    </div>
  `, '报修详情', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '关闭'
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await repairApi.submit(formData)
        ElMessage.success('报修提交成功')
        dialogVisible.value = false
        getRepairList()
      } catch (error) {
        ElMessage.error('提交失败')
      }
    }
  })
}

const handleStart = async (row) => {
  try {
    await repairApi.handle(row.id)
    ElMessage.success('已开始处理')
    getRepairList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 显示分配对话框
const showAssignDialog = (row) => {
  currentRepair.value = row
  assignForm.assigneeId = row.handlerId || null
  assignDialogVisible.value = true
}

// 分配任务
const handleAssign = async () => {
  if (!assignForm.assigneeId) {
    ElMessage.warning('请选择老师')
    return
  }
  try {
    await repairApi.assign(currentRepair.value.id, assignForm.assigneeId)
    ElMessage.success('任务分配成功')
    assignDialogVisible.value = false
    getRepairList()
  } catch (error) {
    ElMessage.error('分配失败')
  }
}

// 显示我的任务对话框
const showMyTasksDialog = async () => {
  myTasksDialogVisible.value = true
  myTasksLoading.value = true
  try {
    const res = await repairApi.getMyTasks({ current: 1, size: 100 })
    myTasksData.value = res.data.records || []
  } catch (error) {
    ElMessage.error('获取任务列表失败')
  } finally {
    myTasksLoading.value = false
  }
}

const showCompleteDialog = (row) => {
  currentRepair.value = row
  Object.assign(completeForm, { repairDescription: '', partsUsed: '', repairCost: 0 })
  completeDialogVisible.value = true
}

const handleComplete = async () => {
  try {
    await repairApi.complete(currentRepair.value.id, completeForm)
    ElMessage.success('报修已完成')
    completeDialogVisible.value = false
    getRepairList()
    // 如果我的任务对话框打开，也刷新它
    if (myTasksDialogVisible.value) {
      showMyTasksDialog()
    }
  } catch (error) {
    ElMessage.error('操作失败')
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

// 获取老师列表
const getTeacherList = async () => {
  try {
    const res = await userApi.getTeacherList()
    teacherList.value = res.data || []
  } catch (error) {
    ElMessage.error('获取老师列表失败')
  }
}

onMounted(() => {
  getRepairList()
  getEquipmentList()
  // 只有管理员和老师需要获取老师列表（用于分配任务）
  if (isAdminOrTeacher.value) {
    getTeacherList()
  }
})

// ==================== 图片上传相关 ====================

const uploadedImages = ref([])

// 处理图片URL，添加 /api 前缀
const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return url.startsWith('/api') ? url : '/api' + url
}

// 获取第一张图片URL（用于表格缩略图）
const getFirstImageUrl = (faultImageUrls) => {
  if (!faultImageUrls) return ''
  const urls = faultImageUrls.split(',')
  return getImageUrl(urls[0])
}

// 获取图片列表（用于预览）
const getImageList = (faultImageUrls) => {
  if (!faultImageUrls) return []
  const urls = faultImageUrls.split(',').map(url => getImageUrl(url.trim()))
  return urls
}

// 上传前校验
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10
  const hasRoom = uploadedImages.value.length < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB！')
    return false
  }
  if (!hasRoom) {
    ElMessage.error('最多只能上传 5 张图片！')
    return false
  }
  return true
}

// 处理图片上传
const handleImageUpload = async (options) => {
  const { file, onSuccess, onError } = options
  try {
    const res = await fileApi.upload(file)
    if (res.code === 200 || res.code === 0) {
      const imageUrl = res.data || res.url
      uploadedImages.value.push(imageUrl)
      // 更新表单数据
      formData.faultImageUrls = uploadedImages.value.join(',')
      ElMessage.success('图片上传成功')
      onSuccess(res)
    } else {
      ElMessage.error(res.message || '图片上传失败')
      onError(new Error(res.message))
    }
  } catch (error) {
    ElMessage.error('图片上传失败')
    onError(error)
  }
}

// 删除图片
const removeImage = (index) => {
  uploadedImages.value.splice(index, 1)
  formData.faultImageUrls = uploadedImages.value.join(',')
}
</script>

<style lang="scss" scoped>
.repair-page {
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  // 故障图片上传样式
  .fault-image-upload {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;

    .uploaded-images {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;

      .image-item {
        position: relative;
        width: 80px;
        height: 80px;
        border-radius: 4px;
        overflow: hidden;

        .image-mask {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          background: rgba(0, 0, 0, 0.5);
          opacity: 0;
          transition: opacity 0.3s;
          cursor: pointer;

          .el-icon {
            font-size: 20px;
            color: #fff;
          }

          &:hover {
            opacity: 1;
          }
        }
      }
    }

    .fault-uploader {
      .upload-placeholder {
        width: 80px;
        height: 80px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        border: 1px dashed #d9d9d9;
        border-radius: 4px;
        background: #fafafa;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          border-color: #409eff;
          background: #ecf5ff;
        }

        .el-icon {
          font-size: 20px;
          color: #8c939d;
          margin-bottom: 4px;
        }

        span {
          font-size: 12px;
          color: #8c939d;
        }
      }
    }
  }

  .upload-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #999;
  }
}
</style>
