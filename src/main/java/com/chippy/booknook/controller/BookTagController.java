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

    //添加书标签
    @PostMapping
    public Result<?> add(@RequestBody BookTag bookTag) {
        long count = bookTagService.lambdaQuery()
                .eq(BookTag::getBookId, bookTag.getBookId())
                .eq(BookTag::getTagName, bookTag.getTagName())
                .count();
        if(count > 0){
            return Result.error("该标签已存在");
        }
        bookTagService.save(bookTag);
        return Result.success(bookTag);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        bookTagService.removeById(id);
        return Result.success("删除成功");
    }

    //查某本书的标签
    @GetMapping("/book/{bookId}")
    public Result<List<BookTag>> byBook(@PathVariable Long bookId) {
        List<BookTag> list = bookTagService.lambdaQuery()
                .eq(BookTag::getBookId, bookId)
                .list();
        return Result.success(list);
    }

}
