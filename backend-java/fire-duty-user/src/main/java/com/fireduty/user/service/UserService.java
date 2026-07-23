package com.fireduty.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fireduty.user.dto.CreateUserRequest;
import com.fireduty.user.dto.UpdateUserRequest;
import com.fireduty.user.dto.UserQuery;
import com.fireduty.user.entity.Role;
import com.fireduty.user.entity.User;

import java.util.List;

public interface UserService {
    IPage<User> list(UserQuery query);
    User get(Long id);
    User create(CreateUserRequest req);
    User update(Long id, UpdateUserRequest req);
    void delete(Long id);
    /** 获取所有角色列表 */
    List<Role> listRoles();
    /** 获取用户的角色名称列表 */
    List<String> getUserRoles(Long userId);
}
