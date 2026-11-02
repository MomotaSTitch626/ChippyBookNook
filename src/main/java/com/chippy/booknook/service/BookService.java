package com.chippy.booknook.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.Book;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface BookService extends IService<Book>{
    // 删除自己的书：查存在、校验书主、删除都在 BookServiceImpl 里
    Result<String> delete(Long userId, Long id);
    // 新增书：设置归属 ownerId、保存都在 BookServiceImpl 里
    Result<Book> add(Book book, Long userId);
    // 管理员删任意书：管理员校验、查存在、删除都在 BookServiceImpl 里
    Result<String> adminDelete(Long adminId, Long id);
    // 修改阅读状态：归属校验、字段白名单、更新都在 BookServiceImpl 里
    Result<String> updateStatus(Book book, Long userId);
    // 我的书屋：自己的书 + 借来未还的书，四步并集逻辑都在 BookServiceImpl 里
    Result<List<Book>> my(Long userId);
    // 模糊搜索（书名/作者）：查询条件构建 + 分页在 BookServiceImpl 里
    Result<IPage<Book>> search(String keyword, long page, long pageSize);
    // 按标签查书：标签关联查询 + 分页在 BookServiceImpl 里
    Result<IPage<Book>> byTag(String tagName, long page, long pageSize);
    // 上传图书：类型校验、文件保存、TXT 字数统计、写库都在 BookServiceImpl 里
    Result<String> upload(MultipartFile file, MultipartFile cover, String name, String author,
                          String format, String publishTime, String category, String status,
                          Long userId) throws IOException;
    // 在线阅读：查存在、TXT 校验、读文件都在 BookServiceImpl 里
    Result<String> read(Long id) throws IOException;
}
