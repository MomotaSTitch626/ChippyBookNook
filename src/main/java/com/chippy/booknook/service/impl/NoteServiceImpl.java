package com.chippy.booknook.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.Book;
import com.chippy.booknook.entity.LendRecord;
import com.chippy.booknook.entity.Note;
import com.chippy.booknook.mapper.NoteMapper;
import com.chippy.booknook.service.BookService;
import com.chippy.booknook.service.LendRecordService;
import com.chippy.booknook.service.NoteService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    // 写笔记要校验"书存在 + 是不是自己的书"，所以注入 BookService；
    // 不是自己的书还要查借阅记录，所以注入 LendRecordService（单向依赖，无循环）
    @Autowired
    private BookService bookService;
    @Autowired
    private LendRecordService lendRecordService;

    //添加笔记
    @Override
    public Result<Note> add(Note note, Long userId) {
        // 第 1 步：书必须存在
        Book book = bookService.getById(note.getBookId());
        if (book == null) {
            return Result.error("图书不存在");
        }
        // 第 2 步：校验只有"自己的书"或"正在借阅中的书"才能写笔记
        boolean isOwner = book.getOwnerId().equals(userId);
        if (!isOwner) {
            // 不是自己的，再查自己有没有这本书的"借出中"记录（status=0 表示还没还）
            LendRecord lent = lendRecordService.lambdaQuery()
                    .eq(LendRecord::getBookId, note.getBookId())
                    .eq(LendRecord::getBorrowerId, userId)
                    .eq(LendRecord::getStatus, 0)
                    .one();
            // 既不是自己的书，也没有借阅记录 → 拒绝写笔记
            if (lent == null) {
                return Result.error("只能给自己的书或借阅中的书写笔记");
            }
        }
        // 第 3 步：笔记归属当前登录用户并保存
        note.setUserId(userId);
        this.save(note);
        return Result.success(note);
    }

    //删除读书笔记
    @Override
    public Result<String> delete(Long id, Long userId) {
        // 第 1 步：笔记必须存在
        Note dbNote = this.getById(id);
        if (dbNote == null) {
            return Result.error("笔记不存在");
        }
        // 第 2 步：只能删除自己的笔记（防越权）
        if (!userId.equals(dbNote.getUserId())) {
            return Result.error("只能删除自己的笔记");
        }
        // 第 3 步：删掉
        this.removeById(id);
        return Result.success("删除成功");
    }

    //查某本书上我的笔记
    @Override
    public Result<List<Note>> byBook(Long bookId, Long userId) {
        // 第 1 步：按书 + 用户两个条件查询
        List<Note> list = this.lambdaQuery()
                .eq(Note::getBookId, bookId)
                .eq(Note::getUserId, userId)
                .list();
        return Result.success(list);
    }

    //查找我的全部笔记
    @Override
    public Result<List<Note>> my(Long userId) {
        // 第 1 步：按用户条件查询
        List<Note> list = this.lambdaQuery()
                .eq(Note::getUserId, userId)
                .list();
        return Result.success(list);
    }

    //修改笔记
    @Override
    public Result<String> update(Note note, Long userId) {
        // 第 1 步：笔记必须存在
        Note dbNote = this.getById(note.getId());
        if (dbNote == null) {
            return Result.error("笔记不存在");
        }
        // 第 2 步：只能修改自己的笔记（防越权）
        if (!dbNote.getUserId().equals(userId)) {
            return Result.error("只能修改自己的笔记");
        }
        // 第 3 步：只更新内容字段（白名单式更新，不整个覆盖）
        dbNote.setContent(note.getContent());
        this.updateById(dbNote);
        return Result.success("更新成功");
    }
}
