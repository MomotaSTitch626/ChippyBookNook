package com.chippy.booknook.controller;

import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.Book;
import com.chippy.booknook.entity.LendRecord;
import com.chippy.booknook.service.BookService;
import com.chippy.booknook.service.LendRecordService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/lend-record")
public class LendRecordController {
    @Autowired
    private LendRecordService lendRecordService;
    @Autowired
    private BookService bookService;

    @GetMapping("/{id}")
    public Result<LendRecord> getById(@PathVariable Long id){
        return Result.success(lendRecordService.getById(id));
    }

    @GetMapping("/list")
    public Result<List<LendRecord>> list(){
        return Result.success(lendRecordService.list());
    }

    @PostMapping
    public Result<LendRecord> add(@RequestBody LendRecord lendRecord){
        lendRecordService.save(lendRecord);
        return Result.success(lendRecord);
    }

    //删除借阅记录
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id){
        lendRecordService.removeById(id);
        return Result.success("删除成功");
    }

    //借书
    @PostMapping("/borrow")
    public Result<LendRecord> borrow(@RequestParam  Long bookId, HttpServletRequest request){
        //声明借阅人 = 当前登录的用户
        Long borrowerId = (Long) request.getAttribute("userId");

        //确认书是否存在
        Book book = bookService.getById(bookId);
        if (book == null) {
            return Result.error("图书不存在");
        }
        //不能借自己的书：出借人（书主）和借阅人是同一个人时直接拒绝
        if (book.getOwnerId().equals(borrowerId)) {
            return Result.error("不能借自己的书");
        }
        //这本书有没有被借走了
        LendRecord exist = lendRecordService.lambdaQuery()
                .eq(LendRecord::getBookId,bookId)
                .eq(LendRecord::getStatus,0)
                .one();
        if (exist != null) {
            return Result.error("该书已经被借走");
        }

        //组装借阅记录并保存
        LendRecord record = new LendRecord();
        record.setBookId(bookId);
        record.setLenderId(book.getOwnerId());
        record.setBorrowerId(borrowerId);
        record.setLendTime(LocalDateTime.now());
        record.setStatus(0);
        lendRecordService.save(record);
        return Result.success(record);
    }

    //还书
    @PostMapping("/return")
    public Result<String>  returnRecord(@RequestParam  Long recordId, HttpServletRequest request){
        //声明还书人 = 当前登录的用户
        Long borrowerId = (Long) request.getAttribute("userId");

        //查阅借书记录是否存在
        LendRecord record = lendRecordService.getById(recordId);
        if (record == null) {
            return Result.error("借阅记录不存在");
        }

        //校验：只能借阅人本人还；已经还过不能再还
        if (!record.getBorrowerId().equals(borrowerId)) {
            return Result.error("必须由借阅人本人归还");
        }
        if (record.getStatus() == 1){
            return Result.error("该书已归还");
        }

        //更新归还时间和状态
        record.setReturnTime(LocalDateTime.now());
        record.setStatus(1);
        /*
        getById(id)      → 只查 → 传 id 就够
        removeById(id)   → 只删 → 传 id 就够
        updateById(record) → 要改 → 传整个对象
        */
        lendRecordService.updateById(record);
        return Result.success("归还成功");
    }

    //我的借阅列表
    @GetMapping("/my")
    public Result<List<LendRecord>> myList(HttpServletRequest request){
        //当前登录用户
        Long borrowerId = (Long) request.getAttribute("userId");

        //查询该用户所有的借阅记录
        List<LendRecord> records = lendRecordService.lambdaQuery()
                .eq(LendRecord::getBorrowerId,borrowerId)
                .orderByDesc(LendRecord::getLendTime)
                .list();
        return Result.success(records);
    }
    //查询某本书当前是否在借出中（详情页显示借阅状态用）
    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam Long bookId){
        //找这本书的"借出中"记录：status=0 表示还没还
        LendRecord exist = lendRecordService.lambdaQuery()
                .eq(LendRecord::getBookId, bookId)
                .eq(LendRecord::getStatus, 0)
                .one();
        //exist 不是 null 说明有人正借着，返回 true；否则 false
        return Result.success(exist != null);
    }

}
