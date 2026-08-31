# 实验室设备智能管理系统

一个基于 B/S 架构的高校实验室设备智能化管理系统，实现设备的入库、借出、预约、报修、维护等全流程管理。

## 技术栈

### 后端
- Spring Boot 3.2.0
- MyBatis Plus 3.5.5
- Spring Security + JWT
- MySQL
- Redis
- Druid 连接池
- Apache POI (Excel 导出)
- ZXing (二维码生成)

### 前端
- Vue 3
- Element Plus
- Vue Router
- Pinia
- Axios
- ECharts
- Vite

## 功能模块

### 1. 设备管理模块
- 设备入库登记（设备基本信息、二维码生成）
- 设备出库登记（借用记录、归还确认）
- 设备信息查询与编辑
- Excel 导出功能

### 2. 预约借用模块
- 在线预约功能（用户选择设备、时间段）
- 预约审核与管理
- 借用记录查询

### 3. 故障报修模块
- 故障提交（描述故障现象、上传图片）
- 维修状态跟踪
- 维修记录管理

### 4. 维护提醒模块
- 设备定期维护计划设置
- 自动提醒功能（邮件/系统通知）
- 维护记录登记

### 5. 统计报表模块
- 设备利用率统计（按时间、设备类型等维度）
- 设备损坏率分析
- Excel 报表导出功能

## 项目结构

```
LabEquipment33333/
├── src/main/java/com/lab/equipment/
│   ├── common/          # 通用类
│   ├── controller/      # 控制器层
│   ├── dto/             # 数据传输对象
│   ├── entity/          # 实体类
│   ├── enums/           # 枚举类
│   ├── exception/       # 异常处理
│   ├── mapper/          # 数据访问层
│   ├── security/        # 安全配置
│   ├── service/         # 服务层
│   ├── util/            # 工具类
│   └── LabEquipmentApplication.java
├── src/main/resources/
│   ├── db/              # 数据库脚本
│   └── application.yml  # 配置文件
└── frontend/            # 前端项目
    ├── src/
    │   ├── api/         # API 接口
    │   ├── components/  # 公共组件
    │   ├── layout/      # 布局组件
    │   ├── router/      # 路由配置
    │   ├── stores/      # 状态管理
    │   ├── styles/      # 样式文件
    │   └── views/       # 页面组件
    ├── index.html
    ├── package.json
    └── vite.config.js
```

## 快速开始

### 1. 数据库初始化

```sql
-- 执行 src/main/resources/db/schema.sql 初始化数据库表结构和初始数据
```

### 2. 后端启动

1. 修改 `application.yml` 中的数据库配置
2. 启动 Spring Boot 应用

```bash
mvn spring-boot:run
```

默认管理员账号：`admin` / `admin123`

### 3. 前端启动

```bash
cd frontend
npm install
npm run dev
```

访问地址：http://localhost:3000

## API 接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/info` - 获取当前用户信息
- `POST /api/auth/change-password` - 修改密码

### 设备接口
- `GET /api/equipment/list` - 获取设备列表（分页）
- `GET /api/equipment/{id}` - 获取设备详情
- `POST /api/equipment` - 新增设备
- `PUT /api/equipment/{id}` - 更新设备
- `DELETE /api/equipment/{id}` - 删除设备
- `POST /api/equipment/{id}/borrow` - 设备借出
- `POST /api/equipment/{id}/return` - 设备归还
- `POST /api/equipment/qrcode/regenerate-all` - 批量生成设备二维码
- `POST /api/equipment/{id}/qrcode/regenerate` - 生成单个设备二维码

### 预约接口
- `GET /api/reservation/list` - 获取预约列表（分页）
- `GET /api/reservation/my` - 获取我的预约
- `GET /api/reservation/{id}` - 获取预约详情
- `POST /api/reservation` - 创建预约
- `POST /api/reservation/{id}/approve` - 审核预约
- `POST /api/reservation/{id}/cancel` - 取消预约

### 报修接口
- `GET /api/repair/list` - 获取报修列表（分页）
- `GET /api/repair/{id}` - 获取报修详情
- `POST /api/repair` - 提交报修
- `POST /api/repair/{id}/handle` - 处理报修
- `POST /api/repair/{id}/complete` - 完成报修
- `POST /api/repair/{id}/reject` - 驳回报修
- `GET /api/repair/pending-count` - 获取待处理报修数量
- `POST /api/repair/{id}/assign` - 分配报修任务
- `GET /api/repair/my-tasks` - 获取我的报修任务

### 维护接口
- `GET /api/maintenance/plans` - 获取维护计划列表
- `POST /api/maintenance/plans` - 创建维护计划
- `PUT /api/maintenance/plans/{id}` - 更新维护计划
- `DELETE /api/maintenance/plans/{id}` - 删除维护计划
- `GET /api/maintenance/records` - 获取维护记录
- `POST /api/maintenance/records` - 添加维护记录
- `POST /api/maintenance/plans/{id}/complete` - 完成维护
- `GET /api/maintenance/reminders` - 获取维护提醒列表
- `POST /api/maintenance/plans/{id}/assign` - 分配维护任务
- `GET /api/maintenance/my-plans` - 获取我的维护计划
- `GET /api/maintenance/teachers` - 获取维护负责老师列表

### 统计接口
- `GET /api/statistics` - 获取统计数据
- `GET /api/statistics/export` - 导出统计数据

### 文件接口
- `POST /api/file/upload` - 上传单个文件
- `POST /api/file/uploads` - 上传多个文件

### 用户管理接口
- `GET /api/admin/user/list` - 获取用户列表（分页）
- `GET /api/admin/user/{id}` - 获取用户详情
- `POST /api/admin/user/create` - 新增用户
- `PUT /api/admin/user/update` - 更新用户信息
- `DELETE /api/admin/user/{id}` - 删除用户
- `DELETE /api/admin/user/batch` - 批量删除用户
- `POST /api/admin/user/reset-password` - 重置用户密码
- `PUT /api/admin/user/status` - 批量修改用户状态
- `GET /api/admin/user/roles` - 获取所有角色
- `GET /api/admin/user/students` - 获取所有学生列表
- `GET /api/repair/teachers` - 获取所有老师列表

## 权限说明

| 角色 | 权限 |
|------|------|
| ADMIN | 管理员，拥有所有权限 |
| TEACHER | 教师，可预约设备、提交报修 |
| STUDENT | 学生，可预约设备、提交报修 |

## 注意事项

1. 文件上传路径需要根据实际环境修改 `application.yml` 中的 `file.upload.path`
2. 邮件发送功能需要配置正确的 SMTP 服务器信息
3. Redis 用于缓存和会话管理，建议在生产环境使用

## 许可证
MIT License