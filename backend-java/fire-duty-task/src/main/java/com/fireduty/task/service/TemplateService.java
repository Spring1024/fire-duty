package com.fireduty.task.service;

import com.fireduty.task.dto.*;

import java.util.List;

public interface TemplateService {

    /**
     * List all templates.
     */
    List<TemplateDTO> listTemplates();

    /**
     * Get a single template with its items.
     */
    TemplateDTO getTemplate(Long id);

    /**
     * Create a new template with items.
     */
    TemplateDTO createTemplate(CreateTemplateRequest request);
}
