package com.chippy.booknook.controller;

import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.BookTag;
import com.chippy.booknook.service.BookTagService;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/book-tag")
public class BookTagController {
    @Autowired
    private BookTagService bookTagService;

    @GetMapping("/{id}")
    public Result<BookTag> getById(@PathVariable Long id) {
        return Result.success(bookTagService.getById(id));
    }

    @GetMapping("/list")
    public Result<List<BookTag>> list(){
        return Result.success(bookTagService.list());
    }

    //添加书标签：Controller 收参、调 Service；查重、保存都在 BookTagServiceImpl.add 里
    @PostMapping
    public Result<BookTag> add(@RequestBody BookTag bookTag) {
        return bookTagService.add(bookTag);
    }

    //删除标签：Controller 收参、调 Service；删除逻辑在 BookTagServiceImpl.delete 里
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        return bookTagService.delete(id);
    }

    //查某本书的标签：条件查询在 BookTagServiceImpl.byBook 里
    @GetMapping("/book/{bookId}")
    public Result<List<BookTag>> byBook(@PathVariable Long bookId) {
        return bookTagService.byBook(bookId);
    }

}
