package com.fireduty.task.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fireduty.common.exception.ResourceNotFoundException;
import com.fireduty.task.dto.*;
import com.fireduty.task.entity.InspectionResult;
import com.fireduty.task.entity.InspectionTask;
import com.fireduty.task.mapper.ResultMapper;
import com.fireduty.task.mapper.TaskMapper;
import com.fireduty.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskMapper taskMapper;
    private final ResultMapper resultMapper;

    @Override
    public IPage<TaskDTO> listTasks(TaskQuery query) {
        Page<InspectionTask> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<InspectionTask> wrapper = new LambdaQueryWrapper<InspectionTask>()
                .like(StrUtil.isNotBlank(query.getKeyword()), InspectionTask::getTitle, query.getKeyword())
                .eq(StrUtil.isNotBlank(query.getStatus()), InspectionTask::getStatus, query.getStatus())
                .eq(StrUtil.isNotBlank(query.getPriority()), InspectionTask::getPriority, query.getPriority())
                .eq(query.getTemplateId() != null, InspectionTask::getTemplateId, query.getTemplateId())
                .eq(StrUtil.isNotBlank(query.getAssignedTo()), InspectionTask::getAssignedTo, query.getAssignedTo())
                .orderByDesc(InspectionTask::getCreatedAt);

        IPage<InspectionTask> taskPage = taskMapper.selectPage(page, wrapper);

        return taskPage.convert(this::toDTO);
    }

    @Override
    public TaskDTO getTask(Long id) {
        InspectionTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new ResourceNotFoundException("巡检任务不存在: " + id);
        }
        TaskDTO dto = toDTO(task);

        // Load results if task is completed
        if ("completed".equals(task.getStatus())) {
            List<InspectionResult> results = resultMapper.selectList(
                    new LambdaQueryWrapper<InspectionResult>()
                            .eq(InspectionResult::getTaskId, id));
            dto.setResults(results.stream().map(this::toResultDTO).collect(Collectors.toList()));
        }

        return dto;
    }

    @Override
    @Transactional
    public TaskDTO createTask(CreateTaskRequest request) {
        InspectionTask task = new InspectionTask();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : "draft");
        task.setPriority(request.getPriority() != null ? request.getPriority() : "medium");
        task.setTemplateId(request.getTemplateId());
        task.setAssignedTo(request.getAssignedTo());
        task.setLocation(request.getLocation());
        task.setScheduledDate(request.getScheduledDate());
        task.setCreatedBy(request.getCreatedBy());

        taskMapper.insert(task);
        return toDTO(task);
    }

    @Override
    @Transactional
    public TaskDTO submitTask(Long id, SubmitTaskRequest request) {
        InspectionTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new ResourceNotFoundException("巡检任务不存在: " + id);
        }

        // Mark as completed
        task.setStatus("completed");
        task.setCompletedDate(LocalDateTime.now());
        taskMapper.updateById(task);

        // Save inspection results
        if (request.getItems() != null) {
            for (SubmitTaskRequest.ItemResult item : request.getItems()) {
                InspectionResult result = new InspectionResult();
                result.setTaskId(id);
                result.setTemplateItemId(item.getTemplateItemId());
                result.setValue(item.getValue());
                result.setImageUrl(item.getImageUrl());
                result.setRemark(item.getRemark() != null ? item.getRemark() : request.getRemark());
                result.setCreatedBy(request.getCompletedBy());
                resultMapper.insert(result);
            }
        }

        return getTask(id);
    }

    // ---- internal helpers ----

    private TaskDTO toDTO(InspectionTask task) {
        if (task == null) return null;
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setTemplateId(task.getTemplateId());
        dto.setAssignedTo(task.getAssignedTo());
        dto.setLocation(task.getLocation());
        dto.setScheduledDate(task.getScheduledDate());
        dto.setCompletedDate(task.getCompletedDate());
        dto.setCreatedBy(task.getCreatedBy());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        return dto;
    }

    private InspectionResultDTO toResultDTO(InspectionResult result) {
        if (result == null) return null;
        InspectionResultDTO dto = new InspectionResultDTO();
        dto.setId(result.getId());
        dto.setTaskId(result.getTaskId());
        dto.setTemplateItemId(result.getTemplateItemId());
        dto.setValue(result.getValue());
        dto.setImageUrl(result.getImageUrl());
        dto.setRemark(result.getRemark());
        dto.setCreatedBy(result.getCreatedBy());
        dto.setCreatedAt(result.getCreatedAt());
        return dto;
    }
}
