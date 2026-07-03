import request from './request'

// 认证相关
export const authApi = {
  // 登录
  login(data) {
    return request.post('/user/login', data)
  },
  // 登出
  logout() {
    return request.post('/user/logout')
  },
  // 获取当前用户信息
  getCurrentUser() {
    return request.get('/user/info')
  },
  // 修改密码
  changePassword(data) {
    return request.post('/user/change-password', data)
  }
}

// 设备相关
export const equipmentApi = {
  // 获取设备列表
  getList(params) {
    return request.get('/equipment/list', { params })
  },
  // 获取设备详情
  getById(id) {
    return request.get(`/equipment/${id}`)
  },
  // 新增设备
  add(data) {
    return request.post('/equipment', data)
  },
  // 更新设备
  update(id, data) {
    return request.put(`/equipment/${id}`, data)
  },
  // 删除设备
  delete(id) {
    return request.delete(`/equipment/${id}`)
  },
  // 设备出库
  borrow(id, data) {
    return request.post(`/equipment/${id}/borrow`, null, { params: data })
  },
  // 设备归还
  returnEquipment(id, data) {
    return request.post(`/equipment/${id}/return`, null, { params: data })
  },
  // 导出设备列表
  export() {
    return request.get('/equipment/export', { responseType: 'blob' })
  },
  // 批量重新生成所有设备的二维码
  regenerateAllQrCodes() {
    return request.post('/equipment/qrcode/regenerate-all')
  },
  // 重新生成指定设备的二维码
  regenerateQrCode(id) {
    return request.post(`/equipment/${id}/qrcode/regenerate`)
  }
}

// 预约相关
export const reservationApi = {
  // 获取预约列表
  getList(params) {
    return request.get('/reservation/list', { params })
  },
  // 获取我的预约
  getMyList(params) {
    return request.get('/reservation/my', { params })
  },
  // 获取预约详情
  getById(id) {
    return request.get(`/reservation/${id}`)
  },
  // 创建预约
  create(data) {
    return request.post('/reservation', data)
  },
  // 审核预约
  approve(id, data) {
    return request.post(`/reservation/${id}/approve`, null, { params: data })
  },
  // 取消预约
  cancel(id) {
    return request.post(`/reservation/${id}/cancel`)
  }
}

// 报修相关
export const repairApi = {
  // 获取报修列表
  getList(params) {
    return request.get('/repair/list', { params })
  },
  // 获取报修详情
  getById(id) {
    return request.get(`/repair/${id}`)
  },
  // 提交报修
  submit(data) {
    return request.post('/repair', data)
  },
  // 处理报修
  handle(id) {
    return request.post(`/repair/${id}/handle`)
  },
  // 完成报修
  complete(id, data) {
    return request.post(`/repair/${id}/complete`, null, { params: data })
  },
  // 驳回报修
  reject(id, data) {
    return request.post(`/repair/${id}/reject`, null, { params: data })
  },
  // 获取待处理报修数量
  getPendingCount() {
    return request.get('/repair/pending-count')
  },
  // 分配报修任务
  assign(id, assigneeId) {
    return request.post(`/repair/${id}/assign`, null, { params: { assigneeId } })
  },
  // 获取分配给我的报修任务
  getMyTasks(params) {
    return request.get('/repair/my-tasks', { params })
  }
}

// 维护相关
export const maintenanceApi = {
  // 获取维护计划列表
  getPlans(params) {
    return request.get('/maintenance/plans', { params })
  },
  // 创建维护计划
  createPlan(data) {
    return request.post('/maintenance/plans', data)
  },
  // 更新维护计划
  updatePlan(id, data) {
    return request.put(`/maintenance/plans/${id}`, data)
  },
  // 删除维护计划
  deletePlan(id) {
    return request.delete(`/maintenance/plans/${id}`)
  },
  // 获取维护记录列表
  getRecords(params) {
    return request.get('/maintenance/records', { params })
  },
  // 添加维护记录
  addRecord(data) {
    return request.post('/maintenance/records', data)
  },
  // 完成维护
  complete(planId, data) {
    return request.post(`/maintenance/plans/${planId}/complete`, null, { params: data })
  },
  // 获取待提醒计划
  getReminders() {
    return request.get('/maintenance/reminders')
  },
  // 分配维护任务
  assignPlan(id, assigneeId) {
    return request.post(`/maintenance/plans/${id}/assign`, null, { params: { assigneeId } })
  },
  // 获取分配给我的维护任务
  getMyPlans(params) {
    return request.get('/maintenance/my-plans', { params })
  },
  // 获取老师列表（用于分配任务）
  getTeacherList(params) {
    return request.get('/maintenance/teachers', { params })
  }
}

// 统计相关
export const statisticsApi = {
  // 获取统计数据
  getStatistics() {
    return request.get('/statistics')
  },
  // 获取利用率趋势
  getUtilizationTrend(params) {
    return request.get('/statistics/utilization-trend', { params })
  },
  // 获取按分类统计
  getByCategory() {
    return request.get('/statistics/by-category')
  },
  // 导出统计报表
  export() {
    return request.get('/statistics/export', { responseType: 'blob' })
  }
}

// 文件上传相关
export const fileApi = {
  // 单文件上传
  upload(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  // 多文件上传
  uploads(files) {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    return request.post('/file/uploads', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

// 用户管理相关
export const userApi = {
  // 获取用户列表
  getList(params) {
    return request.get('/admin/user/list', { params })
  },
  // 获取用户详情
  getById(id) {
    return request.get(`/admin/user/${id}`)
  },
  // 创建用户
  create(data) {
    return request.post('/admin/user/create', data)
  },
  // 更新用户
  update(data) {
    return request.put('/admin/user/update', data)
  },
  // 删除用户
  delete(id) {
    return request.delete(`/admin/user/${id}`)
  },
  // 批量删除用户
  batchDelete(userIds) {
    return request.delete('/admin/user/batch', { data: userIds })
  },
  // 重置密码
  resetPassword(data) {
    return request.post('/admin/user/reset-password', data)
  },
  // 批量更新用户状态
  batchUpdateStatus(userIds, status) {
    return request.put('/admin/user/status', userIds, { params: { status } })
  },
  // 获取角色列表
  getRoles() {
    return request.get('/admin/user/roles')
  },
  // 获取学生列表（用于分配任务，管理员和老师可访问）
  getStudentList(params) {
    return request.get('/admin/user/students', { params })
  },
  // 获取老师列表（用于分配任务）
  getTeacherList(params) {
    return request.get('/repair/teachers', { params })
  }
}
