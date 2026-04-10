package com.sgfb.rent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sgfb.rent.entity.Device;
import com.sgfb.rent.entity.RentRecord;
import com.sgfb.rent.service.DeviceService;
import com.sgfb.rent.service.RentRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class RentRecordController {
    
    @Autowired
    private RentRecordService rentRecordService;
    
    @Autowired
    private DeviceService deviceService;
    
    @GetMapping("/rent-records")
    public ResponseEntity<Map<String, Object>> getAllRentRecords(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        LambdaQueryWrapper<RentRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (startDate != null) {
            wrapper.ge(RentRecord::getBrwtime, LocalDateTime.of(startDate, LocalTime.MIN));
        }
        
        if (endDate != null) {
            wrapper.le(RentRecord::getBrwtime, LocalDateTime.of(endDate, LocalTime.MAX));
        }
        
        wrapper.orderByDesc(RentRecord::getBrwtime);
        
        Page<RentRecord> pageResult = rentRecordService.page(new Page<>(page, pageSize), wrapper);
        List<RentRecord> records = pageResult.getRecords();
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
            
            Map<String, Object> camaraInfo = new HashMap<>();
            if (record.getCamara() != null) {
                Device camara = deviceMap.get(record.getCamara());
                camaraInfo.put("id", record.getCamara());
                camaraInfo.put("name", camara != null ? camara.getName() : "未知设备");
            }
            item.put("camara", camaraInfo);
            
            Map<String, Object> lensInfo = new HashMap<>();
            if (record.getLens() != null) {
                Device lens = deviceMap.get(record.getLens());
                lensInfo.put("id", record.getLens());
                lensInfo.put("name", lens != null ? lens.getName() : "未知设备");
            }
            item.put("lens", lensInfo);
            
            Map<String, Object> otherInfo = new HashMap<>();
            if (record.getOther() != null) {
                Device other = deviceMap.get(record.getOther());
                otherInfo.put("id", record.getOther());
                otherInfo.put("name", other != null ? other.getName() : "未知设备");
            }
            item.put("other", otherInfo);
            
            enrichedRecords.add(item);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", enrichedRecords);
        result.put("total", pageResult.getTotal());
        result.put("page", pageResult.getCurrent());
        result.put("pageSize", pageResult.getSize());
        
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
}
