package com.fireduty.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fireduty.common.exception.BusinessException;
import com.fireduty.device.dto.DeviceDTO;
import com.fireduty.device.dto.DeviceImportDTO;
import com.fireduty.device.dto.DeviceQuery;
import com.fireduty.device.entity.Device;
import com.fireduty.device.entity.DeviceType;
import com.fireduty.device.mapper.DeviceMapper;
import com.fireduty.device.mapper.DeviceTypeMapper;
import com.fireduty.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceService {

    private final DeviceTypeMapper deviceTypeMapper;

    @Override
    public IPage<DeviceDTO> queryPage(DeviceQuery query) {
        Page<DeviceDTO> page = new Page<>(query.getPage(), query.getPageSize());
        return baseMapper.selectPageWithTypeName(page, query);
    }

    @Override
    public DeviceDTO getById(Long id) {
        return baseMapper.selectDetailById(id);
    }

    @Override
    public boolean addDevice(DeviceDTO deviceDTO) {
        Device device = new Device();
        BeanUtils.copyProperties(deviceDTO, device, "deviceTypeName");
        return this.save(device);
    }

    @Override
    public boolean updateDevice(Long id, DeviceDTO deviceDTO) {
        Device device = new Device();
        BeanUtils.copyProperties(deviceDTO, device, "deviceTypeName");
        device.setId(id);
        return this.updateById(device);
    }

    @Override
    public boolean removeDevice(Long id) {
        return this.removeById(id);
    }

    @Override
    public List<Device> getDeviceTree() {
        return this.list();
    }

    @Override
    public int importDevices(List<DeviceImportDTO> importDTOList) {
        // 预加载设备类型映射: name → id
        Map<String, Long> typeNameToId = listDeviceTypes().stream()
                .collect(Collectors.toMap(DeviceType::getName, DeviceType::getId));

        List<Device> deviceList = importDTOList.stream().map(dto -> {
            Device device = new Device();
            device.setCode(dto.getCode());
            device.setName(dto.getName());
            device.setLocation(dto.getLocation());
            device.setManufacturer(dto.getManufacturer());
            device.setInstallDate(dto.getInstallDate());
            // 根据类型名称解析 device_type_id
            Long typeId = typeNameToId.get(dto.getType());
            if (typeId == null) {
                throw new BusinessException("未知的设备类型: " + dto.getType());
            }
            device.setDeviceTypeId(typeId);
            return device;
        }).collect(Collectors.toList());
        return this.saveBatch(deviceList) ? deviceList.size() : 0;
    }

    @Override
    public List<DeviceDTO> exportDevices(DeviceQuery query) {
        // 使用不分页的方式查询全部（设置大页）
        Page<DeviceDTO> page = new Page<>(1, Integer.MAX_VALUE);
        IPage<DeviceDTO> result = baseMapper.selectPageWithTypeName(page, query);
        return result.getRecords();
    }

    @Override
    public List<DeviceType> listDeviceTypes() {
        return deviceTypeMapper.selectList(
                new LambdaQueryWrapper<DeviceType>().orderByAsc(DeviceType::getSortOrder));
    }

    private LambdaQueryWrapper<Device> buildQueryWrapper(DeviceQuery query) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        if (query == null) {
            return wrapper;
        }
        if (query.getDeviceTypeId() != null) {
            wrapper.eq(Device::getDeviceTypeId, query.getDeviceTypeId());
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
