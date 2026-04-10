package com.sgfb.rent.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AnnounceMapper {

    @Select("SELECT ANNOUNCE FROM RENT_ANC LIMIT 1")
    String getAnnounce();

    @Update("UPDATE RENT_ANC SET ANNOUNCE = #{announce}")
    int updateAnnounce(String announce);
}
