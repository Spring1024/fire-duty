package com.fireduty.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fireduty.auth.entity.Permission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}
