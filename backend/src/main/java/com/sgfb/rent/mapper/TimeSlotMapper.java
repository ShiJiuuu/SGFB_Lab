package com.sgfb.rent.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sgfb.rent.entity.TimeSlot;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface TimeSlotMapper extends BaseMapper<TimeSlot> {
    default List<TimeSlot> selectByDayOfWeek(int dayOfWeek) {
        return selectList(new LambdaQueryWrapper<TimeSlot>()
            .eq(TimeSlot::getDayOfWeek, dayOfWeek)
            .orderByAsc(TimeSlot::getPeriodIndex));
    }
}
