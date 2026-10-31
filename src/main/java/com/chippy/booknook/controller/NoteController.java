package com.chippy.booknook.controller;


import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.Book;
import com.chippy.booknook.entity.Note;
import com.chippy.booknook.service.BookService;
import com.chippy.booknook.service.NoteService;
import com.chippy.booknook.entity.LendRecord;
import com.chippy.booknook.service.LendRecordService;


import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/note")
public class NoteController {
    @Autowired
    private NoteService noteService;
    @Autowired
    private BookService bookService;
    @Autowired
    private LendRecordService lendRecordService;


    @GetMapping("/{id}")
    public Result<Note> getById(@PathVariable Long id){
        return Result.success(noteService.getById(id));
    }

    @GetMapping("/list")
    public Result<List<Note>> list(){
        return Result.success(noteService.list());
    }

    //添加笔记
    @PostMapping
    public Result<Note> add(@RequestBody Note note, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        Book book = bookService.getById(note.getBookId());
        if(book == null){
            return Result.error("图书不存在");
        }
        //校验：只有"自己的书"或"正在借阅中的书"才能写笔记
        //先看这本书是不是自己的（ownerId 等于当前登录用户就是自己的书）
        boolean isOwner = book.getOwnerId().equals(userId);
        if (!isOwner) {
            //不是自己的，再查自己有没有这本书的"借出中"记录（status=0 表示还没还）
            LendRecord lent = lendRecordService.lambdaQuery()
                    .eq(LendRecord::getBookId, note.getBookId())
                    .eq(LendRecord::getBorrowerId, userId)
                    .eq(LendRecord::getStatus, 0)
                    .one();
            //既不是自己的书，也没有借阅记录 → 拒绝写笔记
            if (lent == null) {
                return Result.error("只能给自己的书或借阅中的书写笔记");
            }
        }
        note.setUserId(userId);
        noteService.save(note);
        return Result.success(note);
    }

    //删除读书笔记
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        Note dbNote = noteService.getById(id);
        if(dbNote == null){
            return Result.error("笔记不存在");
        }
        if(!userId.equals(dbNote.getUserId())){
            return Result.error("只能删除自己的笔记");
        }
        noteService.removeById(id);
        return Result.success("删除成功");
    }

    //查某本书上我的笔记
    @GetMapping("/book/{bookId}")
    public Result<List<Note>> byBook(@PathVariable Long bookId, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        List<Note> list = noteService.lambdaQuery()
                .eq(Note::getBookId, bookId)
                .eq(Note::getUserId, userId)
                .list();
        return Result.success(list);
    }

    //查找我的全部笔记
    @GetMapping("/my")
    public Result<List<Note>> my(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        List<Note> list = noteService.lambdaQuery()
                .eq(Note::getUserId, userId)
                .list();
        return Result.success(list);
    }

    //修改笔记
    @PutMapping("/update")
    public Result<?> update(@RequestBody Note note, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        Note dbNote = noteService.getById(note.getId());
        if(dbNote == null){
            return Result.error("笔记不存在");
        }
        if(!dbNote.getUserId().equals(userId)){
            return Result.error("只能修改自己的笔记");
        }
        dbNote.setContent(note.getContent());
        noteService.updateById(dbNote);
        return Result.success("更新成功");
    }
}
