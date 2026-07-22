package com.fireduty.rectification.controller;

import com.fireduty.common.annotation.RequirePermission;
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
    @RequirePermission(resource = "rectifications", action = "read")
    public Result<?> list(RectificationQuery query) {
        return Result.success(rectificationService.list(query));
    }

    @GetMapping("/{id}")
    @RequirePermission(resource = "rectifications", action = "read")
    public Result<RectificationDTO> get(@PathVariable Long id) {
        return Result.success(rectificationService.getDetail(id));
    }

    @PutMapping("/{id}/dispatch")
    @RequirePermission(resource = "rectifications", action = "write")
    public Result<Rectification> dispatch(@PathVariable Long id) {
        return Result.success(rectificationService.dispatch(id));
    }

    @PutMapping("/{id}/submit-fix")
    @RequirePermission(resource = "rectifications", action = "write")
    public Result<Rectification> submitFix(@PathVariable Long id, @RequestBody SubmitFixRequest req) {
        return Result.success(rectificationService.submitFix(id, req.comment()));
    }

    @PutMapping("/{id}/review")
    @RequirePermission(resource = "rectifications", action = "write")
    public Result<Rectification> review(@PathVariable Long id, @RequestBody ReviewRequest req) {
        return Result.success(rectificationService.review(id, req.approved(), req.comment()));
    }

    @PostMapping("/{id}/photos")
    @RequirePermission(resource = "rectifications", action = "write")
    public Result<Rectification> uploadPhoto(@PathVariable Long id, @RequestBody PhotoRequest req) {
        return Result.success(rectificationService.uploadPhoto(id, req.type(), req.url()));
    }

    @PutMapping("/{id}/archive")
    @RequirePermission(resource = "rectifications", action = "write")
    public Result<Rectification> archive(@PathVariable Long id) {
        return Result.success(rectificationService.archive(id));
    }

    public record SubmitFixRequest(String comment) {}
    public record ReviewRequest(boolean approved, String comment) {}
    public record PhotoRequest(String type, String url) {}
}
