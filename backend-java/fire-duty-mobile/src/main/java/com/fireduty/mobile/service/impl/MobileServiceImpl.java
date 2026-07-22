package com.fireduty.mobile.service.impl;

import com.fireduty.mobile.dto.ScanCheckRequest;
import com.fireduty.mobile.entity.ScanCheckRecord;
import com.fireduty.mobile.mapper.ScanCheckRecordMapper;
import com.fireduty.mobile.service.MobileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobileServiceImpl implements MobileService {

    private final ScanCheckRecordMapper scanCheckRecordMapper;

    @Override
    public Map<String, Object> sync(String since) {
        // Placeholder for sync logic
        return Map.of(
                "devices", java.util.Collections.emptyList(),
                "tasks", java.util.Collections.emptyList(),
                "rectifications", java.util.Collections.emptyList(),
                "syncTime", LocalDateTime.now().toString()
        );
    }

    @Override
    @Transactional
    public ScanCheckRecord saveScanCheck(ScanCheckRequest req, Long userId, String userName) {
        ScanCheckRecord record = new ScanCheckRecord();
        record.setDeviceCode(req.getDeviceCode());
        record.setStatus(req.getStatus());
        record.setRemark(req.getRemark());
        record.setUserId(userId);
        record.setUserName(userName);
        scanCheckRecordMapper.insert(record);
        log.info("Saved scan check: device={}, status={}", req.getDeviceCode(), req.getStatus());
        return record;
    }
}
