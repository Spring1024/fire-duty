package com.fireduty.task.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fireduty.common.response.Result;
import com.fireduty.task.dto.*;
import com.fireduty.task.service.TaskService;
import com.fireduty.task.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final TemplateService templateService;

    // ==================== Task Endpoints ====================

    /**
     * List tasks with pagination and filter (tab support).
     * GET /tasks?page=1&pageSize=10&status=pending&priority=high&keyword=xxx
     */
    @GetMapping
    public Result<IPage<TaskDTO>> listTasks(TaskQuery query) {
        return Result.success(taskService.listTasks(query));
    }

    /**
     * Create a new task.
     */
    @PostMapping
    public Result<TaskDTO> createTask(@RequestBody CreateTaskRequest request) {
        return Result.created(taskService.createTask(request));
    }

    /**
     * Get a single task detail, including results if completed.
     */
    @GetMapping("/{id}")
    public Result<TaskDTO> getTask(@PathVariable Long id) {
        return Result.success(taskService.getTask(id));
    }

    /**
     * Submit (complete) a task with inspection result values.
     */
    @PostMapping("/{id}/submit")
    public Result<TaskDTO> submitTask(@PathVariable Long id,
                                       @RequestBody SubmitTaskRequest request) {
        return Result.success(taskService.submitTask(id, request));
    }

    // ==================== Template Endpoints ====================

    /**
     * List all inspection templates.
     */
    @GetMapping("/templates")
    public Result<List<TemplateDTO>> listTemplates() {
        return Result.success(templateService.listTemplates());
    }

    /**
     * Create a new inspection template with items.
     */
    @PostMapping("/templates")
    public Result<TemplateDTO> createTemplate(@RequestBody CreateTemplateRequest request) {
        return Result.created(templateService.createTemplate(request));
    }

    /**
     * Get a single template with its items.
     */
    @GetMapping("/templates/{id}")
    public Result<TemplateDTO> getTemplate(@PathVariable Long id) {
        return Result.success(templateService.getTemplate(id));
    }
}
