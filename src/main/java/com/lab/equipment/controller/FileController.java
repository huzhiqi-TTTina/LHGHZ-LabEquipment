package com.lab.equipment.controller;

import com.lab.equipment.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    @Value("${file.upload.path}")
    private String uploadPath;

    /**
     * 单文件上传
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择文件");
        }

        try {
            // 创建上传目录
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            Files.write(filePath, file.getBytes());

            // 返回访问URL
            String fileUrl = "/uploads/" + filename;
            return Result.success("上传成功", fileUrl);

        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 多文件上传
     */
    @PostMapping("/uploads")
    public Result<List<String>> uploads(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error("请选择文件");
        }

        List<String> fileUrls = new ArrayList<>();

        try {
            // 创建上传目录
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // 生成文件名
                    String originalFilename = file.getOriginalFilename();
                    String extension = originalFilename != null ?
                            originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
                    String filename = UUID.randomUUID().toString() + extension;

                    // 保存文件
                    Path filePath = uploadDir.resolve(filename);
                    Files.write(filePath, file.getBytes());

                    // 返回访问URL
                    String fileUrl = "/uploads/" + filename;
                    fileUrls.add(fileUrl);
                }
            }

            return Result.success("上传成功", fileUrls);

        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
