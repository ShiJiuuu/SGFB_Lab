package com.sgfb.rent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sgfb.rent.entity.Device;
import com.sgfb.rent.entity.RentRecord;
import com.sgfb.rent.service.DeviceService;
import com.sgfb.rent.service.RentRecordService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RentRecordController {
    
    @Autowired
    private RentRecordService rentRecordService;
    
    @Autowired
    private DeviceService deviceService;

    private List<Map<String, Object>> enrichRentRecords(List<RentRecord> records) {
        List<Device> devices = deviceService.getAllDevices();
        Map<Integer, Device> deviceMap = new HashMap<>();
        for (Device device : devices) {
            deviceMap.put(device.getId(), device);
        }

        List<Map<String, Object>> enrichedRecords = new ArrayList<>();
        for (RentRecord record : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("name", record.getName());
            item.put("num", record.getNum());
            item.put("tel", record.getTel());
            item.put("brwtime", record.getBrwtime());
            item.put("rtuntime", record.getRtuntime());
            item.put("status", record.getStatus());
            item.put("remark", record.getRemark());

            Map<String, Object> camaraInfo = null;
            if (record.getCamara() != null) {
                camaraInfo = new HashMap<>();
                Device camara = deviceMap.get(record.getCamara());
                camaraInfo.put("id", record.getCamara());
                camaraInfo.put("name", camara != null ? camara.getName() : "未知设备");
            }
            item.put("camara", camaraInfo);

            Map<String, Object> lensInfo = null;
            if (record.getLens() != null) {
                lensInfo = new HashMap<>();
                Device lens = deviceMap.get(record.getLens());
                lensInfo.put("id", record.getLens());
                lensInfo.put("name", lens != null ? lens.getName() : "未知设备");
            }
            item.put("lens", lensInfo);

            Map<String, Object> otherInfo = null;
            if (record.getOther() != null) {
                otherInfo = new HashMap<>();
                Device other = deviceMap.get(record.getOther());
                otherInfo.put("id", record.getOther());
                otherInfo.put("name", other != null ? other.getName() : "未知设备");
            }
            item.put("other", otherInfo);

            enrichedRecords.add(item);
        }
        return enrichedRecords;
    }
    
    @GetMapping("/rent-records")
    public ResponseEntity<Map<String, Object>> getAllRentRecords(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        LambdaQueryWrapper<RentRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (startDate != null) {
            wrapper.ge(RentRecord::getBrwtime, LocalDateTime.of(startDate, LocalTime.MIN));
        }
        
        if (endDate != null) {
            wrapper.le(RentRecord::getBrwtime, LocalDateTime.of(endDate, LocalTime.MAX));
        }
        
        if (status != null && !status.isEmpty()) {
            if (status.contains(",")) {
                String[] statuses = status.split(",");
                List<Integer> statusList = new ArrayList<>();
                for (String s : statuses) {
                    if ("active".equals(s)) {
                        statusList.add(0);
                    } else if ("returned".equals(s)) {
                        statusList.add(1);
                    } else if ("overdue".equals(s)) {
                        statusList.add(2);
                    }
                }
                if (!statusList.isEmpty()) {
                    wrapper.in(RentRecord::getStatus, statusList);
                }
            } else {
                if ("active".equals(status)) {
                    wrapper.eq(RentRecord::getStatus, 0);
                } else if ("returned".equals(status)) {
                    wrapper.eq(RentRecord::getStatus, 1);
                } else if ("overdue".equals(status)) {
                    wrapper.eq(RentRecord::getStatus, 2);
                }
            }
        }
        
        wrapper.orderByAsc(RentRecord::getBrwtime);
        
        Page<RentRecord> pageResult = rentRecordService.page(new Page<>(page, pageSize), wrapper);
        List<RentRecord> records = pageResult.getRecords();
        List<Map<String, Object>> enrichedRecords = enrichRentRecords(records);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", enrichedRecords);
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("pageSize", pageResult.getSize());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/rent-records/gantt")
    public ResponseEntity<Map<String, Object>> getGanttRentRecords(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime time) {
        LocalDateTime timelineEnd = time != null ? time : LocalDateTime.now().plusDays(4);
        LocalDateTime timelineStart = timelineEnd.minusDays(4);

        LambdaQueryWrapper<RentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(RentRecord::getBrwtime, timelineEnd)
                .ge(RentRecord::getRtuntime, timelineStart)
                .orderByDesc(RentRecord::getBrwtime);

        List<RentRecord> records = rentRecordService.list(wrapper);
        List<Map<String, Object>> enrichedRecords = enrichRentRecords(records);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", enrichedRecords);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/rent-records")
    public ResponseEntity<Map<String, Object>> addRentRecord(@RequestBody RentRecord rentRecord) {
        boolean hasConflict = rentRecordService.checkTimeConflict(rentRecord);
        
        Map<String, Object> result = new HashMap<>();
        if (hasConflict) {
            result.put("success", false);
            result.put("message", "预约失败：所选设备在该时间段已被占用");
            return ResponseEntity.ok(result);
        }
        
        boolean success = rentRecordService.addRentRecord(rentRecord);
        
        if (success) {
            result.put("success", true);
            result.put("message", "预约成功");
        } else {
            result.put("success", false);
            result.put("message", "预约失败");
        }
        
        return ResponseEntity.ok(result);
    }
    
    @PutMapping("/rent-records/{id}/status")
    public ResponseEntity<Map<String, Object>> updateRentRecordStatus(
            @PathVariable String id,
            @RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        boolean success = rentRecordService.updateRentRecordStatus(id, status);
        
        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("success", true);
            result.put("message", "状态更新成功");
        } else {
            result.put("success", false);
            result.put("message", "状态更新失败");
        }
        
        return ResponseEntity.ok(result);
    }
    
    @PutMapping("/rent-records/{id}")
    public ResponseEntity<Map<String, Object>> updateRentRecord(
            @PathVariable String id,
            @RequestBody RentRecord rentRecord) {
        rentRecord.setId(id);

        boolean hasConflict = rentRecordService.checkTimeConflictForUpdate(rentRecord);

        Map<String, Object> result = new HashMap<>();
        if (hasConflict) {
            result.put("success", false);
            result.put("message", "更新失败：所选设备在该时间段已被其他订单占用");
            return ResponseEntity.ok(result);
        }

        boolean success = rentRecordService.updateRentRecord(rentRecord);

        if (success) {
            result.put("success", true);
            result.put("message", "订单更新成功");
        } else {
            result.put("success", false);
            result.put("message", "订单更新失败");
        }

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/rent-records/{id}")
    public ResponseEntity<Map<String, Object>> deleteRentRecord(@PathVariable String id) {
        boolean success = rentRecordService.deleteRentRecord(id);

        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("success", true);
            result.put("message", "订单删除成功");
        } else {
            result.put("success", false);
            result.put("message", "订单删除失败");
        }

        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/rent-records/export")
    public ResponseEntity<byte[]> exportRentRecords(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        LambdaQueryWrapper<RentRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (startDate != null) {
            wrapper.ge(RentRecord::getBrwtime, LocalDateTime.of(startDate, LocalTime.MIN));
        }
        
        if (endDate != null) {
            wrapper.le(RentRecord::getBrwtime, LocalDateTime.of(endDate, LocalTime.MAX));
        }
        
        wrapper.orderByAsc(RentRecord::getBrwtime);
        
        List<RentRecord> records = rentRecordService.list(wrapper);
        List<Device> devices = deviceService.getAllDevices();
        
        Map<Integer, Device> deviceMap = new HashMap<>();
        for (Device device : devices) {
            deviceMap.put(device.getId(), device);
        }
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("预约记录");
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        
        String[] headers = { "订单号", "姓名", "学号", "手机号", "相机", "镜头", "其他", "预约时间", "归还时间" };
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        int rowNum = 1;
        for (RentRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(record.getId() != null ? record.getId() : "");
            row.createCell(1).setCellValue(record.getName() != null ? record.getName() : "");
            row.createCell(2).setCellValue(record.getNum() != null ? record.getNum() : "");
            row.createCell(3).setCellValue(record.getTel() != null ? record.getTel() : "");
            
            String camaraName = "";
            if (record.getCamara() != null) {
                Device camara = deviceMap.get(record.getCamara());
                camaraName = camara != null ? camara.getName() : "未知设备";
            }
            row.createCell(4).setCellValue(camaraName);
            
            String lensName = "";
            if (record.getLens() != null) {
                Device lens = deviceMap.get(record.getLens());
                lensName = lens != null ? lens.getName() : "未知设备";
            }
            row.createCell(5).setCellValue(lensName);
            
            String otherName = "";
            if (record.getOther() != null) {
                Device other = deviceMap.get(record.getOther());
                otherName = other != null ? other.getName() : "未知设备";
            }
            row.createCell(6).setCellValue(otherName);
            
            row.createCell(7).setCellValue(record.getBrwtime() != null ? record.getBrwtime().format(formatter) : "");
            row.createCell(8).setCellValue(record.getRtuntime() != null ? record.getRtuntime().format(formatter) : "");
        }
        
        for (int i = 0; i < 9; i++) {
            sheet.autoSizeColumn(i);
        }
        
        try {
            byte[] excelBytes;
            try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                workbook.write(baos);
                excelBytes = baos.toByteArray();
            }
            workbook.close();
            
            String dateStr;
            if (startDate != null && endDate != null) {
                dateStr = startDate.toString() + "_" + endDate.toString();
            } else if (startDate != null) {
                dateStr = startDate.toString() + "_至今";
            } else if (endDate != null) {
                dateStr = "开始_" + endDate.toString();
            } else {
                dateStr = "全部";
            }
            String fileName = "预约记录_" + dateStr + ".xlsx";
            
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8");
            responseHeaders.setContentDispositionFormData("attachment", encodedFileName);
            
            return new ResponseEntity<>(excelBytes, responseHeaders, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
