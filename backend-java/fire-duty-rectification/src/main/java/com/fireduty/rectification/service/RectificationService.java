package com.fireduty.rectification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fireduty.rectification.dto.RectificationDTO;
import com.fireduty.rectification.dto.RectificationQuery;
import com.fireduty.rectification.entity.Rectification;

public interface RectificationService {
    Page<Rectification> list(RectificationQuery query);
    RectificationDTO getDetail(Long id);
    Rectification dispatch(Long id);
    Rectification submitFix(Long id, String comment);
    Rectification review(Long id, boolean approved, String comment);
    Rectification uploadPhoto(Long id, String type, String url);
}
