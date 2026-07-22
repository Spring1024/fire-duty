package com.fireduty.mobile.service.impl;

import com.fireduty.mobile.entity.WatermarkPhoto;
import com.fireduty.mobile.mapper.WatermarkPhotoMapper;
import com.fireduty.mobile.service.WatermarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 水印照片服务实现。
 * 使用 Java AWT 在图片上叠加文字水印。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WatermarkServiceImpl implements WatermarkService {

    private final WatermarkPhotoMapper watermarkPhotoMapper;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public WatermarkPhoto uploadWithWatermark(MultipartFile file, String deviceCode,
                                               String location, String inspector, Long userId) {
        try {
            // 1. 读取原始图片
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (original == null) {
                throw new RuntimeException("无法解析图片文件");
            }

            // 2. 生成水印图片
            BufferedImage watermarked = addWatermark(original, deviceCode, location, inspector);

            // 3. 保存到文件系统
            String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String filename = String.format("%d_%s_%s.png",
                    System.currentTimeMillis(), deviceCode, file.getOriginalFilename());

            String relativePath = "watermark/" + dateDir + "/" + filename;
            Path fullPath = Paths.get(uploadDir, relativePath);
            Files.createDirectories(fullPath.getParent());

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(watermarked, "png", bos);
            Files.write(fullPath, bos.toByteArray());

            // 4. 保存记录到数据库
            WatermarkPhoto photo = new WatermarkPhoto();
            photo.setDeviceCode(deviceCode);
            photo.setLocation(location);
            photo.setInspector(inspector);
            photo.setFilePath(relativePath);
            photo.setUserId(userId);
            photo.setTakenAt(LocalDateTime.now());
            watermarkPhotoMapper.insert(photo);

            log.info("Watermarked photo saved: {}", relativePath);
            return photo;

        } catch (IOException e) {
            log.error("Failed to process watermark photo", e);
            throw new RuntimeException("水印照片处理失败: " + e.getMessage());
        }
    }

    /**
     * 在图片右下角叠加水印信息。
     * 水印内容：设备编码、位置、检查人、时间
     */
    private BufferedImage addWatermark(BufferedImage image, String deviceCode,
                                        String location, String inspector) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage watermarked = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = watermarked.createGraphics();

        // 绘制原始图片
        g2d.drawImage(image, 0, 0, null);

        // 配置水印样式
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // 水印背景：半透明黑色条
        int fontSize = Math.max(14, width / 40);
        Font font = new Font("SansSerif", Font.BOLD, fontSize);
        g2d.setFont(font);

        String[] lines = {
                "设备: " + deviceCode,
                "位置: " + location,
                "检查人: " + inspector,
                "时间: " + LocalDateTime.now().format(FMT)
        };

        // 计算水印区域尺寸
        FontRenderContext frc = g2d.getFontRenderContext();
        double maxWidth = 0;
        double totalHeight = 0;
        for (String line : lines) {
            Rectangle2D bounds = font.getStringBounds(line, frc);
            maxWidth = Math.max(maxWidth, bounds.getWidth());
            totalHeight += bounds.getHeight() + 6;
        }

        int padding = 12;
        int bgWidth = (int) maxWidth + padding * 2;
        int bgHeight = (int) totalHeight + padding * 2;
        int bgX = width - bgWidth - 15;
        int bgY = height - bgHeight - 15;

        // 绘制半透明背景
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(bgX, bgY, bgWidth, bgHeight, 8, 8);

        // 绘制水印文字
        g2d.setColor(Color.WHITE);
        int textY = bgY + padding + fontSize;
        for (String line : lines) {
            g2d.drawString(line, bgX + padding, textY);
            textY += fontSize + 6;
        }

        g2d.dispose();
        return watermarked;
    }
}
