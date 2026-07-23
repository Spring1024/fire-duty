package com.fireduty.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fireduty.user.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

    /** 查询用户角色名称列表 */
    @Select("SELECT r.name FROM roles r INNER JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id = #{userId}")
    List<String> selectRoleNamesByUserId(@Param("userId") Long userId);

    /** 通用分页查询（含角色名称） */
    @Select("<script>" +
            "SELECT u.*, r.name AS role_name FROM users u " +
            "LEFT JOIN user_roles ur ON u.id = ur.user_id " +
            "LEFT JOIN roles r ON ur.role_id = r.id " +
            "<where>" +
            "  <if test='roleId != null'> AND ur.role_id = #{roleId}</if>" +
            "  <if test='status != null'> AND u.status = #{status}</if>" +
            "  <if test='search != null and search != \"\"'>" +
            "    AND (u.name LIKE CONCAT('%',#{search},'%') OR u.username LIKE CONCAT('%',#{search},'%'))" +
            "  </if>" +
            "</where>" +
            " ORDER BY u.id DESC" +
            "</script>")
    IPage<User> selectPageWithRole(Page<User> page, @Param("roleId") Long roleId,
                                    @Param("status") Integer status, @Param("search") String search);
}
