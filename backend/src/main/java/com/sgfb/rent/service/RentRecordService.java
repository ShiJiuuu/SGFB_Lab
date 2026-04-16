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
        boolean success = save(rentRecord);
        
        if (success) {
            updateDeviceStatus(rentRecord.getCamara(), 1);
            updateDeviceStatus(rentRecord.getLens(), 1);
            updateDeviceStatus(rentRecord.getOther(), 1);
        }
        
        return success;
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
    
    private void updateDeviceStatus(Integer deviceId, Integer status) {
        if (deviceId != null) {
            Device device = deviceService.getById(deviceId);
            if (device != null) {
                device.setStatus(status);
                deviceService.updateById(device);
            }
        }
    }
    
    @Transactional
    public boolean updateRentRecordStatus(String id, Integer status) {
        RentRecord record = getById(id);
        if (record == null) {
            return false;
        }
        
        Integer oldStatus = record.getStatus();
        record.setStatus(status);
        boolean success = updateById(record);
        
        if (success) {
            if (oldStatus == 0 && status == 1) {
                updateDeviceStatus(record.getCamara(), 0);
                updateDeviceStatus(record.getLens(), 0);
                updateDeviceStatus(record.getOther(), 0);
            } else if (oldStatus == 0 && status == 2) {
                updateDeviceStatus(record.getCamara(), 2);
                updateDeviceStatus(record.getLens(), 2);
                updateDeviceStatus(record.getOther(), 2);
            } else if ((oldStatus == 1 || oldStatus == 2) && status == 0) {
                updateDeviceStatus(record.getCamara(), 1);
                updateDeviceStatus(record.getLens(), 1);
                updateDeviceStatus(record.getOther(), 1);
            } else if (oldStatus == 2 && status == 1) {
                updateDeviceStatus(record.getCamara(), 0);
                updateDeviceStatus(record.getLens(), 0);
                updateDeviceStatus(record.getOther(), 0);
            } else if (oldStatus == 1 && status == 2) {
                updateDeviceStatus(record.getCamara(), 2);
                updateDeviceStatus(record.getLens(), 2);
                updateDeviceStatus(record.getOther(), 2);
            }
        }
        
        return success;
    }
    
    public boolean updateRentRecord(RentRecord rentRecord) {
        RentRecord existingRecord = getById(rentRecord.getId());
        if (existingRecord == null) {
            return false;
        }

        Integer oldStatus = existingRecord.getStatus();
        Integer newStatus = rentRecord.getStatus();
        Integer oldCamara = existingRecord.getCamara();
        Integer oldLens = existingRecord.getLens();
        Integer oldOther = existingRecord.getOther();
        Integer newCamara = rentRecord.getCamara();
        Integer newLens = rentRecord.getLens();
        Integer newOther = rentRecord.getOther();

        existingRecord.setName(rentRecord.getName());
        existingRecord.setNum(rentRecord.getNum());
        existingRecord.setTel(rentRecord.getTel());
        existingRecord.setCamara(newCamara);
        existingRecord.setLens(newLens);
        existingRecord.setOther(newOther);
        existingRecord.setBrwtime(rentRecord.getBrwtime());
        existingRecord.setRtuntime(rentRecord.getRtuntime());
        existingRecord.setStatus(newStatus);
        existingRecord.setRemark(rentRecord.getRemark());

        boolean success = updateById(existingRecord);

        if (success) {
            if (oldStatus != null && oldStatus == 0) {
                updateDeviceStatus(oldCamara, 0);
                updateDeviceStatus(oldLens, 0);
                updateDeviceStatus(oldOther, 0);
            }

            if (newStatus != null && newStatus == 0) {
                updateDeviceStatus(newCamara, 1);
                updateDeviceStatus(newLens, 1);
                updateDeviceStatus(newOther, 1);
            } else if (newStatus != null && newStatus == 2) {
                updateDeviceStatus(newCamara, 2);
                updateDeviceStatus(newLens, 2);
                updateDeviceStatus(newOther, 2);
            }
        }

        return success;
    }

    @Transactional
    public boolean deleteRentRecord(String id) {
        RentRecord existingRecord = getById(id);
        if (existingRecord == null) {
            return false;
        }

        boolean success = removeById(id);
        if (!success) {
            return false;
        }

        if (existingRecord.getStatus() != null && (existingRecord.getStatus() == 0 || existingRecord.getStatus() == 2)) {
            updateDeviceStatus(existingRecord.getCamara(), 0);
            updateDeviceStatus(existingRecord.getLens(), 0);
            updateDeviceStatus(existingRecord.getOther(), 0);
        }
        return true;
    }
    
    @Scheduled(fixedRate = 600000)
    public void checkOverdueRecords() {
        LambdaQueryWrapper<RentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentRecord::getStatus, 0);
        List<RentRecord> activeRecords = list(wrapper);
        
        LocalDateTime now = LocalDateTime.now();
        
        for (RentRecord record : activeRecords) {
            if (record.getRtuntime() != null && record.getRtuntime().isBefore(now)) {
                record.setStatus(2);
                updateById(record);
                updateDeviceStatus(record.getCamara(), 2);
                updateDeviceStatus(record.getLens(), 2);
                updateDeviceStatus(record.getOther(), 2);
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
