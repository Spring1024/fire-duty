package com.fireduty.mobile.controller;

import com.fireduty.common.response.Result;
import com.fireduty.mobile.dto.ScanCheckRequest;
import com.fireduty.mobile.entity.ScanCheckRecord;
import com.fireduty.mobile.service.MobileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/mobile")
@RequiredArgsConstructor
public class MobileController {

    private final MobileService mobileService;

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
}
