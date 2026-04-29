package com.sgfb.rent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgfb.rent.entity.Device;
import com.sgfb.rent.entity.RentRecord;
import com.sgfb.rent.mapper.RentRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RentRecordService extends ServiceImpl<RentRecordMapper, RentRecord> {
    
    @Autowired
    private DeviceService deviceService;
    
    public List<RentRecord> getAllRentRecords() {
        return list();
    }
    
    public List<RentRecord> getActiveRentRecords() {
        LambdaQueryWrapper<RentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentRecord::getStatus, 0);
        return list(wrapper);
    }
    
    @Transactional
    public boolean addRentRecord(RentRecord rentRecord) {
        rentRecord.setId(generateId());
        rentRecord.setStatus(0);
        boolean saved = save(rentRecord);
        
        if (saved) {
            updateDeviceIfNeeded(rentRecord.getCamara(), 1);
            updateDeviceIfNeeded(rentRecord.getLens(), 1);
            updateDeviceIfNeeded(rentRecord.getOther(), 1);
        }
        
        return saved;
    }
    
    public boolean checkTimeConflict(RentRecord rentRecord) {
        if (rentRecord.getCamara() != null) {
            if (!deviceService.isDeviceAvailable(rentRecord.getCamara(), rentRecord.getBrwtime(), rentRecord.getRtuntime(), null)) {
                return true;
            }
        }
        if (rentRecord.getLens() != null) {
            if (!deviceService.isDeviceAvailable(rentRecord.getLens(), rentRecord.getBrwtime(), rentRecord.getRtuntime(), null)) {
                return true;
            }
        }
        if (rentRecord.getOther() != null) {
            if (!deviceService.isDeviceAvailable(rentRecord.getOther(), rentRecord.getBrwtime(), rentRecord.getRtuntime(), null)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkTimeConflictForUpdate(RentRecord rentRecord) {
        if (rentRecord.getCamara() != null) {
            if (!deviceService.isDeviceAvailable(rentRecord.getCamara(), rentRecord.getBrwtime(), rentRecord.getRtuntime(), rentRecord.getId())) {
                return true;
            }
        }
        if (rentRecord.getLens() != null) {
            if (!deviceService.isDeviceAvailable(rentRecord.getLens(), rentRecord.getBrwtime(), rentRecord.getRtuntime(), rentRecord.getId())) {
                return true;
            }
        }
        if (rentRecord.getOther() != null) {
            if (!deviceService.isDeviceAvailable(rentRecord.getOther(), rentRecord.getBrwtime(), rentRecord.getRtuntime(), rentRecord.getId())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean updateRentRecordStatus(String id, Integer status) {
        RentRecord record = getById(id);
        if (record == null) {
            return false;
        }
        
        Integer oldStatus = record.getStatus();
        record.setStatus(status);
        boolean updated = updateById(record);
        
        if (updated) {
            updateDeviceStatusForOrder(record, oldStatus, status);
        }
        
        return updated;
    }
    
    private void updateDeviceStatusForOrder(RentRecord record, Integer oldStatus, Integer newStatus) {
        if (newStatus == 0 || newStatus == 3) {
            // 订单变为"已预约"或"已借出" → 设备库存变为"已预约借出"
            updateDeviceIfNeeded(record.getCamara(), 1);
            updateDeviceIfNeeded(record.getLens(), 1);
            updateDeviceIfNeeded(record.getOther(), 1);
        } else if (newStatus == 1) {
            // 订单变为"已归还" → 设备库存恢复为"正常库存"
            updateDeviceIfNeeded(record.getCamara(), 0);
            updateDeviceIfNeeded(record.getLens(), 0);
            updateDeviceIfNeeded(record.getOther(), 0);
        } else if (newStatus == 2) {
            // 订单变为"逾期未还" → 设备库存变为"逾期未还"
            updateDeviceIfNeeded(record.getCamara(), 2);
            updateDeviceIfNeeded(record.getLens(), 2);
            updateDeviceIfNeeded(record.getOther(), 2);
        }
    }
    
    private void updateDeviceIfNeeded(Integer deviceId, Integer targetStatus) {
        if (deviceId == null) {
            return;
        }
        Device device = deviceService.getById(deviceId);
        if (device != null && device.getStatus() != targetStatus) {
            device.setStatus(targetStatus);
            deviceService.updateById(device);
        }
    }
    
    public boolean updateRentRecord(RentRecord rentRecord) {
        RentRecord existingRecord = getById(rentRecord.getId());
        if (existingRecord == null) {
            return false;
        }

        Integer oldStatus = existingRecord.getStatus();
        
        existingRecord.setName(rentRecord.getName());
        existingRecord.setNum(rentRecord.getNum());
        existingRecord.setTel(rentRecord.getTel());
        existingRecord.setCamara(rentRecord.getCamara());
        existingRecord.setLens(rentRecord.getLens());
        existingRecord.setOther(rentRecord.getOther());
        existingRecord.setBrwtime(rentRecord.getBrwtime());
        existingRecord.setRtuntime(rentRecord.getRtuntime());
        existingRecord.setStatus(rentRecord.getStatus());
        existingRecord.setRemark(rentRecord.getRemark());

        boolean updated = updateById(existingRecord);
        
        if (updated && !oldStatus.equals(rentRecord.getStatus())) {
            updateDeviceStatusForOrder(existingRecord, oldStatus, rentRecord.getStatus());
        }
        
        return updated;
    }

    @Transactional
    public boolean deleteRentRecord(String id) {
        RentRecord existingRecord = getById(id);
        if (existingRecord == null) {
            return false;
        }

        // 删除订单时，如果订单是"已预约"状态，恢复设备库存为"正常库存"
        if (existingRecord.getStatus() == 0) {
            updateDeviceIfNeeded(existingRecord.getCamara(), 0);
            updateDeviceIfNeeded(existingRecord.getLens(), 0);
            updateDeviceIfNeeded(existingRecord.getOther(), 0);
        }

        return removeById(id);
    }
    
    @Scheduled(fixedRate = 600000)
    public void checkOverdueRecords() {
        LambdaQueryWrapper<RentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentRecord::getStatus, 3);
        List<RentRecord> borrowedRecords = list(wrapper);
        
        LocalDateTime now = LocalDateTime.now();
        
        for (RentRecord record : borrowedRecords) {
            if (record.getRtuntime() != null && record.getRtuntime().isBefore(now)) {
                // 使用updateRentRecordStatus方法来确保设备状态同步更新
                updateRentRecordStatus(record.getId(), 2);
            }
        }
    }
    
    private String generateId() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        LambdaQueryWrapper<RentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(RentRecord::getId, today);
        wrapper.orderByDesc(RentRecord::getId);
        wrapper.last("LIMIT 1");
        
        RentRecord lastRecord = getOne(wrapper);
        int sequence = 1;
        
        if (lastRecord != null) {
            String lastId = lastRecord.getId();
            String lastSequenceStr = lastId.substring(8);
            sequence = Integer.parseInt(lastSequenceStr) + 1;
        }
        
        return today + String.format("%04d", sequence);
    }
}
