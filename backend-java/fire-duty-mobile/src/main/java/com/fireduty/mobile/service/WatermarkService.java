package com.fireduty.mobile.service;

import com.fireduty.mobile.entity.WatermarkPhoto;
import org.springframework.web.multipart.MultipartFile;

/**
 * 水印照片服务：生成带水印的检查照片。
 */
public interface WatermarkService {

    /**
     * 上传并生成水印照片
     *
     * @param file       原始图片文件
     * @param deviceCode 设备编码
     * @param location   位置信息
     * @param inspector  检查人
     * @param userId     用户ID
     * @return 水印照片记录
     */
    WatermarkPhoto uploadWithWatermark(MultipartFile file, String deviceCode,
                                        String location, String inspector, Long userId);
}
