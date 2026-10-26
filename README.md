# 实验室设备智能管理系统

一个基于 B/S 架构的高校实验室设备智能化管理系统。本项目在原架构基础上进行了深度重构与拓展，实现了设备的入库、借出、预约、报修、维护等全流程管理，并引入了基于大模型的 AI 智能辅助与移动端扫码 H5 交互。

## 技术栈

**后端**
- Spring Boot 3.2.0
- MyBatis Plus 3.5.5 (支持复杂条件构造与分页)
- Spring Security + JWT (无状态鉴权与 RBAC 权限控制)
- MySQL 8.0
- Redis (缓存与会话管理)
- Druid 连接池 (SQL 监控)
- Apache POI (Excel 数据导出)
- ZXing (设备二维码生成)
- LLM API (接入通义千问大模型)

**前端**
- Vue 3 + Element Plus
- Vue Router + Pinia
- Axios (统一请求封装与拦截器)
- ECharts (统计数据可视化)
- Vite

---

## 核心功能模块

### 1. 设备管理模块
- 设备入库登记（基本信息、自动生成二维码）
- 设备出库登记（借用记录、归还确认）
- 设备信息查询与编辑（支持按 ID 或中文名称模糊检索）
- Excel 导出功能

### 2. 预约借用模块
- 在线预约功能（选择可用设备与时间段）
- 预约审核与管理
- 借用记录查询

### 3. 故障报修模块
- 故障提交与上传
- 维修状态跟踪与流转（分配、处理、完成、驳回）
- 维修记录管理

### 4. 维护提醒模块
- 设备定期维护计划设置
- 自动提醒功能（邮件/系统通知）
- 维护记录登记

### 5. 统计报表模块
- 设备利用率统计（按时间趋势、按分类维度）
- 设备损坏率分析
- ECharts 可视化展示与 Excel 报表导出

### 6. AI 智能助手模块（拓展亮点）
- **AI 智能问答**：接入大语言模型，支持自然语言交互。
- **个性化推荐算法**：基于用户历史借用行为与 AI 生成结果，实现设备智能推荐。

### 7. H5 扫码交互模块（拓展亮点）
- **轻量级移动端**：前端通过扫码直接拉起 H5 静态页面，无需下载 App。
- **扫码借还**：在移动端实现设备详情查看与一键出库/入库，形成业务闭环。

---

## 接口设计说明 (API Design)

系统全面采用 **RESTful 风格** 进行接口设计，共计提供 **63 个后端 API 接口**，涵盖认证、设备、预约、报修、维护、统计、文件、用户、AI 与 H5 模块。

- **统一响应结构**：所有接口均返回标准 `Result<T>` 或 `PageResult<T>` 格式，包含 `code`、`message`、`data`，极大降低前后端联调成本。
- **全局异常处理**：基于 `@RestControllerAdvice` 实现全局异常拦截，对 400（参数校验）、403（权限不足）、500（系统异常）进行统一的格式化封装，防止敏感信息泄露。
- **参数校验**：DTO 层广泛使用 Hibernate Validator 结合 `@Valid` 注解，确保非法入参在进入 Controller 前被拦截。

---

## 项目结构

```text
LabEquipment33333/
├── src/main/java/com/lab/equipment/
│   ├── common/          # 通用类 (Result 等)
│   ├── controller/      # 控制器层 (RESTful 接口)
│   ├── dto/             # 数据传输对象
│   ├── entity/          # 实体类
│   ├── enums/           # 枚举类 (状态机)
│   ├── exception/       # 全局异常处理
│   ├── mapper/          # 数据访问层 (MyBatis Plus)
│   ├── security/        # 安全配置 (JWT、过滤器)
│   ├── service/         # 服务层
│   ├── util/            # 工具类 (JWT, Excel, QRCode)
│   └── LabEquipmentApplication.java
├── src/main/resources/
│   ├── db/              # 数据库脚本 (schema, data)
│   ├── static/h5/       # H5 扫码静态页面
│   └── application.yml  # 全局配置文件
└── frontend/            # 前端 Vue 3 项目
```

