package com.sgfb.rent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sgfb.rent.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
