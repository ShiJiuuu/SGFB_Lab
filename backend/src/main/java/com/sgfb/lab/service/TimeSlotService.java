package com.sgfb.lab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sgfb.lab.entity.TimeSlot;
import com.sgfb.lab.mapper.TimeSlotMapper;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class TimeSlotService extends ServiceImpl<TimeSlotMapper, TimeSlot> {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public List<TimeSlot> getAllSlots() {
        LambdaQueryWrapper<TimeSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(TimeSlot::getDayOfWeek);
        return list(wrapper);
    }

    public List<TimeSlot> getEnabledSlots() {
        LambdaQueryWrapper<TimeSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimeSlot::getEnabled, 1);
        wrapper.orderByAsc(TimeSlot::getDayOfWeek);
        return list(wrapper);
    }

    public TimeSlot getSlotByDayOfWeek(int dayOfWeek) {
        LambdaQueryWrapper<TimeSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimeSlot::getDayOfWeek, dayOfWeek);
        return getOne(wrapper);
    }

    public List<String> generateTimePoints(int dayOfWeek) {
        List<TimeSlot> slots = baseMapper.selectByDayOfWeek(dayOfWeek);
        Set<String> pointSet = new TreeSet<>();

        for (TimeSlot slot : slots) {
            if (slot.getEnabled() == null || slot.getEnabled() == 0) {
                continue;
            }
            LocalTime start = slot.getTimeRangeStart();
            LocalTime end = slot.getTimeRangeEnd();
            if (start == null || end == null || !start.isBefore(end)) {
                continue;
            }
            LocalTime current = start.withMinute((start.getMinute() / 5) * 5).withSecond(0);
            while (!current.isAfter(end)) {
                pointSet.add(current.format(TIME_FORMATTER));
                current = current.plusMinutes(5);
            }
        }

        return new ArrayList<>(pointSet);
    }

    public boolean saveSlot(TimeSlot timeSlot) {
        if (timeSlot.getId() != null) {
            TimeSlot existing = getById(timeSlot.getId());
            if (existing != null) {
                existing.setDayOfWeek(timeSlot.getDayOfWeek());
                existing.setPeriodIndex(timeSlot.getPeriodIndex());
                existing.setTimeRangeStart(timeSlot.getTimeRangeStart());
                existing.setTimeRangeEnd(timeSlot.getTimeRangeEnd());
                existing.setEnabled(timeSlot.getEnabled());
                return updateById(existing);
            }
        }

        LambdaQueryWrapper<TimeSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TimeSlot::getDayOfWeek, timeSlot.getDayOfWeek());
        wrapper.eq(TimeSlot::getPeriodIndex, timeSlot.getPeriodIndex());
        TimeSlot existing = getOne(wrapper);

        if (existing != null) {
            existing.setTimeRangeStart(timeSlot.getTimeRangeStart());
            existing.setTimeRangeEnd(timeSlot.getTimeRangeEnd());
            existing.setEnabled(timeSlot.getEnabled());
            return updateById(existing);
        }

        return save(timeSlot);
    }

    public boolean deleteSlotById(int id) {
        return removeById(id);
    }
}
