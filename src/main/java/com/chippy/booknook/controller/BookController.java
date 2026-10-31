package com.chippy.booknook.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.Book;
import com.chippy.booknook.entity.BookTag;
import com.chippy.booknook.entity.LendRecord;
import com.chippy.booknook.entity.User;
import com.chippy.booknook.service.UserService;
import com.chippy.booknook.service.BookService;
import com.chippy.booknook.service.LendRecordService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.chippy.booknook.service.BookTagService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/book")
public class BookController {
    @Autowired
    private BookService bookService;
    @Autowired
    private UserService userService;
    @Autowired
    private BookTagService bookTagService;

    @Autowired
    private LendRecordService lendRecordService;

    // 上传目录：从配置读取（application.yml 的 file.upload-dir），不再硬编码个人电脑路径
    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/{id}")
    public Result<Book> getById(@PathVariable Long id){
        return Result.success(bookService.getById(id));
    }

    @GetMapping("/list")
    public Result<IPage<Book>> list(@RequestParam(defaultValue = "1") long page,
                                    @RequestParam(defaultValue = "5") long pageSize,
                                    @RequestParam(required = false) String category,
                                    @RequestParam(required = false) String status){
        LambdaQueryWrapper <Book> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()){
            wrapper.eq(Book::getCategory, category);
        }
        if (status != null && !status.isEmpty()){
            wrapper.eq(Book::getStatus, status);
        }
        Page<Book> p = new Page<>(page, pageSize);
        return Result.success(bookService.page(p,wrapper));
    }

    @PostMapping
    public Result<Book> add(@RequestBody Book book, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        book.setOwnerId(userId);
        bookService.save(book);
        return Result.success(book);
    }

    //删除自己的书(仅限书主)
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id,HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        Book dbBook = bookService.getById(id);
        if(dbBook == null){
            return Result.error("图书不存在");
        }
        if(!userId.equals(dbBook.getOwnerId())){
            return Result.error("只能删自己的书");
        }
        bookService.removeById(id);
        return Result.success("删除成功");
    }

    //管理员可以删任意的书
    @DeleteMapping("/admin/{id}")
    public Result<String> adminDelete(@PathVariable Long id,HttpServletRequest request){
        Long adminId = (Long) request.getAttribute("userId");
        User admin = userService.getById(adminId);
        if(admin.getRole() == null || admin.getRole() != 1){
            return Result.error("没有管理员权限");
        }
        Book book = bookService.getById(id);
        if(book == null){
            return Result.error("图书不存在");
        }
        bookService.removeById(id);
        return Result.success("删除成功");
    }


    //我的书屋：我的书 + 我借来未还的书
    @GetMapping("/my")
    public Result<List<Book>> my(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        //第一步：查我自己的书，标记 source = own
        List<Book> myBooks = bookService.lambdaQuery()
                .eq(Book::getOwnerId, userId)
                .list();
        for (Book b : myBooks) {
            b.setSource("own");
        }
        //第二步：查我借来未还的记录（status=0），拿到书的 id 列表
        List<LendRecord> records = lendRecordService.lambdaQuery()
                .eq(LendRecord::getBorrowerId, userId)
                .eq(LendRecord::getStatus, 0)
                .list();
        //把 bookId → 借阅记录id 存成映射，还书时要用记录id
        Map<Long, Long> recordIdMap = new HashMap<>();
        for (LendRecord r : records) {
            recordIdMap.put(r.getBookId(), r.getId());
        }
        List<Long> bookIds = records.stream().map(LendRecord::getBookId).toList();
        //第三步：这些 id 对应的书，标记 source = lent
        List<Book> lentBooks = bookIds.isEmpty()
                ? new ArrayList<>()
                : bookService.lambdaQuery().in(Book::getId, bookIds).list();
        for (Book b : lentBooks) {
            b.setSource("lent");
            b.setRecordId(recordIdMap.get(b.getId()));   //借来的书随身带上借阅记录id
        }
        //第四步：合并返回
        myBooks.addAll(lentBooks);
        return Result.success(myBooks);
    }


    //登录用户修改图书状态
    @PutMapping("/update-status")
    public Result<?> updateStatus(@RequestBody Book book, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        Book dbBook = bookService.getById(book.getId());
        if(dbBook == null){
            return Result.error("图书不存在");
        }
        if(!dbBook.getOwnerId().equals(userId)){
            return Result.error("只能修改自己的书");
        }
        //前端传的整个对象不可信，不能整包丢给 `updateById`；先用 `getById` 把数据库里的真身捞出来，只把允许改的 3 个字段拷过去，再更新真身 —— 这就是 "字段白名单"。
        dbBook.setReadStatus(book.getReadStatus());
        dbBook.setProgress(book.getProgress());
        dbBook.setRating(book.getRating());
        bookService.updateById(dbBook);
        return Result.success("更新成功");
    }

    //按标签查书（分页）
    @GetMapping("/by-tag")
    public Result<IPage<Book>> byTag(@RequestParam String tagName,
                                     @RequestParam(defaultValue = "1") long page,
                                     @RequestParam(defaultValue = "5") long pageSize){
        //先获得数据库中所有的标签类型
        List<BookTag> bookTags = bookTagService.lambdaQuery()
                .eq(BookTag::getTagName, tagName)
                .list();

        //如果没有这个标签，返回一个空页（records 空、total 0）
        if(bookTags.isEmpty()){
            return Result.success(new Page<>(page, pageSize));
        }

        //有标签，把标签收集成一个列表
        List<Long> bookIds = new ArrayList<>();
        for(BookTag bt : bookTags){
            bookIds.add(bt.getBookId());
        }

        //按标签分页查书：把"标签筛出的书"+"第几页/每页几条"一起交给分页插件
        Page<Book> p = new Page<>(page, pageSize);
        bookService.page(p, new LambdaQueryWrapper<Book>().in(Book::getId, bookIds));
        return Result.success(p);
    }

    //查询书籍（模糊查询，书名和作者都可查询到）
    @GetMapping("/search")
    public Result<IPage<Book>> search(@RequestParam String keyword,
                                      @RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "5") long pageSize,
                                      HttpServletRequest request){

        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<Book>()
                .like(Book::getName, keyword)
                .or()
                .like(Book::getAuthor, keyword);

        Page<Book> p = new Page<>(page, pageSize);
        bookService.page(p, wrapper);
        return Result.success(p);
    }

    //上传图书
    //`consumes = MediaType.MULTIPART_FORM_DATA_VALUE` 是说"本接口只接收 multipart/form-data 格式的请求
    // "multipart-form-data 就是 "带文件的表单"，浏览器上传文件用的就是它。
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "cover", required = false) MultipartFile cover,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String author,
                                 @RequestParam String format,
                                 @RequestParam(required = false) String publishTime,
                                 @RequestParam(required = false) String category,
                                 @RequestParam(required = false) String status,
                                 HttpServletRequest request) throws IOException{
        //1.校验书文件类型：只允许 txt / pdf / epub
        String ext = getExt(file.getOriginalFilename());
        if (!ext.matches("txt|pdf|epub")){
            return Result.error("书文件只支持 txt/pdf/epub");
        }

        //2.生成唯一文件名，防止重名覆盖
        String uuid = UUID.randomUUID().toString();
        String filePath = "books/" + uuid + "." +ext;

        //3.保存书文件到 uploads 目录
        File targetFile = new File(uploadDir, filePath);
        if(!targetFile.getParentFile().exists()){
            targetFile.getParentFile().mkdirs();
        }
        file.transferTo(targetFile);
        //4.保存封面（可选）
        String coverPath = null;
        if(cover != null && !cover.isEmpty()){
            String coverExt = getExt(cover.getOriginalFilename());
            coverPath = "covers/" + uuid + "." +coverExt;
            File coverFile = new File(uploadDir,coverPath);
            if(!coverFile.getParentFile().exists()){
                coverFile.getParentFile().mkdirs();
            }
            cover.transferTo(coverFile);
        }
        //5.组装 book 对象写数据库
        Book book = new Book();
        book.setName(name);
        book.setAuthor(author);
        book.setCover(coverPath);
        book.setFilePath(filePath);
        book.setFileFormat(format.toUpperCase());
        book.setFileSize(file.getSize());
        // ★ 5.5 如果是 TXT，读文件数一遍汉字数，存进 word_count（详情页的"字数"）
        if ("TXT".equalsIgnoreCase(format)) {                        // ① 大小写不敏感比较："txt/TXT/Txt" 都算相等，不用先转大写
            String content = Files.readString(targetFile.toPath(), StandardCharsets.UTF_8);
            int wordCount = 0;
            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c >= '一' && c <= '龥') {                          // ② \u4e00 就是"一"，\u9fa5 就是"龥"——直接写字面量
                    wordCount++;
                }
            }
            book.setWordCount(wordCount);
        }

        if(publishTime != null && !publishTime.isEmpty()){
            book.setPublishTime(LocalDate.parse(publishTime));
        }
        book.setCategory(category);
        book.setStatus(status);
        book.setOwnerId((Long) request.getAttribute("userId"));
        bookService.save(book);
        return Result.success("上传成功");
    }

    //取文件扩展名
    private String getExt(String filename){
        if(filename == null || !filename.contains(".")){
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    //下载图书
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downLoad(@PathVariable("id") Long id) throws IOException {
        Book book = bookService.getById(id);
        if(book == null){
            return ResponseEntity.notFound().build();
        }
        //老书没有文件，直接拒绝
        if (book.getFilePath() == null || book.getFilePath().isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        File file = new File(uploadDir, book.getFilePath());
        if (!file.exists()){
            return ResponseEntity.notFound().build();
        }
        byte[] data = Files.readAllBytes(file.toPath());
        String filename = book.getName() + "." + book.getFileFormat().toLowerCase();
        //防止中文名乱码
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+ filename + "\"; filename*=utf-8''" + encoded)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    //在线阅读（只支持纯文本）
    @GetMapping("/read/{id}")
    public Result<String> read(@PathVariable("id") Long id) throws IOException {
        Book book = bookService.getById(id);
        if (book == null){
            return Result.error("图书不存在");
        }
        //有文件吗？（把老书挡在门口）
        if (book.getFilePath() == null || book.getFilePath().isEmpty()){
            return Result.error("这本书没有文件");
        }
        if (!"TXT".equals(book.getFileFormat())){
            return Result.error("只有 TXT 格式支持在线阅读");
        }
        File file = new File(uploadDir, book.getFilePath());
        if (!file.exists()){
            return Result.error("文件不存在");
        }
        //把文件内容按 UTF-8 解码成字符串，原样返回给前端
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        return Result.success(content);
    }

}