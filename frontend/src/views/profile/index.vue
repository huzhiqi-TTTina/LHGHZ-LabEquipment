<template>
  <div class="profile-page">
    <el-card class="profile-card">
      <template #header>
        <span>个人中心</span>
      </template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户名">{{ userInfo?.username }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ userInfo?.realName }}</el-descriptions-item>
        <el-descriptions-item label="学号/工号">{{ userInfo?.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userInfo?.email }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ userInfo?.phone }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ getRoleText(userInfo?.role) }}</el-descriptions-item>
        <el-descriptions-item label="院系">{{ userInfo?.department }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ userInfo?.major }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="password-card">
      <template #header>
        <span>修改密码</span>
      </template>
      <el-form ref="formRef" :model="passwordForm" :rules="rules" label-width="100px" style="max-width: 500px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleChangePassword" :loading="loading">确认修改</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const userInfo = ref(null)
const loading = ref(false)
const formRef = ref(null)

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }]
}

const getRoleText = (role) => {
  const map = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }
  return map[role] || role
}

const getUserInfo = async () => {
  try {
    const res = await authApi.getCurrentUser()
    userInfo.value = res.data
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

const handleChangePassword = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await authApi.changePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        userStore.logout()
        setTimeout(() => {
          location.href = '/login'
        }, 1000)
      } catch (error) {
        ElMessage.error('密码修改失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleReset = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  getUserInfo()
})
</script>

<style lang="scss" scoped>
.profile-page {
  .profile-card {
    margin-bottom: 20px;

    :deep(.el-descriptions__label) {
      width: 120px;
    }
  }

  .password-card {
    max-width: 600px;
  }
}
</style>
