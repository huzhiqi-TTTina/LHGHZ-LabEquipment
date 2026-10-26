package com.lab.equipment.controller;

import com.lab.equipment.entity.EquipmentQrImage;
import com.lab.equipment.mapper.EquipmentQrImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * 设备二维码图片读取（图片存库，不依赖本地磁盘）
 */
@RestController
@RequestMapping("/qrcode")
@RequiredArgsConstructor
public class QrCodeImageController {

    private final EquipmentQrImageMapper qrImageMapper;

    @GetMapping("/{equipmentId}")
    public ResponseEntity<byte[]> getQrImage(@PathVariable Long equipmentId) {
        EquipmentQrImage img = qrImageMapper.selectById(equipmentId);
        if (img == null || img.getImageData() == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType type = img.getContentType() == null
                ? MediaType.IMAGE_PNG
                : MediaType.parseMediaType(img.getContentType());

        return ResponseEntity.ok()
                .contentType(type)
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                .body(img.getImageData());
    }
}
