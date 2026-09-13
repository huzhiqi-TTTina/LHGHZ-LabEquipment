-- 添加设备唯一标识码字段
ALTER TABLE equipment ADD COLUMN unique_code VARCHAR(50) COMMENT '设备唯一标识码（用于扫码）';

-- 为现有设备生成唯一标识码
UPDATE equipment SET unique_code = CONCAT('EQ', LPAD(id, 8, '0')) WHERE unique_code IS NULL;

-- 添加索引（可选，用于快速查询）
 --CREATE INDEX idx_equipment_unique_code ON equipment(unique_code);
