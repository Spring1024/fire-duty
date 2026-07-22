package com.fireduty.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fireduty.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
