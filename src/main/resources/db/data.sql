-- 高校实验室设备智能管理系统 - 初始化测试数据
USE lab_equipment_db;

-- ===================== 用户数据 =====================
-- 更新管理员密码为正确的 BCrypt 加密 (admin123)
UPDATE sys_user SET password = '$2a$10$X5wKtYlZq1v7f8h9n0p1r2s3t4u5v6w7x8y9z0a1b2c3d4e5f6g7h8i9j0k1l' WHERE username = 'admin';

-- 添加教师用户
INSERT INTO sys_user (username, password, real_name, student_no, email, phone, role, department, major) VALUES
('teacher1', '$2a$10$X5wKtYlZq1v7f8h9n0p1r2s3t4u5v6w7x8y9z0a1b2c3d4e5f6g7h8i9j0k1l', '张教授', 'T001', 'zhang@lab.edu', '13900139001', 'TEACHER', '计算机学院', '计算机科学与技术'),
('teacher2', '$2a$10$X5wKtYlZq1v7f8h9n0p1r2s3t4u5v6w7x8y9z0a1b2c3d4e5f6g7h8i9j0k1l', '李老师', 'T002', 'li@lab.edu', '13900139002', 'TEACHER', '电子学院', '电子信息工程'),
('teacher3', '$2a$10$X5wKtYlZq1v7f8h9n0p1r2s3t4u5v6w7x8y9z0a1b2c3d4e5f6g7h8i9j0k1l', '王老师', 'T003', 'wang@lab.edu', '13900139003', 'TEACHER', '机械学院', '机械工程');

-- 添加学生用户
INSERT INTO sys_user (username, password, real_name, student_no, email, phone, role, department, major) VALUES
('student1', '$2a$10$X5wKtYlZq1v7f8h9n0p1r2s3t4u5v6w7x8y9z0a1b2c3d4e5f6g7h8i9j0k1l', '张三', 'S2024001', 'zhangsan@student.edu', '13800138001', 'STUDENT', '计算机学院', '计算机科学与技术'),
('student2', '$2a$10$X5wKtYlZq1v7f8h9n0p1r2s3t4u5v6w7x8y9z0a1b2c3d4e5f6g7h8i9j0k1l', '李四', 'S2024002', 'lisi@student.edu', '13800138002', 'STUDENT', '计算机学院', '软件工程'),
('student3', '$2a$10$X5wKtYlZq1v7f8h9n0p1r2s3t4u5v6w7x8y9z0a1b2c3d4e5f6g7h8i9j0k1l', '王五', 'S2024003', 'wangwu@student.edu', '13800138003', 'STUDENT', '电子学院', '电子信息工程'),
('student4', '$2a$10$X5wKtYlZq1v7f8h9n0p1r2s3t4u5v6w7x8y9z0a1b2c3d4e5f6g7h8i9j0k1l', '赵六', 'S2024004', 'zhaoliu@student.edu', '13800138004', 'STUDENT', '机械学院', '机械工程'),
('student5', '$2a$10$X5wKtYlZq1v7f8h9n0p1r2s3t4u5v6w7x8y9z0a1b2c3d4e5f6g7h8i9j0k1l', '孙七', 'S2024005', 'sunqi@student.edu', '13800138005', 'STUDENT', '化学学院', '化学工程'),
('student6', '$2a$10$X5wKtYlZq1v7f8h9n0p1r2s3t4u5v6w7x8y9z0a1b2c3d4e5f6g7h8i9j0k1l', '周八', 'S2024006', 'zhouba@student.edu', '13800138006', 'STUDENT', '物理学院', '应用物理');

