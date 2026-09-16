package com.lab.equipment;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;  // ← 新增：导入加密类

/**
 * 实验室设备智能管理系统 - 应用启动类
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.lab.equipment.mapper")
public class LabEquipmentApplication {

    public static void main(String[] args) {
        // ↓↓↓ 新增：生成 BCrypt 加密密码（共6行）↓↓↓
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("admin123");
        System.out.println("========================================");
        System.out.println("admin123 加密后的密码是：" + encodedPassword);
        System.out.println("========================================");
        // ↑↑↑ 新增结束 ↑↑↑

        SpringApplication.run(LabEquipmentApplication.class, args);
        System.out.println("========================================");
        System.out.println("实验室设备智能管理系统启动成功！");
        System.out.println("========================================");
    }
}