-- 添加最后完成日期字段到维护计划表
ALTER TABLE `maintenance_plan` ADD COLUMN `last_completed_date` DATE COMMENT '最后完成日期' AFTER `description';