-- ===================== 设备数据 =====================
-- 计算机设备
INSERT INTO equipment (equipment_no, equipment_name, category_id, laboratory_id, brand, model, serial_number, purchase_date, purchase_price, supplier, status, `equipment_condition`, specifications, description) VALUES
('PC001', '联想台式电脑', 7, 1, 'Lenovo', 'ThinkCentre M90t', 'SN2024001', '2024-01-15', 4500.00, '联想', 'NORMAL', 'GOOD', 'Intel i5-12400, 16GB RAM, 512GB SSD, 23.8寸显示器', '计算机实验室主力工作站'),
('PC002', '联想台式电脑', 7, 1, 'Lenovo', 'ThinkCentre M90t', 'SN2024002', '2024-01-15', 4500.00, '联想', 'NORMAL', 'GOOD', 'Intel i5-12400, 16GB RAM, 512GB SSD, 23.8寸显示器', '计算机实验室主力工作站'),
('PC003', '联想台式电脑', 7, 1, 'Lenovo', 'ThinkCentre M90t', 'SN2024003', '2024-01-15', 4500.00, '联想', 'NORMAL', 'GOOD', 'Intel i5-12400, 16GB RAM, 512GB SSD, 23.8寸显示器', '计算机实验室主力工作站'),
('LAPTOP001', '惠普笔记本电脑', 8, 1, 'HP', 'EliteBook 840 G10', 'SN2024011', '2024-02-10', 6800.00, '惠普', 'NORMAL', 'GOOD', 'Intel i7-1365U, 16GB RAM, 512GB SSD, 14寸', '移动办公设备'),
('LAPTOP002', '戴尔笔记本电脑', 8, 1, 'Dell', 'Latitude 7440', 'SN2024012', '2024-02-15', 7200.00, '戴尔', 'BORROWED', 'GOOD', 'Intel i7-1365U, 32GB RAM, 1TB SSD, 14寸', '高性能笔记本'),
('SERVER001', '戴尔服务器', 9, 1, 'Dell', 'PowerEdge R750', 'SN2024021', '2024-03-01', 58000.00, '戴尔', 'NORMAL', 'GOOD', 'Intel Xeon 2x Silver 4314, 128GB RAM, 4TB SAS', '实验室主服务器'),
('SERVER002', '联想服务器', 9, 1, 'Lenovo', 'ThinkSystem SR650', 'SN2024022', '2024-03-05', 52000.00, '联想', 'NORMAL', 'GOOD', 'Intel Xeon 2x Silver 4214, 64GB RAM, 2TB SAS', '测试服务器');

-- 电子仪器
INSERT INTO equipment (equipment_no, equipment_name, category_id, laboratory_id, brand, model, serial_number, purchase_date, purchase_price, supplier, status, `equipment_condition`, specifications, description) VALUES
('OSC001', '泰克示波器', 10, 2, 'Tektronix', 'MDO3024', 'SN2024031', '2024-01-20', 28000.00, '泰克', 'NORMAL', 'GOOD', '4通道, 500MHz, 5GS/s, 混合域示波器', '电子实验室主力示波器'),
('OSC002', '是德示波器', 10, 2, 'Keysight', 'DSOX1204G', 'SN2024032', '2024-02-01', 15800.00, '是德', 'MAINTENANCE', 'GOOD', '4通道, 200MHz, 2GS/s', '教学用示波器'),
('OSC003', '普源示波器', 10, 2, 'Rigol', 'MSO5000', 'SN2024033', '2024-02-15', 12000.00, '普源', 'NORMAL', 'GOOD', '4通道, 350MHz, 4GS/s', '入门级示波器'),
('MUL001', '福禄克万用表', 11, 2, 'Fluke', '87V', 'SN2024041', '2024-01-25', 2200.00, '福禄克', 'NORMAL', 'GOOD', '真有效值, CAT IV 600V, 精度0.05%', '高精度万用表'),
('MUL002', '胜利万用表', 11, 2, 'Victor', 'VC9806+', 'SN2024042', '2024-03-01', 680.00, '胜利', 'NORMAL', 'GOOD', '6000计数, 双显示, 真有效值', '基础万用表'),
('MUL003', '福禄克万用表', 11, 2, 'Fluke', '179', 'SN2024043', '2024-03-10', 1800.00, '福禄克', 'BORROWED', 'GOOD', '真有效值, CAT III 1000V, 背光显示', '便携万用表');

-- 机械设备
INSERT INTO equipment (equipment_no, equipment_name, category_id, laboratory_id, brand, model, serial_number, purchase_date, purchase_price, supplier, status, `equipment_condition`, specifications, description) VALUES
('MAT001', '英斯特朗万能试验机', 12, 3, 'Instron', '5967', 'SN2024051', '2024-02-01', 185000.00, '英斯特朗', 'NORMAL', 'GOOD', '30kN, 单立柱, 智能控制', '材料力学试验'),
('MAT002', '岛津万能试验机', 12, 3, 'Shimadzu', 'AGS-X', 'SN2024052', '2024-02-15', 120000.00, '岛津', 'NORMAL', 'GOOD', '100kN, 双立柱, 触摸屏操作', '高强度材料测试'),
('MAT003', '显微硬度计', 12, 3, 'Wilson', 'VH1202', 'SN2024053', '2024-03-01', 45000.00, 'Wilson', 'NORMAL', 'GOOD', '10g-1000g, 数字显示, 自动压头', '材料硬度测试'),
('MAT004', '金相显微镜', 12, 3, 'Leica', 'DM4 M', 'SN2024054', '2024-03-10', 38000.00, '徕卡', 'NORMAL', 'GOOD', '50x-1000x, LED光源, 数字成像', '金相组织分析'),
('MAT005', '三坐标测量机', 12, 3, 'Hexagon', 'Global Classic', 'SN2024055', '2024-01-20', 280000.00, '海克斯康', 'MAINTENANCE', 'GOOD', '500x500x500mm, 0.003mm精度', '精密测量设备');

