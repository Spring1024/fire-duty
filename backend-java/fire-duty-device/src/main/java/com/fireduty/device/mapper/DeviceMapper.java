package com.fireduty.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fireduty.device.dto.DeviceDTO;
import com.fireduty.device.dto.DeviceQuery;
import com.fireduty.device.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DeviceMapper extends BaseMapper<Device> {

    @Select("SELECT d.*, dt.name AS device_type_name FROM devices d " +
            "LEFT JOIN device_types dt ON d.device_type_id = dt.id " +
            "WHERE d.id = #{id}")
    DeviceDTO selectDetailById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT d.*, dt.name AS device_type_name FROM devices d " +
            "LEFT JOIN device_types dt ON d.device_type_id = dt.id " +
            "<where>" +
            "  <if test='query.deviceTypeId != null'> AND d.device_type_id = #{query.deviceTypeId}</if>" +
            "  <if test='query.status != null'> AND d.status = #{query.status}</if>" +
            "  <if test='query.gridId != null'> AND d.grid_id = #{query.gridId}</if>" +
            "  <if test='query.keyword != null and query.keyword != \"\"'>" +
            "    AND (d.name LIKE CONCAT('%',#{query.keyword},'%') " +
            "      OR d.code LIKE CONCAT('%',#{query.keyword},'%') " +
            "      OR d.location LIKE CONCAT('%',#{query.keyword},'%'))" +
            "  </if>" +
            "</where>" +
            " ORDER BY d.created_at DESC" +
            "</script>")
    IPage<DeviceDTO> selectPageWithTypeName(Page<DeviceDTO> page, @Param("query") DeviceQuery query);
}
