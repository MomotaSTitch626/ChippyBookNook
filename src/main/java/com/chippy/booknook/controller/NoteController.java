package com.chippy.booknook.controller;


import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.Note;
import com.chippy.booknook.service.NoteService;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/note")
public class NoteController {
    @Autowired
    private NoteService noteService;


    @GetMapping("/{id}")
    public Result<Note> getById(@PathVariable Long id){
        return Result.success(noteService.getById(id));
    }

    @GetMapping("/list")
    public Result<List<Note>> list(){
        return Result.success(noteService.list());
    }

    //添加笔记：Controller 解析身份、调 Service；书存在校验、归属校验都在 NoteServiceImpl.add 里
    @PostMapping
    public Result<Note> add(@RequestBody Note note, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        return noteService.add(note, userId);
    }

    //删除读书笔记：Controller 解析身份、调 Service；存在性、归属校验都在 NoteServiceImpl.delete 里
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        return noteService.delete(id, userId);
    }

    //查某本书上我的笔记：条件查询在 NoteServiceImpl.byBook 里
    @GetMapping("/book/{bookId}")
    public Result<List<Note>> byBook(@PathVariable Long bookId, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        return noteService.byBook(bookId, userId);
    }

    //查找我的全部笔记：条件查询在 NoteServiceImpl.my 里
    @GetMapping("/my")
    public Result<List<Note>> my(HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        return noteService.my(userId);
    }

    //修改笔记：Controller 解析身份、调 Service；存在性、归属校验都在 NoteServiceImpl.update 里
    @PutMapping("/update")
    public Result<String> update(@RequestBody Note note, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        return noteService.update(note, userId);
    }
}
