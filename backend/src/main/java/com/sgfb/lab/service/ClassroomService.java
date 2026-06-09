package com.sgfb.lab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgfb.lab.entity.Classroom;
import com.sgfb.lab.entity.RentRecord;
import com.sgfb.lab.entity.RentRoom;
import com.sgfb.lab.mapper.ClassroomMapper;
import com.sgfb.lab.mapper.RentRecordMapper;
import com.sgfb.lab.mapper.RentRoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClassroomService extends ServiceImpl<ClassroomMapper, Classroom> {

    @Autowired
    private RentRecordMapper rentRecordMapper;

    @Autowired
    private RentRoomMapper rentRoomMapper;

    public List<Classroom> getAllClassrooms() {
        return list();
    }

    /**
     * Get RentRoom entries for a given order (with time info).
     */
    public List<RentRoom> getRentRoomsByRentId(String rentId) {
        LambdaQueryWrapper<RentRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RentRoom::getRentId, rentId);
        return rentRoomMapper.selectList(wrapper);
    }

    /**
     * Get room IDs for a given order.
     */
    public List<Integer> getRoomIdsByRentId(String rentId) {
        return getRentRoomsByRentId(rentId).stream()
                .map(RentRoom::getRoomId)
                .collect(Collectors.toList());
    }

    /**
     * Get available classrooms for a specific time window.
     */
    public List<Classroom> getAvailableClassroomsByTime(LocalDateTime borrowTime, LocalDateTime returnTime) {
        LambdaQueryWrapper<Classroom> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Classroom::getStatus, 0, 1);
        List<Classroom> available = list(wrapper);

        if (borrowTime == null || returnTime == null) {
            return available;
        }

        Set<Integer> occupiedRoomIds = getOccupiedRoomIdsForTime(borrowTime, returnTime, null);

        return available.stream()
                .filter(room -> !occupiedRoomIds.contains(room.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Check if a specific room is available for a given time window.
     */
    public boolean isRoomAvailable(Integer roomId, LocalDateTime borrowTime, LocalDateTime returnTime, String excludeRecordId) {
        if (roomId == null || borrowTime == null || returnTime == null) {
            return true;
        }
        Set<Integer> occupiedRoomIds = getOccupiedRoomIdsForTime(borrowTime, returnTime, excludeRecordId);
        return !occupiedRoomIds.contains(roomId);
    }

    /**
     * Get room IDs occupied during a time window, using per-room times from RENT_ROOM.
     */
    private Set<Integer> getOccupiedRoomIdsForTime(LocalDateTime requestStart, LocalDateTime requestEnd, String excludeRecordId) {
        Set<Integer> occupied = new HashSet<>();

        // Get all active orders
        LambdaQueryWrapper<RentRecord> rentWrapper = new LambdaQueryWrapper<>();
        rentWrapper.in(RentRecord::getStatus, 0, 2, 3);
        List<RentRecord> activeRecords = rentRecordMapper.selectList(rentWrapper);

        for (RentRecord record : activeRecords) {
            if (excludeRecordId != null && record.getId().equals(excludeRecordId)) {
                continue;
            }
            // Get this order's rooms with their individual times
            List<RentRoom> rooms = getRentRoomsByRentId(record.getId());
            for (RentRoom room : rooms) {
                if (room.getBrwtime() != null && room.getRtuntime() != null
                        && isTimeOverlap(requestStart, requestEnd, room.getBrwtime(), room.getRtuntime())) {
                    occupied.add(room.getRoomId());
                }
            }
        }
        return occupied;
    }

    private boolean isTimeOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    public boolean isRoomInUse(Integer roomId) {
        if (roomId == null) return false;
        LambdaQueryWrapper<RentRecord> rentWrapper = new LambdaQueryWrapper<>();
        rentWrapper.in(RentRecord::getStatus, 0, 2, 3);
        List<RentRecord> activeRecords = rentRecordMapper.selectList(rentWrapper);
        for (RentRecord record : activeRecords) {
            List<Integer> roomIds = getRoomIdsByRentId(record.getId());
            if (roomIds.contains(roomId)) return true;
        }
        return false;
    }
}
