package com.fireduty.device.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fireduty.device.dto.DeviceDTO;
import com.fireduty.device.dto.DeviceImportDTO;
import com.fireduty.device.dto.DeviceQuery;
import com.fireduty.device.entity.Device;

import java.util.List;

public interface DeviceService extends IService<Device> {

    /**
     * Paginated device query
     */
    IPage<Device> queryPage(DeviceQuery query);

    /**
     * Get device by ID
     */
    DeviceDTO getById(Long id);

    /**
     * Create device
     */
    boolean addDevice(DeviceDTO deviceDTO);

    /**
     * Update device
     */
    boolean updateDevice(Long id, DeviceDTO deviceDTO);

    /**
     * Delete device by ID
     */
    boolean removeDevice(Long id);

    /**
     * Get device tree (grouped by grid)
     */
    List<Device> getDeviceTree();

    /**
     * Batch import devices
     */
    int importDevices(List<DeviceImportDTO> importDTOList);

    /**
     * Export devices (all matching query)
     */
    List<Device> exportDevices(DeviceQuery query);
}
