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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    @Value("${qr.code.public.url:}")
    private String publicUrl;

    @Value("${server.port:8080}")
    private int serverPort;

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
        String base = resolveBaseUrl();
        log.info("二维码基地址: {}", base);
        return base + "/api/h5/equipment.html?id=" + equipmentId;
    }

    /**
     * 解析二维码里使用的基地址：优先自动探测本机内网 IP，失败时退回配置值
     */
    public String resolveBaseUrl() {
        // 公网部署时用配置的公网地址覆盖，避免取到服务器内网 IP
        if (publicUrl != null && !publicUrl.isBlank()) {
            String pub = publicUrl.trim();
            return pub.endsWith("/") ? pub.substring(0, pub.length() - 1) : pub;
        }

        String ip = detectLanIp();
        String base = (ip != null && !ip.isEmpty())
                ? "http://" + ip + ":" + serverPort
                : baseUrl;
        if (base != null && base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    /**
     * 探测本机内网 IPv4：跳过回环、169.254 以及 VPN/TUN 等虚拟网卡
     */
    private String detectLanIp() {
        String fallback = null;
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface ni : interfaces) {
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }

                String name = (ni.getName() + " " + ni.getDisplayName()).toLowerCase();
                if (name.matches(".*(vmware|virtualbox|vethernet|hyper-v|tap|tun|sing|clash|bluetooth|mcafee|vpn|loopback).*")) {
                    continue;
                }

                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    InetAddress addr = ia.getAddress();
                    if (!(addr instanceof Inet4Address)) {
                        continue;
                    }
                    if (addr.isLoopbackAddress() || addr.isLinkLocalAddress() || !addr.isSiteLocalAddress()) {
                        continue;
                    }

                    String ip = addr.getHostAddress();
                    if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
                        return ip;
                    }
                    if (fallback == null) {
                        fallback = ip;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("自动探测本机IP失败: {}", e.getMessage());
        }
        return fallback;
    }

    /**
     * 生成设备唯一标识码
     */
    public String generateUniqueCode(Long equipmentId) {
        return "EQ" + String.format("%08d", equipmentId);
    }
}
