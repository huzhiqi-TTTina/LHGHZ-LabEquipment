package com.lab.equipment.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码工具类
 */
@Slf4j
@Component
public class QrCodeUtil {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${qr.code.base.url}")
    private String baseUrl;

    private static final int QR_CODE_SIZE = 300;
    private static final String FORMAT = "PNG";

    /**
     * 生成二维码并保存到文件
     */
    public String generateQrCode(String content, String fileName) throws Exception {
        // 创建上传目录
        Path uploadDir = Paths.get(uploadPath, "qrcode");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 生成二维码矩阵
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);

        // 保存到文件
        Path filePath = uploadDir.resolve(fileName);
        MatrixToImageWriter.writeToPath(bitMatrix, FORMAT, filePath);

        log.info("二维码文件已生成: {}", filePath.toAbsolutePath());
        log.info("返回的访问URL: {}", "/uploads/qrcode/" + fileName);

        // 返回访问URL
        return "/uploads/qrcode/" + fileName;
    }

    /**
     * 生成二维码字节数组
     */
    public byte[] generateQrCodeBytes(String content) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints);

        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, FORMAT, outputStream);
        return outputStream.toByteArray();
    }

    /**
     * 读取二维码内容
     */
    public String readQrCode(File file) throws Exception {
        BufferedImage bufferedImage = ImageIO.read(file);
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        Binarizer binarizer = new HybridBinarizer(source);
        BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);

        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

        Result result = new MultiFormatReader().decode(binaryBitmap, hints);
        return result.getText();
    }

    /**
     * 生成设备二维码URL（H5页面）
     */
    public String generateEquipmentQrCodeUrl(Long equipmentId) {
        // 注意：context-path是/api，所以H5页面完整路径需要包含/api
        return baseUrl + "/api/h5/equipment.html?id=" + equipmentId;
    }

    /**
     * 生成设备唯一标识码
     */
    public String generateUniqueCode(Long equipmentId) {
        return "EQ" + String.format("%08d", equipmentId);
    }
}
