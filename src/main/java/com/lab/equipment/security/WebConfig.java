package com.lab.equipment.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 处理路径：确保使用正斜杠 /，并且去掉末尾多余的斜杠以防拼接重复
        String location = uploadPath.replace("\\", "/");
        if (location.endsWith("/")) {
            location = location.substring(0, location.length() - 1);
        }

        // 2. 构造标准的 file URL: file:///D:/lab-equipment-uploads/
        // 注意这里是 3 个斜杠 file:///
        String resourceLocation = "file:///" + location + "/";

        System.out.println(">>> [WebConfig] 注册映射：/uploads/**  ->  " + resourceLocation);

        // 3. 注册上传文件映射
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);

        // 4. H5静态页面映射（classpath下的static目录）
        System.out.println(">>> [WebConfig] 注册映射：/h5/**  ->  classpath:/static/h5/");
        registry.addResourceHandler("/h5/**")
                .addResourceLocations("classpath:/static/h5/");
    }
    /*public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 直接写死你的绝对路径，注意 Windows 要用 file:/// 且斜杠要正
        String hardCodePath = "file:///D:/lab-equipment-uploads/";

        System.out.println(">>> [硬编码测试] 尝试映射：/uploads/** 和 /api/uploads/** 到 " + hardCodePath);

        // 同时映射 /uploads/** 和 /api/uploads/**（因为 context-path 是 /api）
        registry.addResourceHandler("/uploads/**", "/api/uploads/**")
                .addResourceLocations(hardCodePath);
    }*/
}
