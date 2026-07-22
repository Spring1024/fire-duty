package com.fireduty.mobile.service;

import com.fireduty.mobile.dto.ScanCheckRequest;
import com.fireduty.mobile.entity.ScanCheckRecord;

import java.util.Map;

public interface MobileService {
    Map<String, Object> sync(String since);
    ScanCheckRecord saveScanCheck(ScanCheckRequest req, Long userId, String userName);
}
