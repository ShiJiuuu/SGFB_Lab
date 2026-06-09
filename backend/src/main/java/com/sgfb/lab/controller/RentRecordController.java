package com.sgfb.lab.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sgfb.lab.entity.Classroom;
import com.sgfb.lab.entity.RentRecord;
import com.sgfb.lab.entity.RentRoom;
import com.sgfb.lab.service.ClassroomService;
import com.sgfb.lab.service.RentRecordService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RentRecordController {

    @Autowired
    private RentRecordService rentRecordService;

    @Autowired
    private ClassroomService classroomService;

    private List<Map<String, Object>> enrichRentRecords(List<RentRecord> records) {
        List<Classroom> classrooms = classroomService.getAllClassrooms();
        Map<Integer, Classroom> classroomMap = new HashMap<>();
        for (Classroom c : classrooms) {
            classroomMap.put(c.getId(), c);
        }

        List<Map<String, Object>> enrichedRecords = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (RentRecord record : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", record.getId());
            item.put("name", record.getName());
            item.put("num", record.getNum());
            item.put("tel", record.getTel());
            item.put("status", record.getStatus());
            item.put("remark", record.getRemark());

            List<RentRoom> rentRooms = classroomService.getRentRoomsByRentId(record.getId());
            List<Map<String, Object>> rooms = new ArrayList<>();
            for (RentRoom rr : rentRooms) {
                Map<String, Object> roomInfo = new HashMap<>();
                roomInfo.put("id", rr.getRoomId());
                Classroom room = classroomMap.get(rr.getRoomId());
                roomInfo.put("name", room != null ? room.getName() : "未知教室");
                roomInfo.put("location", room != null ? room.getLocation() : "");
                if (room != null) roomInfo.put("capacity", room.getCapacity());
                roomInfo.put("brwtime", rr.getBrwtime() != null ? rr.getBrwtime().format(fmt) : null);
                roomInfo.put("rtuntime", rr.getRtuntime() != null ? rr.getRtuntime().format(fmt) : null);
                rooms.add(roomInfo);
            }
            item.put("rooms", rooms);

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

        // Date filter: find records whose rooms overlap with the date range
        if (startDate != null && endDate != null) {
            wrapper.apply("id IN (SELECT rent_id FROM RENT_ROOM WHERE BRWTIME <= {0} AND RTNTIME >= {1})",
                    LocalDateTime.of(endDate, LocalTime.MAX), LocalDateTime.of(startDate, LocalTime.MIN));
        } else if (startDate != null) {
            wrapper.apply("id IN (SELECT rent_id FROM RENT_ROOM WHERE RTNTIME >= {0})",
                    LocalDateTime.of(startDate, LocalTime.MIN));
        } else if (endDate != null) {
            wrapper.apply("id IN (SELECT rent_id FROM RENT_ROOM WHERE BRWTIME <= {0})",
                    LocalDateTime.of(endDate, LocalTime.MAX));
        }

        if (status != null && !status.isEmpty()) {
            if (status.contains(",")) {
                String[] statuses = status.split(",");
                List<Integer> statusList = new ArrayList<>();
                for (String s : statuses) {
                    if ("reserved".equals(s)) statusList.add(0);
                    else if ("borrowed".equals(s)) statusList.add(3);
                    else if ("returned".equals(s)) statusList.add(1);
                    else if ("overdue".equals(s)) statusList.add(2);
                    else if ("unpicked".equals(s)) statusList.add(4);
                }
                if (!statusList.isEmpty()) wrapper.in(RentRecord::getStatus, statusList);
            } else {
                switch (status) {
                    case "reserved": wrapper.eq(RentRecord::getStatus, 0); break;
                    case "borrowed": wrapper.eq(RentRecord::getStatus, 3); break;
                    case "returned": wrapper.eq(RentRecord::getStatus, 1); break;
                    case "overdue": wrapper.eq(RentRecord::getStatus, 2); break;
                    case "unpicked": wrapper.eq(RentRecord::getStatus, 4); break;
                }
            }
        }

        wrapper.orderByDesc(RentRecord::getId);

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
        // Find records where any room overlaps with the timeline
        wrapper.apply("id IN (SELECT rent_id FROM RENT_ROOM WHERE BRWTIME < {0} AND RTNTIME > {1})",
                timelineEnd, timelineStart);
        wrapper.orderByDesc(RentRecord::getId);

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
            result.put("message", "预约失败：所选教室在该时间段已被占用");
            return ResponseEntity.ok(result);
        }
        boolean success = rentRecordService.addRentRecord(rentRecord);
        result.put("success", success);
        result.put("message", success ? "预约成功" : "预约失败");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/rent-records/{id}/status")
    public ResponseEntity<Map<String, Object>> updateRentRecordStatus(
            @PathVariable String id, @RequestBody Map<String, Integer> request) {
        Integer status = request.get("status");
        boolean success = rentRecordService.updateRentRecordStatus(id, status);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "状态更新成功" : "状态更新失败");
        return ResponseEntity.ok(result);
    }

    @PutMapping("/rent-records/{id}")
    public ResponseEntity<Map<String, Object>> updateRentRecord(
            @PathVariable String id, @RequestBody RentRecord rentRecord) {
        rentRecord.setId(id);
        boolean hasConflict = rentRecordService.checkTimeConflictForUpdate(rentRecord);
        Map<String, Object> result = new HashMap<>();
        if (hasConflict) {
            result.put("success", false);
            result.put("message", "更新失败：所选教室在该时间段已被其他订单占用");
            return ResponseEntity.ok(result);
        }
        boolean success = rentRecordService.updateRentRecord(rentRecord);
        result.put("success", success);
        result.put("message", success ? "订单更新成功" : "订单更新失败");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/rent-records/{id}")
    public ResponseEntity<Map<String, Object>> deleteRentRecord(@PathVariable String id) {
        boolean success = rentRecordService.deleteRentRecord(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "订单删除成功" : "订单删除失败");
        return ResponseEntity.ok(result);
    }

    @GetMapping("/rent-records/export")
    public ResponseEntity<byte[]> exportRentRecords(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        LambdaQueryWrapper<RentRecord> wrapper = new LambdaQueryWrapper<>();

        if (startDate != null && endDate != null) {
            wrapper.apply("id IN (SELECT rent_id FROM RENT_ROOM WHERE BRWTIME <= {0} AND RTNTIME >= {1})",
                    LocalDateTime.of(endDate, LocalTime.MAX), LocalDateTime.of(startDate, LocalTime.MIN));
        } else if (startDate != null) {
            wrapper.apply("id IN (SELECT rent_id FROM RENT_ROOM WHERE RTNTIME >= {0})",
                    LocalDateTime.of(startDate, LocalTime.MIN));
        } else if (endDate != null) {
            wrapper.apply("id IN (SELECT rent_id FROM RENT_ROOM WHERE BRWTIME <= {0})",
                    LocalDateTime.of(endDate, LocalTime.MAX));
        }

        wrapper.orderByDesc(RentRecord::getId);

        List<RentRecord> records = rentRecordService.list(wrapper);
        List<Classroom> classrooms = classroomService.getAllClassrooms();
        Map<Integer, Classroom> classroomMap = new HashMap<>();
        for (Classroom c : classrooms) classroomMap.put(c.getId(), c);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("预约记录");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        String[] headers = {"订单号", "姓名", "学号", "手机号", "教室", "预约时间", "归还时间"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        int rowNum = 1;

        for (RentRecord record : records) {
            List<RentRoom> rentRooms = classroomService.getRentRoomsByRentId(record.getId());
            if (rentRooms.isEmpty()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getId() != null ? record.getId() : "");
                row.createCell(1).setCellValue(record.getName() != null ? record.getName() : "");
                row.createCell(2).setCellValue(record.getNum() != null ? record.getNum() : "");
                row.createCell(3).setCellValue(record.getTel() != null ? record.getTel() : "");
                row.createCell(4).setCellValue("");
                row.createCell(5).setCellValue("");
                row.createCell(6).setCellValue("");
            } else {
                for (RentRoom rr : rentRooms) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(record.getId() != null ? record.getId() : "");
                    row.createCell(1).setCellValue(record.getName() != null ? record.getName() : "");
                    row.createCell(2).setCellValue(record.getNum() != null ? record.getNum() : "");
                    row.createCell(3).setCellValue(record.getTel() != null ? record.getTel() : "");
                    Classroom room = classroomMap.get(rr.getRoomId());
                    row.createCell(4).setCellValue(room != null ? room.getName() : "未知教室");
                    row.createCell(5).setCellValue(rr.getBrwtime() != null ? rr.getBrwtime().format(fmt) : "");
                    row.createCell(6).setCellValue(rr.getRtuntime() != null ? rr.getRtuntime().format(fmt) : "");
                }
            }
        }

        for (int i = 0; i < 7; i++) sheet.autoSizeColumn(i);

        try {
            byte[] excelBytes;
            try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                workbook.write(baos);
                excelBytes = baos.toByteArray();
            }
            workbook.close();

            String dateStr = "全部";
            if (startDate != null && endDate != null) dateStr = startDate + "_" + endDate;
            else if (startDate != null) dateStr = startDate + "_至今";
            else if (endDate != null) dateStr = "开始_" + endDate;
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
