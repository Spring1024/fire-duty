package com.fireduty.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fireduty.common.annotation.RequirePermission;
import com.fireduty.common.response.Result;
import com.fireduty.user.dto.CreateUserRequest;
import com.fireduty.user.dto.UpdateUserRequest;
import com.fireduty.user.dto.UserQuery;
import com.fireduty.user.entity.Role;
import com.fireduty.user.entity.User;
import com.fireduty.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @RequirePermission(resource = "users", action = "read")
    public Result<?> list(UserQuery query) {
        return Result.success(userService.list(query));
    }

    @GetMapping("/{id}")
    @RequirePermission(resource = "users", action = "read")
    public Result<User> get(@PathVariable Long id) {
        return Result.success(userService.get(id));
    }

    @GetMapping("/{id}/roles")
    @RequirePermission(resource = "users", action = "read")
    public Result<List<String>> getUserRoles(@PathVariable Long id) {
        return Result.success(userService.getUserRoles(id));
    }

    @GetMapping("/roles")
    @RequirePermission(resource = "users", action = "read")
    public Result<List<Role>> listRoles() {
        return Result.success(userService.listRoles());
    }

    @PostMapping
    @RequirePermission(resource = "users", action = "write")
    public Result<User> create(@RequestBody CreateUserRequest req) {
        return Result.created(userService.create(req));
    }

    @PutMapping("/{id}")
    @RequirePermission(resource = "users", action = "write")
    public Result<User> update(@PathVariable Long id, @RequestBody UpdateUserRequest req) {
        return Result.success(userService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(resource = "users", action = "delete")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}
