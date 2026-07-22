package com.fireduty.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fireduty.common.exception.BusinessException;
import com.fireduty.common.exception.ResourceNotFoundException;
import com.fireduty.user.dto.CreateUserRequest;
import com.fireduty.user.dto.UserQuery;
import com.fireduty.user.entity.User;
import com.fireduty.user.mapper.UserMapper;
import com.fireduty.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Page<User> list(UserQuery query) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getSearch())) {
            wrapper.and(w -> w.like(User::getName, query.getSearch())
                    .or().like(User::getUsername, query.getSearch()));
        }
        if (StrUtil.isNotBlank(query.getRole())) {
            wrapper.eq(User::getRole, query.getRole());
        }
        if (StrUtil.isNotBlank(query.getStatus())) {
            wrapper.eq(User::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(User::getId);
        return userMapper.selectPage(new Page<>(query.getPage(), query.getPageSize()), wrapper);
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
        // Check uniqueness
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setName(req.getName());
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        user.setGridId(req.getGridId());
        user.setPhone(req.getPhone());
        user.setStatus("正常");
        userMapper.insert(user);
        log.info("Created user: {}", req.getUsername());
        return user;
    }

    @Override
    @Transactional
    public User update(Long id, User updated) {
        User existing = userMapper.selectById(id);
        if (existing == null) throw new ResourceNotFoundException("用户不存在");

        if (StrUtil.isNotBlank(updated.getName())) existing.setName(updated.getName());
        if (StrUtil.isNotBlank(updated.getUsername())) {
            // Check uniqueness if changed
            Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getUsername, updated.getUsername())
                            .ne(User::getId, id));
            if (count > 0) throw new BusinessException("用户名已被使用");
            existing.setUsername(updated.getUsername());
        }
        if (StrUtil.isNotBlank(updated.getPasswordHash())) {
            existing.setPasswordHash(passwordEncoder.encode(updated.getPasswordHash()));
        }
        if (StrUtil.isNotBlank(updated.getRole())) existing.setRole(updated.getRole());
        if (updated.getGridId() != null) existing.setGridId(updated.getGridId());
        if (StrUtil.isNotBlank(updated.getPhone())) existing.setPhone(updated.getPhone());
        if (StrUtil.isNotBlank(updated.getStatus())) existing.setStatus(updated.getStatus());
        existing.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(existing);
        return existing;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (userMapper.selectById(id) == null) {
            throw new ResourceNotFoundException("用户不存在");
        }
        userMapper.deleteById(id);
    }
}
