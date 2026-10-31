package com.chippy.booknook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chippy.booknook.entity.Note;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}
