package com.fireduty.device.service;

import com.fireduty.device.dto.DeviceImportDTO;
import com.fireduty.device.entity.Device;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel import/export service using Apache POI.
 */
@Service
public class ExcelService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Parse devices from an Excel (.xlsx) file.
     * Expected columns: code, name, type, status, location, manufacturer, installDate
     */
    public List<DeviceImportDTO> parseDeviceImport(MultipartFile file) throws IOException {
        List<DeviceImportDTO> result = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // skip header row
                Row row = sheet.getRow(i);
                if (row == null) continue;

                DeviceImportDTO dto = new DeviceImportDTO();
                dto.setCode(getCellString(row.getCell(0)));
                dto.setName(getCellString(row.getCell(1)));
                dto.setType(getCellString(row.getCell(2)));
                dto.setStatus(getCellString(row.getCell(3)));
                dto.setLocation(getCellString(row.getCell(4)));
                dto.setManufacturer(getCellString(row.getCell(5)));
                String dateStr = getCellString(row.getCell(6));
                if (!dateStr.isEmpty()) {
                    dto.setInstallDate(LocalDate.parse(dateStr, DATE_FMT));
                }
                result.add(dto);
            }
        }
        return result;
    }

    /**
     * Export devices to Excel bytes.
     */
    public byte[] exportToExcel(List<Device> devices) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("设备列表");

            // Header row
            String[] headers = {"编号", "名称", "类型", "状态", "位置", "厂家", "安装日期", "最后检查", "二维码"};
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (int i = 0; i < devices.size(); i++) {
                Device d = devices.get(i);
                Row dataRow = sheet.createRow(i + 1);
                dataRow.createCell(0).setCellValue(d.getCode());
                dataRow.createCell(1).setCellValue(d.getName());
                dataRow.createCell(2).setCellValue(d.getType());
                dataRow.createCell(3).setCellValue(d.getStatus());
                dataRow.createCell(4).setCellValue(d.getLocation());
                dataRow.createCell(5).setCellValue(d.getManufacturer());
                dataRow.createCell(6).setCellValue(d.getInstallDate() != null ? d.getInstallDate().toString() : "");
                dataRow.createCell(7).setCellValue(d.getLastCheck() != null ? d.getLastCheck().toString() : "");
                dataRow.createCell(8).setCellValue(d.getQrCode() != null ? d.getQrCode() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
