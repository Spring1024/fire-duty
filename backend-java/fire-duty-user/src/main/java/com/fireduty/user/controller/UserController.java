package com.fireduty.user.controller;

import com.fireduty.common.response.Result;
import com.fireduty.user.dto.CreateUserRequest;
import com.fireduty.user.dto.UserQuery;
import com.fireduty.user.entity.User;
import com.fireduty.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<?> list(UserQuery query) {
        return Result.success(userService.list(query));
    }

    @GetMapping("/{id}")
    public Result<User> get(@PathVariable Long id) {
        return Result.success(userService.get(id));
    }

    @PostMapping
    public Result<User> create(@RequestBody CreateUserRequest req) {
        return Result.created(userService.create(req));
    }

    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody User user) {
        return Result.success(userService.update(id, user));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }
}
