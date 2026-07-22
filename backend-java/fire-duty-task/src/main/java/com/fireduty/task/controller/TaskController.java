package com.fireduty.task.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fireduty.common.annotation.RequirePermission;
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

    @GetMapping
    @RequirePermission(resource = "tasks", action = "read")
    public Result<IPage<TaskDTO>> listTasks(TaskQuery query) {
        return Result.success(taskService.listTasks(query));
    }

    @PostMapping
    @RequirePermission(resource = "tasks", action = "write")
    public Result<TaskDTO> createTask(@RequestBody CreateTaskRequest request) {
        return Result.created(taskService.createTask(request));
    }

    @GetMapping("/{id}")
    @RequirePermission(resource = "tasks", action = "read")
    public Result<TaskDTO> getTask(@PathVariable Long id) {
        return Result.success(taskService.getTask(id));
    }

    @PostMapping("/{id}/submit")
    @RequirePermission(resource = "tasks", action = "write")
    public Result<TaskDTO> submitTask(@PathVariable Long id,
                                       @RequestBody SubmitTaskRequest request) {
        return Result.success(taskService.submitTask(id, request));
    }

    @GetMapping("/templates")
    @RequirePermission(resource = "tasks", action = "read")
    public Result<List<TemplateDTO>> listTemplates() {
        return Result.success(templateService.listTemplates());
    }

    @PostMapping("/templates")
    @RequirePermission(resource = "tasks", action = "write")
    public Result<TemplateDTO> createTemplate(@RequestBody CreateTemplateRequest request) {
        return Result.created(templateService.createTemplate(request));
    }

    @GetMapping("/templates/{id}")
    @RequirePermission(resource = "tasks", action = "read")
    public Result<TemplateDTO> getTemplate(@PathVariable Long id) {
        return Result.success(templateService.getTemplate(id));
    }
}