-- 化学设备
INSERT INTO equipment (equipment_no, equipment_name, category_id, laboratory_id, brand, model, serial_number, purchase_date, purchase_price, supplier, status, `equipment_condition`, specifications, description) VALUES
('CHEM001', '岛津分光光度计', 13, 4, 'Shimadzu', 'UV-2600i', 'SN2024061', '2024-02-01', 32000.00, '岛津', 'NORMAL', 'GOOD', '185-900nm, 双光束, 自动基线校正', '紫外可见分光光度计'),
('CHEM002', '赛默飞离心机', 13, 4, 'Thermo', 'Sorvall Legend XTR', 'SN2024062', '2024-02-15', 45000.00, '赛默飞', 'NORMAL', 'GOOD', '25000rpm, 4°C, 角转子+水平转子', '高速冷冻离心机'),
('CHEM003', '安捷伦气相色谱仪', 13, 4, 'Agilent', '7890B', 'SN2024063', '2024-03-01', 185000.00, '安捷伦', 'NORMAL', 'GOOD', 'FID检测器, 自动进样器, 柱温箱', '有机物分析'),
('CHEM004', '梅特勒电子天平', 13, 4, 'Mettler', 'XPR105', 'SN2024064', '2024-03-10', 28000.00, '梅特勒', 'NORMAL', 'GOOD', '0.01mg/81g, 防风罩, 触摸屏', '精密称量'),
('CHEM005', '沃特世液相色谱仪', 13, 4, 'Waters', 'ACQUITY H-Class', 'SN2024065', '2024-01-25', 220000.00, '沃特世', 'NORMAL', 'GOOD', '四元泵, 二极管阵列检测器, 自动进样', '液相色谱分析');

-- 物理设备
INSERT INTO equipment (equipment_no, equipment_name, category_id, laboratory_id, brand, model, serial_number, purchase_date, purchase_price, supplier, status, `equipment_condition`, specifications, description) VALUES
('PHY001', 'Tektronix函数发生器', 14, 5, 'Tektronix', 'AFG31000', 'SN2024071', '2024-02-01', 12000.00, '泰克', 'NORMAL', 'GOOD', '双通道, 100MHz, 16bit垂直分辨率', '信号发生器'),
('PHY002', '是德频谱分析仪', 14, 5, 'Keysight', 'N9020B', 'SN2024072', '2024-02-15', 85000.00, '是德', 'NORMAL', 'GOOD', '9kHz-26.5GHz, -166dBm DANL', '频谱分析'),
('PHY003', '罗德矢量网络分析仪', 14, 5, 'Rohde', 'ZNA43', 'SN2024073', '2024-03-01', 380000.00, '罗德', 'NORMAL', 'GOOD', '10MHz-43GHz, 4端口, 双测试端口', '网络分析'),
('PHY004', '泰克逻辑分析仪', 14, 5, 'Tektronix', 'MSO58', 'SN2024074', '2024-03-10', 55000.00, '泰克', 'NORMAL', 'GOOD', '8通道, 2GHz带宽, 触摸屏', '逻辑分析'),
('PHY005', '普源功率计', 14, 5, 'Rigol', 'DSA815', 'SN2024075', '2024-01-20', 6800.00, '普源', 'NORMAL', 'GOOD', '9kHz-1.5GHz, 频谱+网络分析', '功率测量'),
('PHY006', '激光器', 14, 5, 'Coherent', 'Diamond C-100', 'SN2024076', '2024-02-20', 95000.00, '相干', 'MAINTENANCE', 'GOOD', '532nm, 100mW CW, 单模光纤输出', '激光实验'),
('PHY007', '光学平台', 14, 5, 'Thorlabs', 'B48-30', 'SN2024077', '2024-03-05', 18000.00, 'Thorlabs', 'BORROWED', 'GOOD', '1200mmx800mm, 蜂窝孔设计, 橡胶隔振', '光学实验平台'),
('PHY008', '干涉仪', 14, 5, 'Zygo', 'NewView 8300', 'SN2024078', '2024-02-25', 75000.00, 'Zygo', 'NORMAL', 'GOOD', '白光干涉, 3D表面形貌分析', '精密测量');

