package com.sgfb.lab.controller;

import com.sgfb.lab.entity.Device;
import com.sgfb.lab.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DeviceController {
    
    @Autowired
    private DeviceService deviceService;
    
    @GetMapping("/devices")
    public ResponseEntity<Map<String, Object>> getAllDevices() {
        List<Device> devices = deviceService.getAllDevices();
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", devices);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/devices/by-type/{type}")
    public ResponseEntity<Map<String, Object>> getDevicesByType(@PathVariable String type) {
        List<Device> devices = deviceService.getDevicesByType(type);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", devices);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/devices/available")
    public ResponseEntity<Map<String, Object>> getAvailableDevicesByTypeAndTime(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime borrowTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime returnTime) {
        
        List<Device> devices = deviceService.getAvailableDevicesByTypeAndTime(type, borrowTime, returnTime);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", devices);
        
        return ResponseEntity.ok(result);
    }
    
    @PutMapping("/devices/{id}")
    public ResponseEntity<Map<String, Object>> updateDeviceStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> data) {
        Integer status = data.get("status");
        
        Device device = new Device();
        device.setId(id);
        device.setStatus(status);
        
        boolean success = deviceService.updateById(device);
        
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
    
    @PutMapping("/devices/{id}/info")
    public ResponseEntity<Map<String, Object>> updateDeviceInfo(@PathVariable Integer id, @RequestBody Device deviceData) {
        Device device = new Device();
        device.setId(id);
        device.setName(deviceData.getName());
        device.setBrand(deviceData.getBrand());
        device.setType(deviceData.getType());
        device.setStatus(deviceData.getStatus());
        
        boolean success = deviceService.updateById(device);
        
        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("success", true);
            result.put("message", "更新成功");
        } else {
            result.put("success", false);
            result.put("message", "更新失败");
        }
        
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/devices")
    public ResponseEntity<Map<String, Object>> addDevice(@RequestBody Device device) {
        System.out.println("Received device: " + device.getName() + ", " + device.getBrand() + ", " + device.getType() + ", " + device.getStatus());
        
        boolean success = deviceService.save(device);
        System.out.println("Save result: " + success);
        
        Map<String, Object> result = new HashMap<>();
        if (success) {
            result.put("success", true);
            result.put("message", "添加成功");
        } else {
            result.put("success", false);
            result.put("message", "添加失败");
        }
        
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/devices/{id}")
    public ResponseEntity<Map<String, Object>> deleteDevice(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        
        if (deviceService.isDeviceInUse(id)) {
            result.put("success", false);
            result.put("message", "该设备正在被租赁记录使用，无法删除");
            return ResponseEntity.ok(result);
        }
        
        boolean success = deviceService.removeById(id);
        
        if (success) {
            result.put("success", true);
            result.put("message", "删除成功");
        } else {
            result.put("success", false);
            result.put("message", "删除失败");
        }
        
        return ResponseEntity.ok(result);
    }
}
