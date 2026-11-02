package com.chippy.booknook.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.Book;
import com.chippy.booknook.entity.BookTag;
import com.chippy.booknook.entity.LendRecord;
import com.chippy.booknook.entity.User;
import com.chippy.booknook.mapper.BookMapper;
import com.chippy.booknook.service.BookService;
import com.chippy.booknook.service.BookTagService;
import com.chippy.booknook.service.LendRecordService;
import com.chippy.booknook.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    // 上传目录：从配置读取（application.yml 的 file.upload-dir），配置里写的是相对路径
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 相对路径解析后的绝对路径：IDEA 里跑基于项目根，打 jar 跑基于 jar 所在目录
    private File uploadRoot;

    // 启动时把相对路径转成绝对路径——transferTo 遇到相对路径会被 Tomcat 拼到它的临时目录导致找不到路径
    @PostConstruct
    public void initUploadRoot() {
        this.uploadRoot = new File(uploadDir).getAbsoluteFile();
    }

    // 借书业务要判断"是不是管理员"，所以这里注入 UserService 来查用户
    @Autowired
    private UserService userService;

    // 我的书屋要查借阅记录，所以这里注入 LendRecordService
    @Autowired
    private LendRecordService lendRecordService;

    // 按标签查书要先查 book_tag 表，所以这里注入 BookTagService
    @Autowired
    private BookTagService bookTagService;

    //删除自己的书（仅限书主）
    @Override
    public Result<String> delete(Long userId, Long id) {
        // 第 1 步：查这本书存在吗
        Book dbBook = this.getById(id);
        if (dbBook == null) {
            return Result.error("图书不存在");
        }
        // 第 2 步：校验这本书是不是当前用户的（书主才能删）
        if (!userId.equals(dbBook.getOwnerId())) {
            return Result.error("只能删自己的书");
        }
        // 第 3 步：删掉
        this.removeById(id);
        return Result.success("删除成功");
    }

    //新增书：把书标记为当前用户所有
    @Override
    public Result<Book> add(Book book, Long userId) {
        book.setOwnerId(userId);   //归属：这本书的 owner 是当前登录用户
        this.save(book);
        return Result.success(book);
    }

    //管理员删任意书
    @Override
    public Result<String> adminDelete(Long adminId, Long id) {
        // 第 1 步：先确定操作者是不是管理员
        User admin = userService.getById(adminId);
        if (admin.getRole() == null || admin.getRole() != 1) {
            return Result.error("没有管理员权限");
        }
        // 第 2 步：查目标书存在吗
        Book book = this.getById(id);
        if (book == null) {
            return Result.error("图书不存在");
        }
        // 第 3 步：删掉
        this.removeById(id);
        return Result.success("删除成功");
    }

    //修改阅读状态（未读/在读/已读、进度、评分）
    @Override
    public Result<String> updateStatus(Book book, Long userId) {
        // 第 1 步：查这本书存在吗
        Book dbBook = this.getById(book.getId());
        if (dbBook == null) {
            return Result.error("图书不存在");
        }
        // 第 2 步：校验这本书是不是当前用户的（书主才能改状态）
        if (!dbBook.getOwnerId().equals(userId)) {
            return Result.error("只能修改自己的书");
        }
        // 第 3 步：字段白名单——前端传的整个对象不可信，只把允许改的 3 个字段拷到数据库真身上
        dbBook.setReadStatus(book.getReadStatus());
        dbBook.setProgress(book.getProgress());
        dbBook.setRating(book.getRating());
        // 第 4 步：把改好的真身存回去
        this.updateById(dbBook);
        return Result.success("更新成功");
    }

    //我的书屋：我的书 + 我借来未还的书
    @Override
    public Result<List<Book>> my(Long userId) {
        // 第 1 步：查我自己的书，标记 source = own
        List<Book> myBooks = this.lambdaQuery()
                .eq(Book::getOwnerId, userId)
                .list();
        for (Book b : myBooks) {
            b.setSource("own");
        }
        // 第 2 步：查我借来未还的记录（status=0），拿到书的 id 列表
        List<LendRecord> records = lendRecordService.lambdaQuery()
                .eq(LendRecord::getBorrowerId, userId)
                .eq(LendRecord::getStatus, 0)
                .list();
        // 把 bookId → 借阅记录id 存成映射，还书时要用记录id
        Map<Long, Long> recordIdMap = new HashMap<>();
        for (LendRecord r : records) {
            recordIdMap.put(r.getBookId(), r.getId());
        }
        List<Long> bookIds = records.stream().map(LendRecord::getBookId).toList();
        // 第 3 步：这些 id 对应的书，标记 source = lent
        List<Book> lentBooks = bookIds.isEmpty()
                ? new ArrayList<>()
                : this.lambdaQuery().in(Book::getId, bookIds).list();
        for (Book b : lentBooks) {
            b.setSource("lent");
            b.setRecordId(recordIdMap.get(b.getId()));   //借来的书随身带上借阅记录id
        }
        // 第 4 步：合并返回
        myBooks.addAll(lentBooks);
        return Result.success(myBooks);
    }

    //模糊搜索（书名/作者）
    @Override
    public Result<IPage<Book>> search(String keyword, long page, long pageSize) {
        // 拼查询条件：书名 like 关键词 或 作者 like 关键词
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<Book>()
                .like(Book::getName, keyword)
                .or()
                .like(Book::getAuthor, keyword);
        Page<Book> p = new Page<>(page, pageSize);
        this.page(p, wrapper);
        return Result.success(p);
    }

    //按标签查书（分页）
    @Override
    public Result<IPage<Book>> byTag(String tagName, long page, long pageSize) {
        // 第 1 步：查数据库里所有带这个标签的记录
        List<BookTag> bookTags = bookTagService.lambdaQuery()
                .eq(BookTag::getTagName, tagName)
                .list();
        // 第 2 步：没有这个标签，返回一个空页（records 空、total 0）
        if (bookTags.isEmpty()) {
            return Result.success(new Page<>(page, pageSize));
        }
        // 第 3 步：有标签，把标签关联的书 id 收集成一个列表
        List<Long> bookIds = new ArrayList<>();
        for (BookTag bt : bookTags) {
            bookIds.add(bt.getBookId());
        }
        // 第 4 步：按标签分页查书
        Page<Book> p = new Page<>(page, pageSize);
        this.page(p, new LambdaQueryWrapper<Book>().in(Book::getId, bookIds));
        return Result.success(p);
    }

    //上传图书：保存书文件 + 可选封面 + TXT 字数统计 + 写数据库
    @Override
    public Result<String> upload(MultipartFile file, MultipartFile cover, String name, String author,
                                 String format, String publishTime, String category, String status,
                                 Long userId) throws IOException {
        // 第 1 步：校验书文件类型，只允许 txt / pdf / epub
        String ext = getExt(file.getOriginalFilename());
        if (!ext.matches("txt|pdf|epub")) {
            return Result.error("书文件只支持 txt/pdf/epub");
        }
        // 第 2 步：生成唯一文件名，防止重名覆盖
        String uuid = UUID.randomUUID().toString();
        String filePath = "books/" + uuid + "." + ext;
        // 第 3 步：保存书文件到 uploads 目录，用绝对路径避免被 Tomcat 拼到临时目录
        File targetFile = new File(uploadRoot, filePath);
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        file.transferTo(targetFile);
        // 第 4 步：保存封面（可选）
        String coverPath = null;
        if (cover != null && !cover.isEmpty()) {
            String coverExt = getExt(cover.getOriginalFilename());
            coverPath = "covers/" + uuid + "." + coverExt;
            File coverFile = new File(uploadRoot, coverPath);
            if (!coverFile.getParentFile().exists()) {
                coverFile.getParentFile().mkdirs();
            }
            cover.transferTo(coverFile);
        }
        // 第 5 步：组装 book 对象写数据库
        Book book = new Book();
        book.setName(name);
        book.setAuthor(author);
        book.setCover(coverPath);
        book.setFilePath(filePath);
        book.setFileFormat(format.toUpperCase());
        book.setFileSize(file.getSize());
        // 第 6 步：如果是 TXT，读文件数一遍汉字数，存进 word_count（详情页的"字数"）
        if ("TXT".equalsIgnoreCase(format)) {
            String content = Files.readString(targetFile.toPath(), StandardCharsets.UTF_8);
            int wordCount = 0;
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c >= '一' && c <= '龥') {   // 汉字 Unicode 区间
                    wordCount++;
                }
            }
            book.setWordCount(wordCount);
        }
        if (publishTime != null && !publishTime.isEmpty()) {
            book.setPublishTime(LocalDate.parse(publishTime));
        }
        book.setCategory(category);
        book.setStatus(status);
        book.setOwnerId(userId);
        this.save(book);
        return Result.success("上传成功");
    }

    //取文件扩展名的小工具（从 Controller 搬过来）
    private String getExt(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    //在线阅读（只支持纯文本 TXT）
    @Override
    public Result<String> read(Long id) throws IOException {
        // 第 1 步：查书存在吗
        Book book = this.getById(id);
        if (book == null) {
            return Result.error("图书不存在");
        }
        // 第 2 步：有文件吗（把老书挡在门口）
        if (book.getFilePath() == null || book.getFilePath().isEmpty()) {
            return Result.error("这本书没有文件");
        }
        // 第 3 步：只支持 TXT 在线阅读
        if (!"TXT".equals(book.getFileFormat())) {
            return Result.error("只有 TXT 格式支持在线阅读");
        }
        // 第 4 步：文件在磁盘上存在吗
        File file = new File(uploadDir, book.getFilePath());
        if (!file.exists()) {
            return Result.error("文件不存在");
        }
        // 第 5 步：把文件内容按 UTF-8 解码成字符串，原样返回给前端
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        return Result.success(content);
    }
}
