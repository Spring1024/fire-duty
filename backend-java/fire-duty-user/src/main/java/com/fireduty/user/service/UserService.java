package com.fireduty.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fireduty.user.dto.CreateUserRequest;
import com.fireduty.user.dto.UserQuery;
import com.fireduty.user.entity.User;

public interface UserService {
    Page<User> list(UserQuery query);
    User get(Long id);
    User create(CreateUserRequest req);
    User update(Long id, User updated);
    void delete(Long id);
}
