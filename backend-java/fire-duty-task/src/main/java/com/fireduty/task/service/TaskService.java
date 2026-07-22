package com.fireduty.task.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fireduty.task.dto.*;

public interface TaskService {

    /**
     * List tasks with pagination and optional tab filtering.
     */
    IPage<TaskDTO> listTasks(TaskQuery query);

    /**
     * Get a single task by id, including results if completed.
     */
    TaskDTO getTask(Long id);

    /**
     * Create a new inspection task.
     */
    TaskDTO createTask(CreateTaskRequest request);

    /**
     * Submit (complete) a task with inspection results.
     */
    TaskDTO submitTask(Long id, SubmitTaskRequest request);
}
