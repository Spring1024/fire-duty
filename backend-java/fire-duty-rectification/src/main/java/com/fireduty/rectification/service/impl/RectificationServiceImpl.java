package com.fireduty.rectification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fireduty.common.exception.BusinessException;
import com.fireduty.common.exception.ResourceNotFoundException;
import com.fireduty.rectification.dto.PhotoDTO;
import com.fireduty.rectification.dto.RectificationDTO;
import com.fireduty.rectification.dto.RectificationQuery;
import com.fireduty.rectification.dto.TimelineDTO;
import com.fireduty.rectification.entity.Rectification;
import com.fireduty.rectification.entity.RectificationPhoto;
import com.fireduty.rectification.entity.RectificationTimeline;
import com.fireduty.rectification.mapper.RectificationMapper;
import com.fireduty.rectification.mapper.RectificationPhotoMapper;
import com.fireduty.rectification.mapper.RectificationTimelineMapper;
import com.fireduty.rectification.service.RectificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class RectificationServiceImpl implements RectificationService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RectificationMapper rectificationMapper;
    private final RectificationTimelineMapper timelineMapper;
    private final RectificationPhotoMapper photoMapper;

    @Override
    public Page<Rectification> list(RectificationQuery query) {
        LambdaQueryWrapper<Rectification> wrapper = new LambdaQueryWrapper<>();
        if (query.getTab() != null && !query.getTab().isBlank()) {
            wrapper.eq(Rectification::getStatus, statusFromTab(query.getTab()));
        }
        wrapper.orderByDesc(Rectification::getId);
        return rectificationMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);
    }

    @Override
    public RectificationDTO getDetail(Long id) {
        Rectification rect = rectificationMapper.selectById(id);
        if (rect == null) {
            throw new ResourceNotFoundException("整改单不存在");
        }
        List<RectificationTimeline> timelines = timelineMapper.selectList(
                new LambdaQueryWrapper<RectificationTimeline>()
                        .eq(RectificationTimeline::getRectId, id)
                        .orderByAsc(RectificationTimeline::getCreatedAt));
        List<RectificationPhoto> photos = photoMapper.selectList(
                new LambdaQueryWrapper<RectificationPhoto>()
                        .eq(RectificationPhoto::getRectId, id));
        return toDTO(rect, timelines, photos);
    }

    @Override
    @Transactional
    public Rectification dispatch(Long id) {
        Rectification rect = rectificationMapper.selectById(id);
        if (rect == null) throw new ResourceNotFoundException("整改单不存在");
        if (!"待派发".equals(rect.getStatus())) {
            throw new BusinessException("当前状态不可派发，仅待派发状态的整改单可派发");
        }
        rect.setStatus("整改中");
        rect.setUpdatedAt(LocalDateTime.now());
        rectificationMapper.updateById(rect);

        addTimeline(rect.getId(), "派发", null, "已派发给责任人");
        log.info("Rectification {} dispatched", id);
        return rect;
    }

    @Override
    @Transactional
    public Rectification submitFix(Long id, String comment) {
        Rectification rect = rectificationMapper.selectById(id);
        if (rect == null) throw new ResourceNotFoundException("整改单不存在");
        if (!"整改中".equals(rect.getStatus())) {
            throw new BusinessException("当前状态不可提交整改");
        }
        rect.setStatus("待复核");
        rect.setUpdatedAt(LocalDateTime.now());
        rectificationMapper.updateById(rect);

        addTimeline(rect.getId(), "整改提交", null, comment);
        log.info("Rectification {} submitted for review", id);
        return rect;
    }

    @Override
    @Transactional
    public Rectification review(Long id, boolean approved, String comment) {
        Rectification rect = rectificationMapper.selectById(id);
        if (rect == null) throw new ResourceNotFoundException("整改单不存在");
        if (!"待复核".equals(rect.getStatus())) {
            throw new BusinessException("当前状态不可复核");
        }
        if (approved) {
            rect.setStatus("已闭环");
            rect.setClosedTime(LocalDateTime.now());
            addTimeline(rect.getId(), "复核通过", null, comment);
        } else {
            rect.setStatus("整改中");
            addTimeline(rect.getId(), "复核驳回", null, comment);
        }
        rect.setUpdatedAt(LocalDateTime.now());
        rectificationMapper.updateById(rect);
        log.info("Rectification {} reviewed, approved={}", id, approved);
        return rect;
    }

    @Override
    @Transactional
    public Rectification uploadPhoto(Long id, String type, String url) {
        Rectification rect = rectificationMapper.selectById(id);
        if (rect == null) throw new ResourceNotFoundException("整改单不存在");

        RectificationPhoto photo = new RectificationPhoto();
        photo.setRectId(id);
        photo.setPhotoType(type);
        photo.setUrl(url);
        photo.setTakenAt(LocalDateTime.now());
        photoMapper.insert(photo);
        return rect;
    }

    @Override
    @Transactional
    public Rectification archive(Long id) {
        Rectification rect = rectificationMapper.selectById(id);
        if (rect == null) throw new ResourceNotFoundException("整改单不存在");
        if (!"已闭环".equals(rect.getStatus())) {
            throw new BusinessException("仅已闭环状态的整改单可归档");
        }
        rect.setStatus("已归档");
        rect.setUpdatedAt(LocalDateTime.now());
        rectificationMapper.updateById(rect);
        addTimeline(rect.getId(), "归档", null, "整改单已归档");
        log.info("Rectification {} archived", id);
        return rect;
    }

    @Override
    @Transactional
    public Rectification escalate(Long id) {
        Rectification rect = rectificationMapper.selectById(id);
        if (rect == null) throw new ResourceNotFoundException("整改单不存在");
        // 超时升级：标识超时升级到上一级
        addTimeline(rect.getId(), "超时升级", null, "整改超时48h，已升级到上级网格负责人处理");
        log.info("Rectification {} escalated to superior", id);
        return rect;
    }

    /**
     * 定时任务：每30分钟检查超时整改单，标记已超时
     * 整改中超过48h未完成的自动升级
     */
    @Scheduled(fixedRate = 1800000) // 30分钟
    @Transactional
    public void checkTimeouts() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline48hAgo = now.minusHours(48);

        // 1) 查找已超期的整改单（待派发/整改中/待复核 且 deadline已过）
        List<Rectification> overdueRects = rectificationMapper.selectList(
                new LambdaQueryWrapper<Rectification>()
                        .in(Rectification::getStatus, "待派发", "整改中", "待复核")
                        .isNotNull(Rectification::getDeadline)
                        .lt(Rectification::getDeadline, now));

        for (Rectification rect : overdueRects) {
            if (!"已超时".equals(rect.getStatus())) {
                rect.setStatus("已超时");
                rect.setUpdatedAt(now);
                rectificationMapper.updateById(rect);
                addTimeline(rect.getId(), "系统自动超时", null, "整改期限已过，系统自动标记为已超时");
                log.info("Rectification {} auto-timed out", rect.getId());
            }
        }

        // 2) 查找超时超过48h的整改单，自动升级
        List<Rectification> escalateRects = rectificationMapper.selectList(
                new LambdaQueryWrapper<Rectification>()
                        .eq(Rectification::getStatus, "已超时")
                        .isNotNull(Rectification::getDeadline)
                        .lt(Rectification::getDeadline, deadline48hAgo));

        for (Rectification rect : escalateRects) {
            escalate(rect.getId());
        }
    }

    private void addTimeline(Long rectId, String action, Long operatorId, String comment) {
        RectificationTimeline tl = new RectificationTimeline();
        tl.setRectId(rectId);
        tl.setAction(action);
        tl.setOperatorId(operatorId);
        tl.setComment(comment);
        timelineMapper.insert(tl);
    }

    private RectificationDTO toDTO(Rectification rect, List<RectificationTimeline> timelines,
                                   List<RectificationPhoto> photos) {
        RectificationDTO dto = new RectificationDTO();
        dto.setId(rect.getId());
        dto.setDescription(rect.getDescription());
        dto.setLevel(rect.getLevel());
        dto.setLevelType("紧急".equals(rect.getLevel()) ? "danger" : "warning");
        dto.setStatus(rect.getStatus());
        dto.setFoundTime(rect.getFoundTime() != null ? rect.getFoundTime().toString() : null);
        dto.setDeadline(rect.getDeadline() != null ? rect.getDeadline().format(FMT) : null);
        dto.setCreatedAt(rect.getCreatedAt() != null ? rect.getCreatedAt().format(FMT) : null);
        dto.setUpdatedAt(rect.getUpdatedAt() != null ? rect.getUpdatedAt().format(FMT) : null);
        dto.setTimeline(timelines.stream().map(t -> {
            TimelineDTO td = new TimelineDTO();
            td.setId(t.getId());
            td.setAction(t.getAction());
            td.setComment(t.getComment());
            td.setTimestamp(t.getCreatedAt() != null ? t.getCreatedAt().format(FMT) : null);
            return td;
        }).collect(Collectors.toList()));
        dto.setPhotos(photos.stream().map(p -> {
            PhotoDTO pd = new PhotoDTO();
            pd.setId(p.getId());
            pd.setType(p.getPhotoType());
            pd.setUrl(p.getUrl());
            pd.setTakenAt(p.getTakenAt() != null ? p.getTakenAt().format(FMT) : null);
            return pd;
        }).collect(Collectors.toList()));
        return dto;
    }

    private String statusFromTab(String tab) {
        return switch (tab) {
            case "pending" -> "待派发";
            case "ongoing" -> "整改中";
            case "review" -> "待复核";
            case "closed" -> "已闭环";
            default -> tab;
        };
    }
}
