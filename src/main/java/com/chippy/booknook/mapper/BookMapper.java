package com.chippy.booknook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chippy.booknook.entity.Book;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
}
