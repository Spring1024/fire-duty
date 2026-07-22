package com.fireduty.device.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fireduty.common.annotation.RequirePermission;
import com.fireduty.device.dto.DeviceDTO;
import com.fireduty.device.dto.DeviceImportDTO;
import com.fireduty.device.dto.DeviceQuery;
import com.fireduty.device.entity.Device;
import com.fireduty.device.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping
    @RequirePermission(resource = "devices", action = "read")
    public ResponseEntity<IPage<Device>> page(DeviceQuery query) {
        return ResponseEntity.ok(deviceService.queryPage(query));
    }

    @GetMapping("/{id}")
    @RequirePermission(resource = "devices", action = "read")
    public ResponseEntity<DeviceDTO> getById(@PathVariable Long id) {
        DeviceDTO dto = deviceService.getById(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @RequirePermission(resource = "devices", action = "write")
    public ResponseEntity<Boolean> create(@RequestBody DeviceDTO deviceDTO) {
        return ResponseEntity.ok(deviceService.addDevice(deviceDTO));
    }

    @PutMapping("/{id}")
    @RequirePermission(resource = "devices", action = "write")
    public ResponseEntity<Boolean> update(@PathVariable Long id, @RequestBody DeviceDTO deviceDTO) {
        return ResponseEntity.ok(deviceService.updateDevice(id, deviceDTO));
    }

    @DeleteMapping("/{id}")
    @RequirePermission(resource = "devices", action = "delete")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.removeDevice(id));
    }

    @GetMapping("/tree")
    @RequirePermission(resource = "devices", action = "read")
    public ResponseEntity<List<Map<String, Object>>> tree() {
        List<Device> allDevices = deviceService.getDeviceTree();
        Map<Long, List<Device>> grouped = allDevices.stream()
                .collect(Collectors.groupingBy(d -> d.getGridId() != null ? d.getGridId() : 0L));
        List<Map<String, Object>> tree = grouped.entrySet().stream().map(entry -> {
            Long gridId = entry.getKey();
            List<Device> devices = entry.getValue();
            return Map.of(
                    "gridId", gridId,
                    "gridPath", devices.get(0).getGridPath(),
                    "devices", devices,
                    "count", devices.size()
            );
        }).collect(Collectors.toList());
        return ResponseEntity.ok(tree);
    }

    @PostMapping("/import")
    @RequirePermission(resource = "devices", action = "write")
    public ResponseEntity<Integer> importDevices(@RequestBody List<DeviceImportDTO> importList) {
        return ResponseEntity.ok(deviceService.importDevices(importList));
    }

    @GetMapping("/export")
    @RequirePermission(resource = "devices", action = "read")
    public ResponseEntity<byte[]> export(DeviceQuery query) {
        List<Device> devices = deviceService.exportDevices(query);
        StringBuilder csv = new StringBuilder("ID,编号,名称,类型,状态,位置,网格ID,厂家,安装日期,最后检查,最后维护\n");
        for (Device d : devices) {
            csv.append(d.getId()).append(",")
               .append(d.getCode()).append(",")
               .append(d.getName()).append(",")
               .append(d.getType()).append(",")
               .append(d.getStatus()).append(",")
               .append(d.getLocation()).append(",")
               .append(d.getGridId()).append(",")
               .append(d.getManufacturer()).append(",")
               .append(d.getInstallDate()).append(",")
               .append(d.getLastCheck()).append(",")
               .append(d.getLastMaintenance()).append("\n");
        }
        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "devices.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
