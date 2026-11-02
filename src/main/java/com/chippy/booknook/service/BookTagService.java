package com.chippy.booknook.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.BookTag;

import java.util.List;

public interface BookTagService extends IService<BookTag>{
    // 添加书标签：查重、保存都在 BookTagServiceImpl 里
    Result<BookTag> add(BookTag bookTag);
    // 删除标签：删除逻辑在 BookTagServiceImpl 里
    Result<String> delete(Long id);
    // 查某本书的标签：条件查询在 BookTagServiceImpl 里
    Result<List<BookTag>> byBook(Long bookId);
}
