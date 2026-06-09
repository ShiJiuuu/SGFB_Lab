package com.sgfb.lab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@TableName("RENT_LIST")
public class RentRecord {
    @TableId(type = IdType.INPUT)
    private String id;
    @TableField("NAME")
    private String name;
    @TableField("NUM")
    private String num;
    @TableField("TEL")
    private String tel;
    @TableField("STATUS")
    private Integer status;
    @TableField("REMARK")
    private String remark;

    // Not DB columns — JSON only for receiving per-room data
    @TableField(exist = false)
    @JsonProperty("roomDetails")
    private List<Map<String, Object>> roomDetails;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNum() { return num; }
    public void setNum(String num) { this.num = num; }
    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<Map<String, Object>> getRoomDetails() { return roomDetails; }
    public void setRoomDetails(List<Map<String, Object>> roomDetails) { this.roomDetails = roomDetails; }
}
