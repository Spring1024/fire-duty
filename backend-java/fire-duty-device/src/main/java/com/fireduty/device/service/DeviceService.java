package com.fireduty.device.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fireduty.device.dto.DeviceDTO;
import com.fireduty.device.dto.DeviceImportDTO;
import com.fireduty.device.dto.DeviceQuery;
import com.fireduty.device.entity.Device;
import com.fireduty.device.entity.DeviceType;

import java.util.List;

public interface DeviceService extends IService<Device> {

    /**
     * 分页查询设备（含类型名称）
     */
    IPage<DeviceDTO> queryPage(DeviceQuery query);

    /**
     * 根据ID获取设备详情（含类型名称）
     */
    DeviceDTO getById(Long id);

    /**
     * 新增设备
     */
    boolean addDevice(DeviceDTO deviceDTO);

    /**
     * 更新设备
     */
    boolean updateDevice(Long id, DeviceDTO deviceDTO);

    /**
     * 删除设备
     */
    boolean removeDevice(Long id);

    /**
     * 获取设备树（按网格分组）
     */
    List<Device> getDeviceTree();

    /**
     * 批量导入设备
     */
    int importDevices(List<DeviceImportDTO> importDTOList);

    /**
     * 导出设备（含类型名称）
     */
    List<DeviceDTO> exportDevices(DeviceQuery query);

    /**
     * 获取所有设备类型
     */
    List<DeviceType> listDeviceTypes();
}
