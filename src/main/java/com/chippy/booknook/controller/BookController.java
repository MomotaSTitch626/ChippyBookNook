package com.chippy.booknook.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.Book;
import com.chippy.booknook.service.BookService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;

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
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;

@RestController
@RequestMapping("/api/book")
public class BookController {
    @Autowired
    private BookService bookService;

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

    //新增书：Controller 解析身份、调 Service；设置 ownerId、保存都在 BookServiceImpl.add 里
    @PostMapping
    public Result<Book> add(@RequestBody Book book, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        return bookService.add(book, userId);
    }

    //删除自己的书(仅限书主)：Controller 解析身份、调 Service；查存在、书主校验、删除在 BookServiceImpl.delete 里
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id,HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        return bookService.delete(userId, id);
    }

    //管理员可以删任意的书：Controller 解析身份、调 Service；管理员校验、查存在、删除在 BookServiceImpl.adminDelete 里
    @DeleteMapping("/admin/{id}")
    public Result<String> adminDelete(@PathVariable Long id,HttpServletRequest request){
        Long adminId = (Long) request.getAttribute("userId");
        return bookService.adminDelete(adminId, id);
    }


    //我的书屋：我的书 + 我借来未还的书——四步并集逻辑在 BookServiceImpl.my 里
    @GetMapping("/my")
    public Result<List<Book>> my(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return bookService.my(userId);
    }


    //登录用户修改图书状态：Controller 解析身份、调 Service；归属校验、白名单、更新在 BookServiceImpl.updateStatus 里
    @PutMapping("/update-status")
    public Result<String> updateStatus(@RequestBody Book book, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");
        return bookService.updateStatus(book, userId);
    }

    //按标签查书（分页）：标签关联查询 + 分页在 BookServiceImpl.byTag 里
    @GetMapping("/by-tag")
    public Result<IPage<Book>> byTag(@RequestParam String tagName,
                                     @RequestParam(defaultValue = "1") long page,
                                     @RequestParam(defaultValue = "5") long pageSize){
        return bookService.byTag(tagName, page, pageSize);
    }

    //查询书籍（模糊查询，书名和作者都可查询到）：查询条件构建 + 分页在 BookServiceImpl.search 里
    @GetMapping("/search")
    public Result<IPage<Book>> search(@RequestParam String keyword,
                                      @RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "5") long pageSize){
        return bookService.search(keyword, page, pageSize);
    }

    //上传图书：Controller 只收参数、调 Service；文件保存、TXT 字数统计、写库都在 BookServiceImpl.upload 里
    //`consumes = MediaType.MULTIPART_FORM_DATA_VALUE` 是说"本接口只接收 multipart/form-data 格式的请求
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
        Long userId = (Long) request.getAttribute("userId");
        return bookService.upload(file, cover, name, author, format, publishTime, category, status, userId);
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

    //在线阅读（只支持纯文本 TXT）：校验和读文件逻辑在 BookServiceImpl.read 里
    @GetMapping("/read/{id}")
    public Result<String> read(@PathVariable("id") Long id) throws IOException {
        return bookService.read(id);
    }

}