-- 生物设备
INSERT INTO equipment (equipment_no, equipment_name, category_id, laboratory_id, brand, model, serial_number, purchase_date, purchase_price, supplier, status, `equipment_condition`, specifications, description) VALUES
('BIO001', '赛默飞PCR仪', 15, 1, 'Thermo', 'Veriti 96', 'SN2024081', '2024-02-01', 38000.00, '赛默飞', 'NORMAL', 'GOOD', '96孔, 0.1°C精度, 升温速率6.5°C/s', 'PCR扩增'),
('BIO002', '奥林巴斯显微镜', 15, 1, 'Olympus', 'BX53', 'SN2024082', '2024-02-15', 65000.00, '奥林巴斯', 'NORMAL', 'GOOD', '相差+荧光, 40x-1000x, 数字相机', '生物显微镜'),
('BIO003', '赛默飞培养箱', 15, 1, 'Thermo', '3711', 'SN2024083', '2024-03-01', 28000.00, '赛默飞', 'NORMAL', 'GOOD', 'CO2控制, 温度控制, HEPA过滤', '细胞培养'),
('BIO004', 'BioTek酶标仪', 15, 1, 'BioTek', 'Epoch', 'SN2024084', '2024-03-10', 18000.00, 'BioTek', 'NORMAL', 'GOOD', '200-999nm, 96孔/384孔, 微孔板读取', 'ELISA检测'),
('BIO005', '贝克曼超速离心机', 15, 1, 'Beckman', 'Optima L-100XP', 'SN2024085', '2024-01-25', 185000.00, '贝克曼', 'NORMAL', 'GOOD', '100000rpm, 4°C, 角转子', '超速离心');

-- ===================== 设备借用记录 =====================
INSERT INTO borrow_record (borrow_no, equipment_id, user_id, borrow_date, expected_return_date, actual_return_date, borrow_purpose, borrow_status, handler_id, remark) VALUES
('BR20240220001', 18, 5, '2024-02-20 09:30:00', '2024-02-27 09:30:00', '2024-02-26 14:20:00', '课程设计使用', 'RETURNED', 1, '按时归还'),
('BR20240225001', 15, 6, '2024-02-25 10:00:00', '2024-03-03 10:00:00', NULL, '毕业设计使用', 'BORROWED', 2, '尚未归还'),
('BR20240301001', 21, 7, '2024-03-01 14:30:00', '2024-03-08 14:30:00', '2024-03-08 10:00:00', '实验课使用', 'RETURNED', 2, '提前归还'),
('BR20240305001', 34, 8, '2024-03-05 08:00:00', '2024-03-12 08:00:00', NULL, '科研项目实验', 'BORROWED', 3, '需要延长使用时间'),
('BR20240310001', 39, 9, '2024-03-10 11:00:00', '2024-03-15 11:00:00', '2024-03-14 16:30:00', '课程实验', 'RETURNED', 3, '实验完成提前归还');

-- ===================== 设备预约记录 =====================
INSERT INTO equipment_reservation (reservation_no, equipment_id, user_id, start_time, end_time, purpose, status, approve_user_id, approve_time, approve_remark) VALUES
('RES20240225001', 17, 5, '2024-02-26 09:00:00', '2024-02-26 18:00:00', '课程实验', 'APPROVED', 2, '2024-02-25 15:00:00', '批准'),
('RES20240226001', 18, 6, '2024-02-28 09:00:00', '2024-03-01 18:00:00', '毕业设计', 'APPROVED', 2, '2024-02-26 10:00:00', '批准'),
('RES20240305001', 22, 7, '2024-03-10 09:00:00', '2024-03-12 18:00:00', '科研项目', 'PENDING', NULL, NULL, NULL),
('RES20240306001', 35, 8, '2024-03-15 08:00:00', '2024-03-17 18:00:00', '实验课', 'APPROVED', 3, '2024-03-07 09:00:00', '批准'),
('RES20240308001', 40, 9, '2024-03-20 09:00:00', '2024-03-22 18:00:00', '研究项目', 'COMPLETED', 3, '2024-03-08 14:00:00', '已完成'),
('RES20240310001', 21, 10, '2024-03-25 09:00:00', '2024-03-27 18:00:00', '课程设计', 'PENDING', NULL, NULL, NULL);

