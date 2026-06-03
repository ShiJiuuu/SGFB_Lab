package com.sgfb.lab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sgfb.lab.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
