-- 高校实验室设备智能管理系统数据库表结构
-- 创建数据库
CREATE DATABASE IF NOT EXISTS lab_equipment_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lab_equipment_db;

-- 1. 用户表
CREATE TABLE `sys_user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `student_no` VARCHAR(20) COMMENT '学号/工号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '电话',
    `role` VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT '角色：ADMIN-管理员，TEACHER-教师，STUDENT-学生',
    `department` VARCHAR(100) COMMENT '院系',
    `major` VARCHAR(100) COMMENT '专业',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (`username`),
    INDEX idx_student_no (`student_no`),
    INDEX idx_role (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 设备分类表
CREATE TABLE `equipment_category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `category_code` VARCHAR(20) NOT NULL UNIQUE COMMENT '分类编码',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID，0为顶级分类',
    `description` VARCHAR(200) COMMENT '分类描述',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备分类表';

-- 3. 实验室表
CREATE TABLE `laboratory` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '实验室ID',
    `lab_name` VARCHAR(100) NOT NULL COMMENT '实验室名称',
    `lab_code` VARCHAR(20) NOT NULL UNIQUE COMMENT '实验室编码',
    `location` VARCHAR(200) COMMENT '位置',
    `capacity` INT COMMENT '容纳人数',
    `manager_id` BIGINT COMMENT '管理员ID',
    `description` TEXT COMMENT '描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-关闭，1-开放',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_lab_code (`lab_code`),
    INDEX idx_manager_id (`manager_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='实验室表';

-- 4. 设备表
CREATE TABLE `equipment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    `equipment_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编号',
    `equipment_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
    `category_id` BIGINT COMMENT '分类ID',
    `laboratory_id` BIGINT COMMENT '所属实验室ID',
    `brand` VARCHAR(50) COMMENT '品牌',
    `model` VARCHAR(50) COMMENT '型号',
    `serial_number` VARCHAR(50) COMMENT '序列号',
    `qr_code` VARCHAR(255) COMMENT '二维码内容',
    `qr_code_image` VARCHAR(255) COMMENT '二维码图片URL',
    `purchase_date` DATE COMMENT '购买日期',
    `purchase_price` DECIMAL(10,2) COMMENT '购买价格',
    `supplier` VARCHAR(100) COMMENT '供应商',
    `warranty_date` DATE COMMENT '保修到期日期',
    `status` VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '状态：NORMAL-正常，BORROWED-借出，MAINTENANCE-维修中，SCRAPPED-报废',
    `condition` VARCHAR(20) DEFAULT 'GOOD' COMMENT '完好程度：GOOD-完好，DAMAGED-损坏，SERIOUS-严重损坏',
    `specifications` TEXT COMMENT '规格参数',
    `description` TEXT COMMENT '描述',
    `image_url` VARCHAR(255) COMMENT '设备图片URL',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_equipment_no (`equipment_no`),
    INDEX idx_category_id (`category_id`),
    INDEX idx_laboratory_id (`laboratory_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 5. 设备借用记录表
CREATE TABLE `borrow_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '借出记录ID',
    `borrow_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '借出编号',
    `equipment_id` BIGINT NOT NULL COMMENT '设备ID',
    `user_id` BIGINT NOT NULL COMMENT '借用人ID',
    `borrow_date` DATETIME NOT NULL COMMENT '借出时间',
    `expected_return_date` DATETIME COMMENT '预计归还时间',
    `actual_return_date` DATETIME COMMENT '实际归还时间',
    `borrow_purpose` VARCHAR(200) COMMENT '借用用途',
    `borrow_status` VARCHAR(20) NOT NULL DEFAULT 'BORROWED' COMMENT '状态：BORROWED-借出中，RETURNED-已归还，OVERDUE-逾期',
    `handler_id` BIGINT COMMENT '处理人ID（管理员）',
    `return_handler_id` BIGINT COMMENT '归还处理人ID',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_borrow_no (`borrow_no`),
    INDEX idx_equipment_id (`equipment_id`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_borrow_status (`borrow_status`),
    INDEX idx_borrow_date (`borrow_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备借用记录表';

-- 6. 设备预约表
CREATE TABLE `equipment_reservation` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约ID',
    `reservation_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '预约编号',
    `equipment_id` BIGINT NOT NULL COMMENT '设备ID',
    `user_id` BIGINT NOT NULL COMMENT '预约人ID',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `purpose` VARCHAR(200) COMMENT '使用目的',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待审核，APPROVED-已批准，REJECTED-已拒绝，CANCELLED-已取消，COMPLETED-已完成',
    `approve_user_id` BIGINT COMMENT '审核人ID',
    `approve_time` DATETIME COMMENT '审核时间',
    `approve_remark` VARCHAR(200) COMMENT '审核备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_reservation_no (`reservation_no`),
    INDEX idx_equipment_id (`equipment_id`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_status (`status`),
    INDEX idx_start_time (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备预约表';

-- 7. 故障报修表
CREATE TABLE `repair_request` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报修ID',
    `repair_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '报修编号',
    `equipment_id` BIGINT NOT NULL COMMENT '设备ID',
    `report_user_id` BIGINT NOT NULL COMMENT '报修人ID',
    `fault_description` TEXT NOT NULL COMMENT '故障描述',
    `fault_image_urls` VARCHAR(500) COMMENT '故障图片URL，多个用逗号分隔',
    `report_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报修时间',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理，PROCESSING-处理中，COMPLETED-已完成，REJECTED-已驳回',
    `priority` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '优先级：LOW-低，NORMAL-中，HIGH-高，URGENT-紧急',
    `handler_id` BIGINT COMMENT '处理人ID',
    `handle_time` DATETIME COMMENT '处理时间',
    `complete_time` DATETIME COMMENT '完成时间',
    `repair_description` TEXT COMMENT '维修描述',
    `repair_cost` DECIMAL(10,2) COMMENT '维修费用',
    `parts_used` VARCHAR(500) COMMENT '使用配件',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_repair_no (`repair_no`),
    INDEX idx_equipment_id (`equipment_id`),
    INDEX idx_report_user_id (`report_user_id`),
    INDEX idx_status (`status`),
    INDEX idx_report_time (`report_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='故障报修表';

-- 8. 设备维护计划表
CREATE TABLE `maintenance_plan` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '维护计划ID',
    `plan_name` VARCHAR(100) NOT NULL COMMENT '计划名称',
    `equipment_id` BIGINT COMMENT '设备ID，为空表示适用所有设备',
    `category_id` BIGINT COMMENT '设备分类ID，为空表示适用所有设备',
    `maintenance_type` VARCHAR(20) NOT NULL COMMENT '维护类型：DAILY-日常，WEEKLY-每周，MONTHLY-每月，QUARTERLY-每季度，YEARLY-每年',
    `maintenance_cycle` INT NOT NULL COMMENT '维护周期（天数）',
    `next_maintenance_date` DATE NOT NULL COMMENT '下次维护日期',
    `description` TEXT COMMENT '维护说明',
    `responsible_user_id` BIGINT COMMENT '负责人ID',
    `is_active` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_equipment_id (`equipment_id`),
    INDEX idx_category_id (`category_id`),
    INDEX idx_next_maintenance_date (`next_maintenance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备维护计划表';

-- 9. 设备维护记录表
CREATE TABLE `maintenance_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '维护记录ID',
    `record_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '记录编号',
    `plan_id` BIGINT COMMENT '维护计划ID',
    `equipment_id` BIGINT NOT NULL COMMENT '设备ID',
    `maintenance_date` DATETIME NOT NULL COMMENT '维护时间',
    `maintenance_type` VARCHAR(20) NOT NULL COMMENT '维护类型',
    `maintenance_content` TEXT COMMENT '维护内容',
    `maintenance_result` VARCHAR(20) NOT NULL COMMENT '维护结果：SUCCESS-成功，FAILED-失败，PARTIAL-部分完成',
    `maintainer_id` BIGINT NOT NULL COMMENT '维护人ID',
    `maintenance_duration` INT COMMENT '维护时长（分钟）',
    `cost` DECIMAL(10,2) COMMENT '维护费用',
    `parts_used` VARCHAR(500) COMMENT '使用配件',
    `next_maintenance_date` DATE COMMENT '下次维护日期',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_record_no (`record_no`),
    INDEX idx_equipment_id (`equipment_id`),
    INDEX idx_maintenance_date (`maintenance_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备维护记录表';

-- 10. 维护提醒表
CREATE TABLE `maintenance_reminder` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提醒ID',
    `plan_id` BIGINT NOT NULL COMMENT '维护计划ID',
    `equipment_id` BIGINT COMMENT '设备ID',
    `reminder_date` DATETIME NOT NULL COMMENT '提醒日期',
    `reminder_type` VARCHAR(20) NOT NULL COMMENT '提醒类型：EMAIL-邮件，SYSTEM-系统通知',
    `recipient_user_id` BIGINT NOT NULL COMMENT '接收人ID',
    `is_sent` TINYINT DEFAULT 0 COMMENT '是否已发送：0-未发送，1-已发送',
    `send_time` DATETIME COMMENT '发送时间',
    `send_status` VARCHAR(20) COMMENT '发送状态：SUCCESS-成功，FAILED-失败',
    `remark` VARCHAR(500) COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_plan_id (`plan_id`),
    INDEX idx_equipment_id (`equipment_id`),
    INDEX idx_reminder_date (`reminder_date`),
    INDEX idx_is_sent (`is_sent`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='维护提醒表';

-- 11. 操作日志表
CREATE TABLE `operation_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT COMMENT '操作人ID',
    `username` VARCHAR(50) COMMENT '操作人用户名',
    `operation` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `module` VARCHAR(50) NOT NULL COMMENT '操作模块',
    `description` VARCHAR(500) COMMENT '操作描述',
    `request_method` VARCHAR(10) COMMENT '请求方法',
    `request_url` VARCHAR(255) COMMENT '请求URL',
    `request_params` TEXT COMMENT '请求参数',
    `response_result` TEXT COMMENT '响应结果',
    `ip_address` VARCHAR(50) COMMENT 'IP地址',
    `execute_time` INT COMMENT '执行时长（毫秒）',
    `status` TINYINT COMMENT '状态：0-失败，1-成功',
    `error_message` TEXT COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (`user_id`),
    INDEX idx_operation (`operation`),
    INDEX idx_module (`module`),
    INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 12. 系统配置表
CREATE TABLE `sys_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(50) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` VARCHAR(500) COMMENT '配置值',
    `config_type` VARCHAR(20) COMMENT '配置类型：STRING-字符串，NUMBER-数字，BOOLEAN-布尔',
    `description` VARCHAR(200) COMMENT '配置描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 13. 通知表
CREATE TABLE `notification` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content` TEXT NOT NULL COMMENT '通知内容',
    `type` VARCHAR(20) NOT NULL COMMENT '通知类型：SYSTEM-系统，APPROVE-审核，MAINTENANCE-维护，OVERDUE-逾期',
    `recipient_id` BIGINT NOT NULL COMMENT '接收人ID',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    `read_time` DATETIME COMMENT '阅读时间',
    `related_id` BIGINT COMMENT '关联ID（如预约ID、报修ID等）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_recipient_id (`recipient_id`),
    INDEX idx_is_read (`is_read`),
    INDEX idx_type (`type`),
    INDEX idx_create_time (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 14. 文件上传表
CREATE TABLE `file_upload` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文件ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
    `file_size` BIGINT COMMENT '文件大小（字节）',
    `file_type` VARCHAR(50) COMMENT '文件类型',
    `upload_user_id` BIGINT COMMENT '上传人ID',
    `module` VARCHAR(50) COMMENT '所属模块',
    `related_id` BIGINT COMMENT '关联ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_module (`module`),
    INDEX idx_related_id (`related_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件上传表';

-- 初始化默认数据
-- 插入默认管理员账号（密码: admin123，需要加密）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `student_no`, `email`, `phone`, `role`, `department`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'ADMIN001', 'admin@lab.edu', '13800138000', 'ADMIN', '信息中心');

-- 插入默认设备分类
INSERT INTO `equipment_category` (`category_name`, `category_code`, `parent_id`, `description`, `sort`) VALUES
('计算机设备', 'COMPUTER', 0, '计算机相关设备', 1),
('电子仪器', 'ELECTRONIC', 0, '电子测量仪器', 2),
('机械设备', 'MECHANICAL', 0, '机械类设备', 3),
('化学设备', 'CHEMICAL', 0, '化学实验设备', 4),
('物理设备', 'PHYSICAL', 0, '物理实验设备', 5),
('生物设备', 'BIOLOGICAL', 0, '生物实验设备', 6);

-- 插入子分类
INSERT INTO `equipment_category` (`category_name`, `category_code`, `parent_id`, `description`, `sort`) VALUES
('台式电脑', 'PC', 1, '个人电脑', 1),
('笔记本电脑', 'LAPTOP', 1, '便携式电脑', 2),
('服务器', 'SERVER', 1, '服务器设备', 3),
('示波器', 'OSCILLOSCOPE', 2, '示波器设备', 1),
('万用表', 'MULTIMETER', 2, '万用表设备', 2);

-- 插入默认实验室
INSERT INTO `laboratory` (`lab_name`, `lab_code`, `location`, `capacity`, `manager_id`, `description`) VALUES
('计算机实验室1', 'LAB001', '实验楼A座101', 50, 1, '计算机科学与技术实验室'),
('电子实验室1', 'LAB002', '实验楼A座201', 30, 1, '电子工程实验室'),
('机械实验室1', 'LAB003', '实验楼B座101', 25, 1, '机械工程实验室'),
('化学实验室1', 'LAB004', '实验楼C座101', 20, 1, '化学工程实验室'),
('物理实验室1', 'LAB005', '实验楼D座101', 35, 1, '物理实验室');

-- 插入系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_type`, `description`) VALUES
('borrow.max.days', '7', 'NUMBER', '最大借用天数'),
('overdue.reminder.days', '3', 'NUMBER', '逾期提前提醒天数'),
('maintenance.reminder.days', '7', 'NUMBER', '维护提前提醒天数'),
('approval.required', 'true', 'BOOLEAN', '预约是否需要审核'),
('file.upload.path', '/uploads/', 'STRING', '文件上传路径'),
('qr.code.base.url', 'http://localhost:8080', 'STRING', '二维码基础URL');
