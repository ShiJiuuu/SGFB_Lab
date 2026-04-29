package com.sgfb.rent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.time.LocalTime;

@TableName("TIME_SLOT")
public class TimeSlot {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer dayOfWeek;

    private LocalTime timeRangeStart;

    private LocalTime timeRangeEnd;

    private Integer enabled;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(Integer dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getTimeRangeStart() { return timeRangeStart; }
    public void setTimeRangeStart(LocalTime timeRangeStart) { this.timeRangeStart = timeRangeStart; }

    public LocalTime getTimeRangeEnd() { return timeRangeEnd; }
    public void setTimeRangeEnd(LocalTime timeRangeEnd) { this.timeRangeEnd = timeRangeEnd; }

    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public String getDayOfWeekName() {
        String[] names = {"", "周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        if (dayOfWeek != null && dayOfWeek >= 1 && dayOfWeek <= 7) {
            return names[dayOfWeek];
        }
        return "";
    }
}
