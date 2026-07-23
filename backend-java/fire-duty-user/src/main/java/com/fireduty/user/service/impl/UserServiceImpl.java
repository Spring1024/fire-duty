package com.fireduty.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fireduty.common.exception.BusinessException;
import com.fireduty.common.exception.ResourceNotFoundException;
import com.fireduty.user.dto.CreateUserRequest;
import com.fireduty.user.dto.UpdateUserRequest;
import com.fireduty.user.dto.UserQuery;
import com.fireduty.user.entity.Role;
import com.fireduty.user.entity.User;
import com.fireduty.user.entity.UserRole;
import com.fireduty.user.mapper.RoleMapper;
import com.fireduty.user.mapper.UserMapper;
import com.fireduty.user.mapper.UserRoleMapper;
import com.fireduty.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public IPage<User> list(UserQuery query) {
        Page<User> page = new Page<>(query.getPage(), query.getPageSize());
        return userMapper.selectPageWithRole(page, query.getRoleId(), query.getStatus(), query.getSearch());
    }

    @Override
    public User get(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) throw new ResourceNotFoundException("用户不存在");
        return user;
    }

    @Override
    @Transactional
    public User create(CreateUserRequest req) {
        // 检查用户名唯一性
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setName(req.getName());
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setGridId(req.getGridId());
        user.setPhone(req.getPhone());
        user.setStatus(1);
        userMapper.insert(user);

        // 分配角色
        if (req.getRoleId() != null) {
            assignRole(user.getId(), req.getRoleId());
        }

        log.info("Created user: {} with roleId: {}", req.getUsername(), req.getRoleId());
        return user;
    }

    @Override
    @Transactional
    public User update(Long id, UpdateUserRequest req) {
        User existing = userMapper.selectById(id);
        if (existing == null) throw new ResourceNotFoundException("用户不存在");

        if (StrUtil.isNotBlank(req.getName())) existing.setName(req.getName());
        if (StrUtil.isNotBlank(req.getUsername())) {
            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getUsername, req.getUsername())
                            .ne(User::getId, id));
            if (count > 0) throw new BusinessException("用户名已被使用");
            existing.setUsername(req.getUsername());
        }
        if (StrUtil.isNotBlank(req.getPassword())) {
            existing.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getGridId() != null) existing.setGridId(req.getGridId());
        if (StrUtil.isNotBlank(req.getPhone())) existing.setPhone(req.getPhone());
        if (req.getStatus() != null) existing.setStatus(req.getStatus());
        userMapper.updateById(existing);

        // 更新角色（先删后插）
        if (req.getRoleId() != null) {
            userRoleMapper.deleteByUserId(id);
            assignRole(id, req.getRoleId());
        }

        return existing;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (userMapper.selectById(id) == null) {
            throw new ResourceNotFoundException("用户不存在");
        }
        userRoleMapper.deleteByUserId(id);
        userMapper.deleteById(id);
    }

    @Override
    public List<Role> listRoles() {
        return roleMapper.selectList(
                new LambdaQueryWrapper<Role>().orderByAsc(Role::getSortOrder));
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        return userMapper.selectRoleNamesByUserId(userId);
    }

    private void assignRole(Long userId, Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在: id=" + roleId);
        }
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRoleMapper.insert(userRole);
    }
}
