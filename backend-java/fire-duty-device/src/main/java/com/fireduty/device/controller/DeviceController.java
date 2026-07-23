package com.fireduty.device.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fireduty.common.annotation.RequirePermission;
import com.fireduty.device.dto.DeviceDTO;
import com.fireduty.device.dto.DeviceImportDTO;
import com.fireduty.device.dto.DeviceQuery;
import com.fireduty.device.entity.Device;
import com.fireduty.device.entity.DeviceType;
import com.fireduty.device.service.DeviceService;
import com.fireduty.device.service.ExcelService;
import com.fireduty.device.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final QrCodeService qrCodeService;
    private final ExcelService excelService;

    @GetMapping
    @RequirePermission(resource = "devices", action = "read")
    public ResponseEntity<IPage<DeviceDTO>> page(DeviceQuery query) {
        return ResponseEntity.ok(deviceService.queryPage(query));
    }

    @GetMapping("/{id}")
    @RequirePermission(resource = "devices", action = "read")
    public ResponseEntity<DeviceDTO> getById(@PathVariable Long id) {
        DeviceDTO dto = deviceService.getById(id);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/types")
    @RequirePermission(resource = "devices", action = "read")
    public ResponseEntity<List<DeviceType>> listTypes() {
        return ResponseEntity.ok(deviceService.listDeviceTypes());
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
                    "gridPath", devices.get(0).getGridPath() != null ? devices.get(0).getGridPath() : "",
                    "devices", devices,
                    "count", devices.size()
            );
        }).collect(Collectors.toList());
        return ResponseEntity.ok(tree);
    }

    /**
     * 生成设备二维码图片
     */
    @GetMapping("/{id}/qrcode")
    @RequirePermission(resource = "devices", action = "read")
    public ResponseEntity<byte[]> generateQrCode(@PathVariable Long id) {
        DeviceDTO device = deviceService.getById(id);
        if (device == null) return ResponseEntity.notFound().build();

        String content = "fireduty://device/" + device.getCode();
        byte[] qrImage = qrCodeService.generateQrCode(content, 300, 300);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("attachment", "qrcode_" + device.getCode() + ".png");
        return ResponseEntity.ok().headers(headers).body(qrImage);
    }

    /**
     * 批量导入设备（支持 Excel .xlsx 上传）
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission(resource = "devices", action = "write")
    public ResponseEntity<Integer> importDevices(@RequestParam("file") MultipartFile file) {
        try {
            List<DeviceImportDTO> importList = excelService.parseDeviceImport(file);
            int count = deviceService.importDevices(importList);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(0);
        }
    }

    /**
     * 导出设备为 Excel (.xlsx)
     */
    @GetMapping("/export")
    @RequirePermission(resource = "devices", action = "read")
    public ResponseEntity<byte[]> export(DeviceQuery query) {
        try {
            List<DeviceDTO> devices = deviceService.exportDevices(query);
            byte[] excelBytes = excelService.exportToExcel(devices);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "devices.xlsx");
            return ResponseEntity.ok().headers(headers).body(excelBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
