package com.chippy.booknook.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chippy.booknook.service.BookTagService;
import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.BookTag;
import com.chippy.booknook.mapper.BookTagMapper;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookTagServiceImpl extends ServiceImpl<BookTagMapper, BookTag> implements BookTagService {

    //添加书标签
    @Override
    public Result<BookTag> add(BookTag bookTag) {
        // 第 1 步：查重，同一本书不能重复打同一个标签
        long count = this.lambdaQuery()
                .eq(BookTag::getBookId, bookTag.getBookId())
                .eq(BookTag::getTagName, bookTag.getTagName())
                .count();
        if (count > 0) {
            return Result.error("该标签已存在");
        }
        // 第 2 步：保存
        this.save(bookTag);
        return Result.success(bookTag);
    }

    //删除标签
    @Override
    public Result<String> delete(Long id) {
        // 第 1 步：删掉
        this.removeById(id);
        return Result.success("删除成功");
    }

    //查某本书的标签
    @Override
    public Result<List<BookTag>> byBook(Long bookId) {
        // 第 1 步：查这本书的所有标签
        List<BookTag> list = this.lambdaQuery()
                .eq(BookTag::getBookId, bookId)
                .list();
        return Result.success(list);
    }
}
