package com.sgfb.rent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgfb.rent.entity.Device;
import com.sgfb.rent.entity.RentRecord;
import com.sgfb.rent.mapper.DeviceMapper;
import com.sgfb.rent.mapper.RentRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceService extends ServiceImpl<DeviceMapper, Device> {
    
    @Autowired
    private RentRecordMapper rentRecordMapper;
    
    public List<Device> getAllDevices() {
        return list();
    }
    
    public List<Device> getDevicesByType(String type) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Device::getType, type);
        return list(wrapper);
    }
    
    public List<Device> getAvailableDevicesByType(String type) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        if (type != null && !type.isEmpty()) {
            wrapper.like(Device::getType, type);
        }
        wrapper.in(Device::getStatus, 0, 1);
        return list(wrapper);
    }
    
    public List<Device> getAvailableDevicesByTypeAndTime(String type, LocalDateTime borrowTime, LocalDateTime returnTime) {
        List<Device> devices = getAvailableDevicesByType(type);
        if (borrowTime == null || returnTime == null) {
            return devices;
        }
        
        LambdaQueryWrapper<RentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentRecord::getStatus, 0);
        List<RentRecord> activeRecords = rentRecordMapper.selectList(wrapper);
        List<Integer> occupiedDeviceIds = new ArrayList<>();
        
        for (RentRecord record : activeRecords) {
            if (isTimeOverlap(borrowTime, returnTime, record.getBrwtime(), record.getRtuntime())) {
                if (record.getCamara() != null) occupiedDeviceIds.add(record.getCamara());
                if (record.getLens() != null) occupiedDeviceIds.add(record.getLens());
                if (record.getOther() != null) occupiedDeviceIds.add(record.getOther());
            }
        }
        
        return devices.stream()
                .filter(device -> !occupiedDeviceIds.contains(device.getId()))
                .collect(Collectors.toList());
    }
    
    public boolean isDeviceAvailable(Integer deviceId, LocalDateTime borrowTime, LocalDateTime returnTime, String excludeRecordId) {
        if (deviceId == null || borrowTime == null || returnTime == null) {
            return true;
        }
        
        LambdaQueryWrapper<RentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentRecord::getStatus, 0);
        List<RentRecord> activeRecords = rentRecordMapper.selectList(wrapper);
        
        for (RentRecord record : activeRecords) {
            if (excludeRecordId != null && record.getId().equals(excludeRecordId)) {
                continue;
            }
            
            boolean isDeviceInRecord = deviceId.equals(record.getCamara()) 
                    || deviceId.equals(record.getLens()) 
                    || deviceId.equals(record.getOther());
            
            if (isDeviceInRecord && isTimeOverlap(borrowTime, returnTime, record.getBrwtime(), record.getRtuntime())) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isTimeOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        System.out.println("    --- 时间重叠判断 ---");
        System.out.println("    时间段1: " + start1 + " ~ " + end1);
        System.out.println("    时间段2: " + start2 + " ~ " + end2);
        boolean result = start1.isBefore(end2) && end1.isAfter(start2);
        System.out.println("    结果: " + (result ? "重叠" : "不重叠"));
        return result;
    }
    
    public boolean isDeviceInUse(Integer deviceId) {
        if (deviceId == null) {
            return false;
        }
        LambdaQueryWrapper<RentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentRecord::getStatus, 0);
        List<RentRecord> activeRecords = rentRecordMapper.selectList(wrapper);
        
        for (RentRecord record : activeRecords) {
            if (deviceId.equals(record.getCamara()) 
                    || deviceId.equals(record.getLens()) 
                    || deviceId.equals(record.getOther())) {
                return true;
            }
        }
        return false;
    }
}
