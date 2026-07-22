package com.fireduty.mobile.controller;

import com.fireduty.common.response.Result;
import com.fireduty.mobile.dto.ScanCheckRequest;
import com.fireduty.mobile.entity.ScanCheckRecord;
import com.fireduty.mobile.service.MobileService;
import com.fireduty.mobile.service.WatermarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/mobile")
@RequiredArgsConstructor
public class MobileController {

    private final MobileService mobileService;
    private final WatermarkService watermarkService;

    @GetMapping("/sync")
    public Result<Map<String, Object>> sync(@RequestParam(required = false) String since) {
        return Result.success(mobileService.sync(since));
    }

    @PostMapping("/scan-check")
    public Result<ScanCheckRecord> scanCheck(@RequestBody ScanCheckRequest req,
                                             @RequestHeader("X-User-Id") Long userId,
                                             @RequestHeader("X-User-Name") String userName) {
        return Result.created(mobileService.saveScanCheck(req, userId, userName));
    }

    /**
     * 上传水印照片
     * POST /mobile/photo
     * 表单字段: image (file), deviceCode, location, inspector
     */
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<?> uploadPhoto(
            @RequestParam("image") MultipartFile file,
            @RequestParam("deviceCode") String deviceCode,
            @RequestParam("location") String location,
            @RequestParam("inspector") String inspector,
            @RequestHeader("X-User-Id") Long userId) {

        var photo = watermarkService.uploadWithWatermark(file, deviceCode, location, inspector, userId);
        return Result.created(Map.of(
                "id", photo.getId(),
                "filePath", photo.getFilePath(),
                "takenAt", photo.getTakenAt()
        ));
    }
}
