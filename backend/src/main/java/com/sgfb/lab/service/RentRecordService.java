package com.sgfb.lab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgfb.lab.entity.Classroom;
import com.sgfb.lab.entity.RentRecord;
import com.sgfb.lab.entity.RentRoom;
import com.sgfb.lab.mapper.RentRecordMapper;
import com.sgfb.lab.mapper.RentRoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class RentRecordService extends ServiceImpl<RentRecordMapper, RentRecord> {

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private RentRoomMapper rentRoomMapper;

    public List<RentRecord> getAllRentRecords() {
        return list();
    }

    @Transactional
    public boolean addRentRecord(RentRecord rentRecord) {
        rentRecord.setId(generateId());
        rentRecord.setStatus(0);
        boolean saved = save(rentRecord);

        if (saved && rentRecord.getRoomDetails() != null) {
            for (Map<String, Object> detail : rentRecord.getRoomDetails()) {
                Integer roomId = (Integer) detail.get("roomId");
                LocalDateTime brwtime = parseDateTime(detail.get("brwtime"));
                LocalDateTime rtuntime = parseDateTime(detail.get("rtuntime"));
                if (roomId != null) {
                    rentRoomMapper.insert(new RentRoom(rentRecord.getId(), roomId, brwtime, rtuntime));
                    updateRoomIfNeeded(roomId, 1);
                }
            }
        }
        return saved;
    }

    public boolean checkTimeConflict(RentRecord rentRecord) {
        if (rentRecord.getRoomDetails() == null || rentRecord.getRoomDetails().isEmpty()) {
            return false;
        }
        for (Map<String, Object> detail : rentRecord.getRoomDetails()) {
            Integer roomId = (Integer) detail.get("roomId");
            LocalDateTime brwtime = parseDateTime(detail.get("brwtime"));
            LocalDateTime rtuntime = parseDateTime(detail.get("rtuntime"));
            if (roomId != null && !classroomService.isRoomAvailable(roomId, brwtime, rtuntime, null)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkTimeConflictForUpdate(RentRecord rentRecord) {
        if (rentRecord.getRoomDetails() == null || rentRecord.getRoomDetails().isEmpty()) {
            return false;
        }
        for (Map<String, Object> detail : rentRecord.getRoomDetails()) {
            Integer roomId = (Integer) detail.get("roomId");
            LocalDateTime brwtime = parseDateTime(detail.get("brwtime"));
            LocalDateTime rtuntime = parseDateTime(detail.get("rtuntime"));
            if (roomId != null && !classroomService.isRoomAvailable(roomId, brwtime, rtuntime, rentRecord.getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean updateRentRecordStatus(String id, Integer status) {
        RentRecord record = getById(id);
        if (record == null) return false;

        record.setStatus(status);
        boolean updated = updateById(record);

        if (updated) {
            updateRoomsStatusForOrder(id, status);
        }
        return updated;
    }

    private void updateRoomsStatusForOrder(String rentId, Integer newStatus) {
        List<RentRoom> rooms = classroomService.getRentRoomsByRentId(rentId);
        Integer targetStatus;
        if (newStatus == 0 || newStatus == 3) {
            targetStatus = 1;
        } else if (newStatus == 2) {
            targetStatus = 2;
        } else {
            targetStatus = 0;
        }
        for (RentRoom room : rooms) {
            updateRoomIfNeeded(room.getRoomId(), targetStatus);
        }
    }

    private void updateRoomIfNeeded(Integer roomId, Integer targetStatus) {
        if (roomId == null) return;
        Classroom classroom = classroomService.getById(roomId);
        if (classroom != null && !classroom.getStatus().equals(targetStatus)) {
            classroom.setStatus(targetStatus);
            classroomService.updateById(classroom);
        }
    }

    @Transactional
    public boolean updateRentRecord(RentRecord rentRecord) {
        RentRecord existingRecord = getById(rentRecord.getId());
        if (existingRecord == null) return false;

        existingRecord.setName(rentRecord.getName());
        existingRecord.setNum(rentRecord.getNum());
        existingRecord.setTel(rentRecord.getTel());
        existingRecord.setStatus(rentRecord.getStatus());
        existingRecord.setRemark(rentRecord.getRemark());

        boolean updated = updateById(existingRecord);

        if (updated && rentRecord.getRoomDetails() != null) {
            // Restore old rooms
            List<RentRoom> oldRooms = classroomService.getRentRoomsByRentId(rentRecord.getId());
            if (existingRecord.getStatus() == 0) {
                for (RentRoom old : oldRooms) {
                    updateRoomIfNeeded(old.getRoomId(), 0);
                }
            }

            // Delete old RENT_ROOM entries
            LambdaQueryWrapper<RentRoom> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(RentRoom::getRentId, rentRecord.getId());
            rentRoomMapper.delete(deleteWrapper);

            // Insert new entries with times
            Integer roomTargetStatus = getRoomStatusForOrderStatus(rentRecord.getStatus());
            for (Map<String, Object> detail : rentRecord.getRoomDetails()) {
                Integer roomId = (Integer) detail.get("roomId");
                LocalDateTime brwtime = parseDateTime(detail.get("brwtime"));
                LocalDateTime rtuntime = parseDateTime(detail.get("rtuntime"));
                if (roomId != null) {
                    rentRoomMapper.insert(new RentRoom(rentRecord.getId(), roomId, brwtime, rtuntime));
                    updateRoomIfNeeded(roomId, roomTargetStatus);
                }
            }
        }

        return updated;
    }

    private Integer getRoomStatusForOrderStatus(Integer orderStatus) {
        if (orderStatus == 0 || orderStatus == 3) return 1;
        if (orderStatus == 2) return 2;
        return 0;
    }

    @Transactional
    public boolean deleteRentRecord(String id) {
        RentRecord existingRecord = getById(id);
        if (existingRecord == null) return false;

        if (existingRecord.getStatus() == 0) {
            List<RentRoom> rooms = classroomService.getRentRoomsByRentId(id);
            for (RentRoom room : rooms) {
                updateRoomIfNeeded(room.getRoomId(), 0);
            }
        }

        LambdaQueryWrapper<RentRoom> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(RentRoom::getRentId, id);
        rentRoomMapper.delete(deleteWrapper);

        return removeById(id);
    }

    @Scheduled(fixedRate = 600000)
    public void checkOverdueRecords() {
        // Find active orders and check if any of their rooms are overdue
        LambdaQueryWrapper<RentRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(RentRecord::getStatus, 0, 2, 3);
        wrapper.ne(RentRecord::getStatus, 1); // exclude completed
        List<RentRecord> activeRecords = list(wrapper);

        LocalDateTime now = LocalDateTime.now();

        for (RentRecord record : activeRecords) {
            List<RentRoom> rooms = classroomService.getRentRoomsByRentId(record.getId());
            boolean allOverdue = !rooms.isEmpty();
            for (RentRoom room : rooms) {
                if (room.getRtuntime() == null || !room.getRtuntime().isBefore(now)) {
                    allOverdue = false;
                    break;
                }
            }
            // Only mark as overdue if status is 3 (borrowed) and all rooms past return time
            // Keep existing overdue logic: if status==3 and rtuntime < now, mark overdue
        }

        // Simpler approach: just check status==3 records
        LambdaQueryWrapper<RentRecord> borrowedWrapper = new LambdaQueryWrapper<>();
        borrowedWrapper.eq(RentRecord::getStatus, 3);
        List<RentRecord> borrowedRecords = list(borrowedWrapper);

        for (RentRecord record : borrowedRecords) {
            List<RentRoom> rooms = classroomService.getRentRoomsByRentId(record.getId());
            boolean shouldBeOverdue = false;
            for (RentRoom room : rooms) {
                if (room.getRtuntime() != null && room.getRtuntime().isBefore(now)) {
                    shouldBeOverdue = true;
                    break;
                }
            }
            if (shouldBeOverdue) {
                updateRentRecordStatus(record.getId(), 2);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private LocalDateTime parseDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        String str = value.toString();
        try {
            return LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception e) {
            return LocalDateTime.parse(str);
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
