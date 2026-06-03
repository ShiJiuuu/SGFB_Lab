package com.sgfb.lab.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

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
    @TableField(value = "CAMARA", updateStrategy = FieldStrategy.IGNORED)
    private Integer camara;
    @TableField(value = "LENS", updateStrategy = FieldStrategy.IGNORED)
    private Integer lens;
    @TableField(value = "OTHER", updateStrategy = FieldStrategy.IGNORED)
    private Integer other;
    @TableField("BRWTIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime brwtime;
    @TableField("RTNTIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime rtuntime;
    @TableField("STATUS")
    private Integer status;
    @TableField("REMARK")
    private String remark;
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNum() { return num; }
    public void setNum(String num) { this.num = num; }
    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }
    public Integer getCamara() { return camara; }
    public void setCamara(Integer camara) { this.camara = camara; }
    public Integer getLens() { return lens; }
    public void setLens(Integer lens) { this.lens = lens; }
    public Integer getOther() { return other; }
    public void setOther(Integer other) { this.other = other; }
    public LocalDateTime getBrwtime() { return brwtime; }
    public void setBrwtime(LocalDateTime brwtime) { this.brwtime = brwtime; }
    public LocalDateTime getRtuntime() { return rtuntime; }
    public void setRtuntime(LocalDateTime rtuntime) { this.rtuntime = rtuntime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