-- ===================== 故障报修记录 =====================
INSERT INTO repair_request (repair_no, equipment_id, report_user_id, fault_description, fault_image_urls, report_time, status, priority, handler_id, handle_time, complete_time, repair_description, repair_cost, remark) VALUES
('RR20240301001', 19, 5, '开机时屏幕闪烁，显示不稳定', '/uploads/images/fault_001.jpg,/uploads/images/fault_002.jpg', '2024-03-01 10:00:00', 'COMPLETED', 'NORMAL', 2, '2024-03-01 14:00:00', '2024-03-02 11:30:00', '更换显示排线，问题已解决', 150.00, '更换显示排线'),
('RR20240305001', 23, 6, '按键反应迟钝，有时无反应', NULL, '2024-03-05 09:00:00', 'PROCESSING', 'NORMAL', 2, '2024-03-05 15:00:00', NULL, '正在检查按键电路', NULL, '等待配件'),
('RR20240308001', 44, 7, '激光功率不稳定，输出波动较大', '/uploads/images/fault_003.jpg', '2024-03-08 11:00:00', 'PENDING', 'HIGH', NULL, NULL, NULL, NULL, NULL, '待处理'),
('RR20240312001', 33, 8, '显示屏幕有坏点，影响观察', NULL, '2024-03-12 08:30:00', 'COMPLETED', 'LOW', 3, '2024-03-12 13:00:00', '2024-03-13 10:00:00', '显示屏故障在正常范围内，无需更换', 0.00, '坏点数量在允许范围'),
('RR20240315001', 27, 9, '电机运行时有异响', '/uploads/images/fault_004.jpg', '2024-03-15 14:00:00', 'PROCESSING', 'HIGH', 3, '2024-03-16 09:00:00', NULL, '正在检查电机轴承和润滑情况', NULL, '可能需要更换轴承');

-- ===================== 设备维护计划 =====================
INSERT INTO maintenance_plan (plan_name, equipment_id, category_id, maintenance_type, maintenance_cycle, next_maintenance_date, description, responsible_user_id, is_active) VALUES
('示波器定期维护', NULL, 10, 'MONTHLY', 30, '2024-04-01', '所有示波器每月定期校准和清洁', 2, 1),
('计算机软件更新', NULL, 7, 'MONTHLY', 30, '2024-04-05', '计算机操作系统和安全补丁更新', 2, 1),
('精密仪器年度保养', NULL, NULL, 'YEARLY', 365, '2024-12-01', '所有精密仪器年度全面保养', 3, 1),
('服务器季度维护', 16, 9, 'QUARTERLY', 90, '2024-06-01', '服务器硬件检查和软件升级', 2, 1),
('显微镜日常清洁', NULL, 11, 'DAILY', 1, '2024-03-27', '显微镜镜头清洁和校准', 2, 1),
('离心机月度检查', NULL, 13, 'MONTHLY', 30, '2024-04-10', '离心机转子检查和平衡校准', 2, 1);

-- ===================== 设备维护记录 =====================
INSERT INTO maintenance_record (record_no, plan_id, equipment_id, maintenance_date, maintenance_type, maintenance_content, maintenance_result, maintainer_id, maintenance_duration, cost, parts_used, next_maintenance_date, remark) VALUES
('MR20240301001', 1, 19, '2024-03-01 10:00:00', 'MONTHLY', '示波器校准、清洁接口、检查电源', 'SUCCESS', 2, 60, 0.00, NULL, '2024-04-01', '常规维护'),
('MR20240302001', 2, 16, '2024-03-02 14:00:00', 'QUARTERLY', '服务器硬件检查、系统更新、日志清理', 'SUCCESS', 2, 120, 0.00, NULL, '2024-06-01', '季度维护'),
('MR20240305001', 4, 17, '2024-03-05 09:00:00', 'MONTHLY', '操作系统更新、杀毒扫描、磁盘清理', 'SUCCESS', 2, 90, 0.00, NULL, '2024-04-05', '软件更新'),
('MR20240308001', 1, 20, '2024-03-08 11:00:00', 'MONTHLY', '示波器校准、清洁接口、检查电源', 'SUCCESS', 2, 45, 0.00, NULL, '2024-04-08', '常规维护'),
('MR20240310001', 3, 33, '2024-03-10 08:30:00', 'DAILY', '显微镜镜头清洁、光学对准、检查光源', 'PARTIAL', 3, 30, 150.00, 'LED光源组件', '2024-03-27', '光源亮度略有下降，已更换光源组件');