---

## 快速开始

### 1. 数据库初始化
执行 `src/main/resources/db/schema.sql` 初始化数据库表结构，执行 `data.sql` 载入初始数据。

### 2. 后端启动
修改 `application.yml` 中的数据库与 Redis 配置。
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
访问地址：`http://localhost:3000`

---

## API 接口清单（共 63 个）

### 一、认证接口（4个）
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/info` - 获取当前用户信息
- `POST /api/auth/change-password` - 修改密码

### 二、设备接口（11个）
- `GET /api/equipment/list` - 获取设备列表（分页、条件检索）
- `GET /api/equipment/available` - 获取可用设备列表
- `GET /api/equipment/{id}` - 获取设备详情
- `POST /api/equipment` - 新增设备
- `PUT /api/equipment/{id}` - 更新设备
- `DELETE /api/equipment/{id}` - 删除设备
- `POST /api/equipment/{id}/borrow` - 设备借出
- `POST /api/equipment/{id}/return` - 设备归还
- `GET /api/equipment/export` - 导出设备列表
- `POST /api/equipment/qrcode/regenerate-all` - 批量生成设备二维码
- `POST /api/equipment/{id}/qrcode/regenerate` - 生成单个设备二维码

### 三、预约接口（6个）
- `GET /api/reservation/list` - 获取预约列表（分页）
- `GET /api/reservation/my` - 获取我的预约
- `GET /api/reservation/{id}` - 获取预约详情
- `POST /api/reservation` - 创建预约
- `POST /api/reservation/{id}/approve` - 审核预约
- `POST /api/reservation/{id}/cancel` - 取消预约

### 四、报修接口（11个）
- `GET /api/repair/list` - 获取报修列表（分页）
- `GET /api/repair/{id}` - 获取报修详情
- `POST /api/repair` - 提交报修
- `POST /api/repair/{id}/handle` - 处理报修
- `POST /api/repair/{id}/complete` - 完成报修
- `POST /api/repair/{id}/reject` - 驳回报修
- `GET /api/repair/pending-count` - 获取待处理报修数量
- `POST /api/repair/{id}/assign` - 分配报修任务
- `GET /api/repair/my-tasks` - 获取我的报修任务
- `GET /api/repair/teachers` - 获取所有老师列表
- `GET /api/repair/stats` - 获取报修任务统计

### 五、维护接口（11个）
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

### 六、统计接口（4个）
- `GET /api/statistics` - 获取整体统计数据
- `GET /api/statistics/by-category` - 按设备分类统计
- `GET /api/statistics/utilization-trend` - 获取利用率趋势数据
- `GET /api/statistics/export` - 导出统计数据

### 七、文件接口（2个）
- `POST /api/file/upload` - 上传单个文件
- `POST /api/file/uploads` - 上传多个文件

### 八、用户管理接口（10个）
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

### 九、AI 智能助手接口（1个）
- `POST /api/ai/chat` - AI 智能问答（含推荐逻辑）

### 十、H5 扫码交互接口（3个）
- `GET /api/h5/equipment/{id}` - H5 端获取设备详情
- `POST /api/h5/equipment/{id}/checkout` - H5 端设备借出
- `POST /api/h5/equipment/{id}/checkin` - H5 端设备归还

---

## 权限说明

| 角色 | 权限 |
|------|------|
| ADMIN | 管理员，拥有所有权限 |
| TEACHER | 教师，可预约设备、提交报修 |
| STUDENT | 学生，可预约设备、提交报修 |

**认证机制**：基于 Spring Security + JWT，实现无状态认证。自定义 `JwtAuthenticationFilter` 拦截请求校验 Token，结合 `@PreAuthorize` 实现方法级细粒度权限控制。

## 注意事项
1. 文件上传路径与二维码基础 URL 需根据实际部署环境修改 `application.yml`。
2. AI 模块需要配置有效的 `ai.api-key` 才能正常使用。

## 许可证
MIT License