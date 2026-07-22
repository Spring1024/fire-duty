package com.fireduty.rectification.controller;

import com.fireduty.common.response.Result;
import com.fireduty.rectification.dto.RectificationDTO;
import com.fireduty.rectification.dto.RectificationQuery;
import com.fireduty.rectification.entity.Rectification;
import com.fireduty.rectification.service.RectificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rectifications")
@RequiredArgsConstructor
public class RectificationController {

    private final RectificationService rectificationService;

    @GetMapping
    public Result<?> list(RectificationQuery query) {
        return Result.success(rectificationService.list(query));
    }

    @GetMapping("/{id}")
    public Result<RectificationDTO> get(@PathVariable Long id) {
        return Result.success(rectificationService.getDetail(id));
    }

    @PutMapping("/{id}/dispatch")
    public Result<Rectification> dispatch(@PathVariable Long id) {
        return Result.success(rectificationService.dispatch(id));
    }

    @PutMapping("/{id}/submit-fix")
    public Result<Rectification> submitFix(@PathVariable Long id, @RequestBody SubmitFixRequest req) {
        return Result.success(rectificationService.submitFix(id, req.comment()));
    }

    @PutMapping("/{id}/review")
    public Result<Rectification> review(@PathVariable Long id, @RequestBody ReviewRequest req) {
        return Result.success(rectificationService.review(id, req.approved(), req.comment()));
    }

    @PostMapping("/{id}/photos")
    public Result<Rectification> uploadPhoto(@PathVariable Long id, @RequestBody PhotoRequest req) {
        return Result.success(rectificationService.uploadPhoto(id, req.type(), req.url()));
    }

    // Inner request classes as Java records — accessors are field-name(), not getField()
    public record SubmitFixRequest(String comment) {}
    public record ReviewRequest(boolean approved, String comment) {}
    public record PhotoRequest(String type, String url) {}
}
