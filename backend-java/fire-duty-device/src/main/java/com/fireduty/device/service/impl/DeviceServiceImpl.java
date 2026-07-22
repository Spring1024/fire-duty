package com.fireduty.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fireduty.device.dto.DeviceDTO;
import com.fireduty.device.dto.DeviceImportDTO;
import com.fireduty.device.dto.DeviceQuery;
import com.fireduty.device.entity.Device;
import com.fireduty.device.mapper.DeviceMapper;
import com.fireduty.device.service.DeviceService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {

    @Override
    public IPage<Device> queryPage(DeviceQuery query) {
        Page<Device> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<Device> wrapper = buildQueryWrapper(query);
        return this.page(page, wrapper);
    }

    @Override
    public DeviceDTO getById(Long id) {
        Device device = baseMapper.selectById(id);
        if (device == null) {
            return null;
        }
        DeviceDTO dto = new DeviceDTO();
        BeanUtils.copyProperties(device, dto);
        return dto;
    }

    @Override
    public boolean addDevice(DeviceDTO deviceDTO) {
        Device device = new Device();
        BeanUtils.copyProperties(deviceDTO, device);
        return this.save(device);
    }

    @Override
    public boolean updateDevice(Long id, DeviceDTO deviceDTO) {
        Device device = new Device();
        BeanUtils.copyProperties(deviceDTO, device);
        device.setId(id);
        return this.updateById(device);
    }

    @Override
    public boolean removeDevice(Long id) {
        return this.removeById(id);
    }

    @Override
    public List<Device> getDeviceTree() {
        // Get all devices grouped by gridId
        List<Device> allDevices = this.list();
        // Group by gridId — the tree structure can be assembled on the controller side
        return allDevices;
    }

    @Override
    public int importDevices(List<DeviceImportDTO> importDTOList) {
        List<Device> deviceList = importDTOList.stream().map(dto -> {
            Device device = new Device();
            BeanUtils.copyProperties(dto, device);
            return device;
        }).collect(Collectors.toList());
        return this.saveBatch(deviceList) ? deviceList.size() : 0;
    }

    @Override
    public List<Device> exportDevices(DeviceQuery query) {
        LambdaQueryWrapper<Device> wrapper = buildQueryWrapper(query);
        return this.list(wrapper);
    }

    private LambdaQueryWrapper<Device> buildQueryWrapper(DeviceQuery query) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        if (query == null) {
            return wrapper;
        }
        if (StringUtils.hasText(query.getType())) {
            wrapper.eq(Device::getType, query.getType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Device::getStatus, query.getStatus());
        }
        if (query.getGridId() != null) {
            wrapper.eq(Device::getGridId, query.getGridId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(Device::getName, query.getKeyword())
                    .or()
                    .like(Device::getCode, query.getKeyword())
                    .or()
                    .like(Device::getLocation, query.getKeyword())
            );
        }
        wrapper.orderByDesc(Device::getCreatedAt);
        return wrapper;
    }
}
