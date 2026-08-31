<template>
  <div class="equipment-page">
    <!-- 搜索表单 -->
    <el-card class="search-form">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="关键字">
          <el-input v-model="queryForm.keyword" placeholder="设备名称/编号/品牌/型号/id" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button type="success" :icon="RefreshRight" @click="getEquipmentList">刷新状态</el-button>
          <el-button v-if="userStore.isAdmin" type="primary" :icon="Plus" @click="showAddDialog">新增设备</el-button>
          <el-button v-if="userStore.isAdmin" :icon="Download" @click="handleExport">导出</el-button>
          <el-button v-if="userStore.isAdmin" type="warning" :icon="RefreshRight" @click="handleRegenerateAllQrCodes">重新生成二维码</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <el-image
              v-if="row.imageUrl"
              :src="getImageUrl(row.imageUrl)"
              fit="cover"
              style="width: 50px; height: 50px; border-radius: 4px; cursor: pointer;"
              :preview-src-list="[getImageUrl(row.imageUrl)]"
              preview-teleported
            >
              <template #error>
                <div style="width: 50px; height: 50px; display: flex; align-items: center; justify-content: center; background: #f5f5f5; border-radius: 4px;">
                  <el-icon><icon-picture /></el-icon>
                </div>
              </template>
            </el-image>
            <div v-else style="width: 50px; height: 50px; display: flex; align-items: center; justify-content: center; background: #f5f5f5; border-radius: 4px; color: #999;">
              <el-icon><icon-picture /></el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="uniqueCode" label="唯一标识码" width="120">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.uniqueCode || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="设备id" width="120" />
        <el-table-column prop="equipmentNo" label="设备编号" width="150" />
        <el-table-column prop="equipmentName" label="设备名称" min-width="150" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="brand" label="品牌" width="100" />
        <el-table-column prop="model" label="型号" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="laboratoryName" label="所属实验室" width="150" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showQrCode(row)">二维码</el-button>
            <el-button type="primary" link size="small" @click="showDetail(row)">详情</el-button>
            <el-button v-if="userStore.isAdmin && row.status === 'NORMAL'" type="primary" link size="small" @click="showBorrowDialog(row)">借出</el-button>
            <el-button v-if="userStore.isAdmin && row.status === 'BORROWED'" type="primary" link size="small" @click="showReturnDialog(row)">归还</el-button>
            <el-button v-if="userStore.isAdmin" type="primary" link size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button v-if="userStore.isAdmin" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="设备编号" prop="equipmentNo">
          <el-input v-model="formData.equipmentNo" placeholder="请输入设备编号" />
        </el-form-item>
        <el-form-item label="设备名称" prop="equipmentName">
          <el-input v-model="formData.equipmentName" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option label="计算机设备" :value="1" />
            <el-option label="电子仪器" :value="2" />
            <el-option label="机械设备" :value="3" />
            <el-option label="化学设备" :value="4" />
            <el-option label="物理设备" :value="5" />
            <el-option label="生物设备" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属实验室">
          <el-select v-model="formData.laboratoryId" placeholder="请选择实验室" style="width: 100%">
            <el-option label="计算机实验室1" :value="1" />
            <el-option label="电子实验室1" :value="2" />
            <el-option label="机械实验室1" :value="3" />
            <el-option label="化学实验室1" :value="4" />
            <el-option label="物理实验室1" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="formData.brand" placeholder="请输入品牌" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="formData.model" placeholder="请输入型号" />
        </el-form-item>
        <el-form-item label="购买日期">
          <el-date-picker
            v-model="formData.purchaseDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="购买价格">
          <el-input-number v-model="formData.purchasePrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="formData.supplier" placeholder="请输入供应商" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="设备图片">
          <div class="image-upload-container">
            <el-upload
              class="equipment-image-uploader"
              :show-file-list="false"
              :before-upload="beforeImageUpload"
              :http-request="handleImageUpload"
              accept="image/*"
            >
              <div v-if="formData.imageUrl" class="image-preview">
                <el-image
                  :src="getImageUrl(formData.imageUrl)"
                  fit="cover"
                  style="width: 120px; height: 120px; border-radius: 6px;"
                />
                <div class="image-actions">
                  <el-button type="danger" size="small" :icon="Delete" circle @click.stop="handleRemoveImage" />
                </div>
              </div>
              <div v-else class="upload-placeholder">
                <el-icon><upload-filled /></el-icon>
                <span>点击上传图片</span>
              </div>
            </el-upload>
            <div class="upload-tip">支持 JPG、PNG 格式，最大 10MB</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 借出对话框 -->
    <el-dialog v-model="borrowDialogVisible" title="设备借出" width="500px">
      <el-form :model="borrowForm" label-width="100px">
        <el-form-item label="设备名称">
          <span>{{ currentEquipment?.equipmentName }}</span>
        </el-form-item>
        <el-form-item label="借用人ID">
          <el-input v-model="borrowForm.userId" placeholder="请输入借用人ID" />
        </el-form-item>
        <el-form-item label="借用用途">
          <el-input v-model="borrowForm.purpose" type="textarea" placeholder="请输入借用用途" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="borrowDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBorrow">确定</el-button>
      </template>
    </el-dialog>

    <!-- 归还对话框 -->
    <el-dialog v-model="returnDialogVisible" title="设备归还" width="500px">
      <el-form :model="returnForm" label-width="100px">
        <el-form-item label="设备名称">
          <span>{{ currentEquipment?.equipmentName }}</span>
        </el-form-item>
        <el-form-item label="完好程度">
          <el-radio-group v-model="returnForm.condition">
            <el-radio label="GOOD">完好</el-radio>
            <el-radio label="DAMAGED">损坏</el-radio>
            <el-radio label="SERIOUS">严重损坏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReturn">确定</el-button>
      </template>
    </el-dialog>

    <!-- 二维码对话框 -->
    <el-dialog v-model="qrCodeDialogVisible" title="设备二维码" width="450px" align-center>
      <div v-if="currentEquipment" style="text-align: center;">
        <p style="margin-bottom: 10px; font-size: 18px;">
          <strong>{{ currentEquipment.equipmentName }}</strong>
        </p>
        <div style="display: flex; justify-content: center; gap: 20px; margin-bottom: 15px; font-size: 14px; color: #666;">
          <span>唯一码：{{ currentEquipment.uniqueCode || '-' }}</span>
          <span>编号：{{ currentEquipment.equipmentNo }}</span>
        </div>
        <div style="margin-bottom: 10px;">
          <el-tag :type="getStatusType(currentEquipment.status)">{{ currentEquipment.statusText }}</el-tag>
        </div>
        <div v-if="qrCodeImageUrl" style="padding: 20px; background: #f5f5f5; border-radius: 8px; display: inline-block;">
          <el-image
            :src="qrCodeImageUrl"
            fit="contain"
            style="width: 250px; height: 250px;"
            :preview-src-list="[qrCodeImageUrl]"
          >
            <template #error>
              <div class="image-error">
                <el-icon><icon-picture /></el-icon>
                <span>二维码加载失败</span>
              </div>
            </template>
          </el-image>
        </div>
        <div v-else style="padding: 20px; color: #999;">
          <el-icon style="font-size: 48px;"><icon-picture /></el-icon>
          <p>暂无二维码</p>
        </div>
        <div v-if="currentEquipment.qrCode" style="margin-top: 15px; padding: 10px; background: #f9f9f9; border-radius: 6px;">
          <p style="font-size: 12px; color: #666; margin-bottom: 5px;">扫码链接（H5页面）：</p>
          <p style="font-size: 12px; color: #333; word-break: break-all;">{{ currentEquipment.qrCode }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="qrCodeDialogVisible = false">关闭</el-button>
        <el-button v-if="currentEquipment?.qrCode" type="success" @click="openH5Page">打开H5页面</el-button>
        <el-button v-if="qrCodeImageUrl" type="primary" @click="downloadQrCode">下载二维码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { equipmentApi, fileApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, RefreshRight, Plus, Download, Picture as IconPicture, UploadFilled, Delete } from '@element-plus/icons-vue'

const userStore = useUserStore()

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增设备')
const borrowDialogVisible = ref(false)
const returnDialogVisible = ref(false)
const qrCodeDialogVisible = ref(false)
const currentEquipment = ref(null)
const qrCodeImageUrl = ref('')
const formRef = ref(null)

const queryForm = reactive({
  current: 1,
  size: 10,
  keyword: ''
})

const formData = reactive({
  id: null,
  equipmentNo: '',
  equipmentName: '',
  categoryId: null,
  laboratoryId: null,
  brand: '',
  model: '',
  purchaseDate: '',
  purchasePrice: null,
  supplier: '',
  description: '',
  imageUrl: ''  // 设备图片
})

const borrowForm = reactive({
  userId: null,
  purpose: ''
})

const returnForm = reactive({
  condition: 'GOOD'
})

const rules = {
  equipmentNo: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  equipmentName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

// 获取设备列表
const getEquipmentList = async () => {
  loading.value = true
  try {
    const res = await equipmentApi.getList(queryForm)
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    ElMessage.error('获取设备列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryForm.current = 1
  getEquipmentList()
}

// 重置
const handleReset = () => {
  queryForm.keyword = ''
  handleSearch()
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    NORMAL: 'success',
    BORROWED: 'warning',
    MAINTENANCE: 'danger',
    SCRAPPED: 'info'
  }
  return map[status] || 'info'
}

// 显示新增对话框
const showAddDialog = () => {
  dialogTitle.value = '新增设备'
  Object.assign(formData, {
    id: null,
    equipmentNo: '',
    equipmentName: '',
    categoryId: null,
    laboratoryId: null,
    brand: '',
    model: '',
    purchaseDate: '',
    purchasePrice: null,
    supplier: '',
    description: '',
    imageUrl: ''
  })
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  dialogTitle.value = '编辑设备'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 显示详情
const showDetail = (row) => {
  const imageHtml = row.imageUrl 
    ? `<p style="text-align: center; margin: 15px 0;"><img src="${getImageUrl(row.imageUrl)}" style="max-width: 300px; max-height: 200px; border-radius: 8px; border: 1px solid #ddd;" /></p>`
    : ''
  
  ElMessageBox.alert(`
    <div style="line-height: 2;">
      ${imageHtml}
      <p><strong>设备编号：</strong>${row.equipmentNo}</p>
      <p><strong>设备名称：</strong>${row.equipmentName}</p>
      <p><strong>品牌：</strong>${row.brand || '-'}</p>
      <p><strong>型号：</strong>${row.model || '-'}</p>
      <p><strong>分类：</strong>${row.categoryName || '-'}</p>
      <p><strong>所属实验室：</strong>${row.laboratoryName || '-'}</p>
      <p><strong>状态：</strong>${row.statusText}</p>
      <p><strong>描述：</strong>${row.description || '-'}</p>
    </div>
  `, '设备详情', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '关闭'
  })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (formData.id) {
          await equipmentApi.update(formData.id, formData)
          ElMessage.success('更新成功')
        } else {
          await equipmentApi.add(formData)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        getEquipmentList()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

// 对话框关闭
const handleDialogClose = () => {
  formRef.value?.resetFields()
}

// 显示借出对话框
const showBorrowDialog = (row) => {
  currentEquipment.value = row
  borrowForm.userId = null
  borrowForm.purpose = ''
  borrowDialogVisible.value = true
}

// 处理借出
const handleBorrow = async () => {
  if (!borrowForm.userId) {
    ElMessage.warning('请输入借用人ID')
    return
  }
  try {
    await equipmentApi.borrow(currentEquipment.value.id, borrowForm)
    ElMessage.success('借出成功')
    borrowDialogVisible.value = false
    getEquipmentList()
  } catch (error) {
    ElMessage.error('借出失败')
  }
}

// 显示归还对话框
const showReturnDialog = (row) => {
  currentEquipment.value = row
  returnForm.condition = 'GOOD'
  returnDialogVisible.value = true
}

// 处理归还
const handleReturn = async () => {
  try {
    await equipmentApi.returnEquipment(currentEquipment.value.id, returnForm)
    ElMessage.success('归还成功')
    returnDialogVisible.value = false
    getEquipmentList()
  } catch (error) {
    ElMessage.error('归还失败')
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该设备吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await equipmentApi.delete(row.id)
    ElMessage.success('删除成功')
    getEquipmentList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 导出
const handleExport = async () => {
  try {
    await ElMessageBox.confirm('确定要导出设备列表吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await equipmentApi.export()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '设备列表.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('导出失败')
    }
  }
}

// 显示二维码
const showQrCode = async (row) => {
  currentEquipment.value = row
  // 后端返回的是 /uploads/qrcode/xxx.png，需要加上 /api 前缀
  if (row.qrCodeImage) {
    qrCodeImageUrl.value = row.qrCodeImage.startsWith('/api')
      ? row.qrCodeImage
      : '/api' + row.qrCodeImage
  } else {
    qrCodeImageUrl.value = ''
  }
  qrCodeDialogVisible.value = true
}

// 下载二维码
const downloadQrCode = () => {
  if (!qrCodeImageUrl.value) return
  const link = document.createElement('a')
  link.href = qrCodeImageUrl.value
  link.download = `${currentEquipment.value.equipmentName}_二维码.png`
  link.click()
}

// 打开H5页面
const openH5Page = () => {
  if (!currentEquipment.value?.qrCode) return
  // 在新窗口打开H5页面
  window.open(currentEquipment.value.qrCode, '_blank')
}

// 批量重新生成二维码
const handleRegenerateAllQrCodes = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要重新生成所有设备的二维码吗？此操作会更新所有设备的二维码链接和图片。',
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    const res = await equipmentApi.regenerateAllQrCodes()
    ElMessage.success(`成功重新生成 ${res.data} 个设备的二维码`)
    await getEquipmentList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('重新生成二维码失败')
    }
  } finally {
    loading.value = false
  }
}

// 初始化
getEquipmentList()

// ==================== 图片上传相关 ====================

// 处理图片URL，添加 /api 前缀
const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return url.startsWith('/api') ? url : '/api' + url
}

// 上传前校验
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB！')
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
      formData.imageUrl = res.data || res.url
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
const handleRemoveImage = () => {
  formData.imageUrl = ''
}
</script>

<style lang="scss" scoped>
.equipment-page {
  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .image-error {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 250px;
    height: 250px;
    background: #f5f5f5;
    color: #999;
    font-size: 14px;

    .el-icon {
      font-size: 48px;
      margin-bottom: 10px;
    }
  }

  // 图片上传样式
  .image-upload-container {
    .equipment-image-uploader {
      .upload-placeholder {
        width: 120px;
        height: 120px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        border: 1px dashed #d9d9d9;
        border-radius: 6px;
        background: #fafafa;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          border-color: #409eff;
          background: #ecf5ff;
        }

        .el-icon {
          font-size: 28px;
          color: #8c939d;
          margin-bottom: 8px;
        }

        span {
          font-size: 12px;
          color: #8c939d;
        }
      }

      .image-preview {
        position: relative;
        width: 120px;
        height: 120px;

        .image-actions {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          background: rgba(0, 0, 0, 0.5);
          border-radius: 6px;
          opacity: 0;
          transition: opacity 0.3s;

          &:hover {
            opacity: 1;
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
}
</style>
