package com.sgfb.lab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@TableName("RENT_ROOM")
public class RentRoom {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("rent_id")
    private String rentId;
    @TableField("room_id")
    private Integer roomId;
    @TableField("BRWTIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime brwtime;
    @TableField("RTNTIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime rtuntime;

    public RentRoom() {}

    public RentRoom(String rentId, Integer roomId, LocalDateTime brwtime, LocalDateTime rtuntime) {
        this.rentId = rentId;
        this.roomId = roomId;
        this.brwtime = brwtime;
        this.rtuntime = rtuntime;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getRentId() { return rentId; }
    public void setRentId(String rentId) { this.rentId = rentId; }
    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }
    public LocalDateTime getBrwtime() { return brwtime; }
    public void setBrwtime(LocalDateTime brwtime) { this.brwtime = brwtime; }
    public LocalDateTime getRtuntime() { return rtuntime; }
    public void setRtuntime(LocalDateTime rtuntime) { this.rtuntime = rtuntime; }
}
