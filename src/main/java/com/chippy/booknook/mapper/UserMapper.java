package com.chippy.booknook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chippy.booknook.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
