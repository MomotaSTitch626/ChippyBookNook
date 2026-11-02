package com.chippy.booknook.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.Note;

import java.util.List;

public interface NoteService extends IService<Note> {
    // 写笔记：书存在校验、"自己的书或借阅中的书"归属校验都在 NoteServiceImpl 里
    Result<Note> add(Note note, Long userId);
    // 删除笔记：存在性 + 归属校验都在 NoteServiceImpl 里
    Result<String> delete(Long id, Long userId);
    // 查某本书上我的笔记：条件查询在 NoteServiceImpl 里
    Result<List<Note>> byBook(Long bookId, Long userId);
    // 查找我的全部笔记：条件查询在 NoteServiceImpl 里
    Result<List<Note>> my(Long userId);
    // 修改笔记：存在性 + 归属校验 + 字段更新都在 NoteServiceImpl 里
    Result<String> update(Note note, Long userId);
}