-- ===================== 操作日志 =====================
INSERT INTO operation_log (user_id, username, operation, module, description, request_method, request_url, ip_address, execute_time, status) VALUES
(1, 'admin', 'ADD', '设备管理', '新增设备 PC001', 'POST', '/api/equipment', '192.168.1.100', 120, 1),
(1, 'admin', 'UPDATE', '设备管理', '更新设备 PC001 信息', 'PUT', '/api/equipment/1', '192.168.1.100', 85, 1),
(1, 'admin', 'DELETE', '设备管理', '删除测试设备', 'DELETE', '/api/equipment/999', '192.168.1.100', 50, 1),
(1, 'admin', 'APPROVE', '预约管理', '批准预约 RES20240225001', 'PUT', '/api/reservation/approve/1', '192.168.1.100', 60, 1),
(2, 'teacher1', 'BORROW', '借用管理', '出库登记 LAPTOP002', 'POST', '/api/borrow', '192.168.1.101', 90, 1),
(2, 'teacher1', 'RETURN', '借用管理', '归还确认 BR20240220001', 'POST', '/api/borrow/return/1', '192.168.1.101', 70, 1),
(3, 'teacher2', 'HANDLE', '报修管理', '处理报修 RR20240305001', 'PUT', '/api/repair/handle/2', '192.168.1.102', 110, 1),
(5, 'student1', 'RESERVE', '预约管理', '提交设备预约', 'POST', '/api/reservation', '192.168.1.200', 80, 1),
(6, 'student2', 'LOGIN', '用户管理', '用户登录', 'POST', '/api/auth/login', '192.168.1.201', 150, 1),
(1, 'admin', 'EXPORT', '设备管理', '导出设备列表', 'GET', '/api/equipment/export', '192.168.1.100', 200, 1);

-- ===================== 通知消息 =====================
INSERT INTO notification (title, content, type, recipient_id, is_read, read_time, related_id) VALUES
('预约审核通过', '您申请的设备预约（编号：RES20240225001）已通过审核，请按时使用。', 'APPROVE', 5, 1, '2024-02-25 15:30:00', 1),
('设备归还提醒', '您借用的设备（编号：BR20240225001）将于2024-03-03到期，请及时归还。', 'OVERDUE', 6, 0, NULL, 2),
('报修进度更新', '您提交的设备报修（编号：RR20240305001）正在处理中，请耐心等待。', 'MAINTENANCE', 6, 1, '2024-03-05 16:00:00', 2),
('新预约待审核', '有新的设备预约（编号：RES20240305001）待审核。', 'SYSTEM', 2, 0, NULL, 3),
('维护提醒', '示波器 OSC002 需要进行月度维护，请安排时间。', 'MAINTENANCE', 2, 1, '2024-03-01 08:00:00', 1),
('系统通知', '实验室设备管理系统已升级，新功能已上线。', 'SYSTEM', 5, 1, '2024-03-10 09:00:00', NULL);

-- ===================== 系统配置补充 =====================
INSERT INTO sys_config (config_key, config_value, config_type, description) VALUES
('system.name', '高校实验室设备智能管理系统', 'STRING', '系统名称'),
('system.version', '1.0.0', 'STRING', '系统版本'),
('reservation.advance.days', '7', 'NUMBER', '预约提前天数'),
('reservation.max.days', '30', 'NUMBER', '最大预约天数'),
('borrow.max.equipment', '3', 'NUMBER', '单人最大借用设备数'),
('notification.email.enabled', 'false', 'BOOLEAN', '是否启用邮件通知'),
('maintenance.auto.create', 'true', 'BOOLEAN', '是否自动创建维护计划');

-- 数据插入完成
SELECT '测试数据插入完成！' AS message;
SELECT COUNT(*) AS user_count FROM sys_user;
SELECT COUNT(*) AS equipment_count FROM equipment;
SELECT COUNT(*) AS category_count FROM equipment_category;
SELECT COUNT(*) AS laboratory_count FROM laboratory;
SELECT COUNT(*) AS borrow_count FROM borrow_record;
SELECT COUNT(*) AS reservation_count FROM equipment_reservation;
SELECT COUNT(*) AS repair_count FROM repair_request;
