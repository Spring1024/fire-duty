package com.fireduty.task.service.impl;

import com.fireduty.common.exception.ResourceNotFoundException;
import com.fireduty.task.dto.CreateTemplateRequest;
import com.fireduty.task.dto.TemplateDTO;
import com.fireduty.task.entity.InspectionTemplate;
import com.fireduty.task.entity.InspectionTemplateItem;
import com.fireduty.task.mapper.TemplateItemMapper;
import com.fireduty.task.mapper.TemplateMapper;
import com.fireduty.task.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateMapper templateMapper;
    private final TemplateItemMapper templateItemMapper;

    @Override
    public List<TemplateDTO> listTemplates() {
        List<InspectionTemplate> templates = templateMapper.selectList(null);
        return templates.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public TemplateDTO getTemplate(Long id) {
        InspectionTemplate template = templateMapper.selectById(id);
        if (template == null) {
            throw new ResourceNotFoundException("巡检模板不存在: " + id);
        }
        return toDTOWithItems(template);
    }

    @Override
    @Transactional
    public TemplateDTO createTemplate(CreateTemplateRequest request) {
        InspectionTemplate template = new InspectionTemplate();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setCategory(request.getCategory());
        template.setCreatedBy(request.getCreatedBy());
        templateMapper.insert(template);

        // Save template items
        if (request.getItems() != null) {
            for (CreateTemplateRequest.ItemDef itemDef : request.getItems()) {
                InspectionTemplateItem item = new InspectionTemplateItem();
                item.setTemplateId(template.getId());
                item.setName(itemDef.getName());
                item.setDescription(itemDef.getDescription());
                item.setType(itemDef.getType() != null ? itemDef.getType() : "checkbox");
                item.setRequired(itemDef.getRequired() != null ? itemDef.getRequired() : true);
                item.setSortOrder(itemDef.getSortOrder() != null ? itemDef.getSortOrder() : 0);
                templateItemMapper.insert(item);
            }
        }

        return toDTOWithItems(template);
    }

    // ---- internal helpers ----

    private TemplateDTO toDTO(InspectionTemplate template) {
        if (template == null) return null;
        TemplateDTO dto = new TemplateDTO();
        dto.setId(template.getId());
        dto.setName(template.getName());
        dto.setDescription(template.getDescription());
        dto.setCategory(template.getCategory());
        dto.setCreatedBy(template.getCreatedBy());
        dto.setCreatedAt(template.getCreatedAt());
        dto.setUpdatedAt(template.getUpdatedAt());
        dto.setItems(Collections.emptyList());
        return dto;
    }

    private TemplateDTO toDTOWithItems(InspectionTemplate template) {
        TemplateDTO dto = toDTO(template);
        List<InspectionTemplateItem> items = templateItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<InspectionTemplateItem>()
                        .eq(InspectionTemplateItem::getTemplateId, template.getId())
                        .orderByAsc(InspectionTemplateItem::getSortOrder));
        dto.setItems(items.stream().map(this::toItemDTO).collect(Collectors.toList()));
        return dto;
    }

    private TemplateDTO.TemplateItemDTO toItemDTO(InspectionTemplateItem item) {
        if (item == null) return null;
        TemplateDTO.TemplateItemDTO dto = new TemplateDTO.TemplateItemDTO();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setType(item.getType());
        dto.setRequired(item.getRequired());
        dto.setSortOrder(item.getSortOrder());
        return dto;
    }
}
