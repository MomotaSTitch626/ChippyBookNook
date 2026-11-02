package com.chippy.booknook.controller;

import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.LendRecord;
import com.chippy.booknook.service.LendRecordService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lend-record")
public class LendRecordController {
    @Autowired
    private LendRecordService lendRecordService;

    @GetMapping("/{id}")
    public Result<LendRecord> getById(@PathVariable Long id){
        return Result.success(lendRecordService.getById(id));
    }

    @GetMapping("/list")
    public Result<List<LendRecord>> list(){
        return Result.success(lendRecordService.list());
    }

    //新增借阅记录：Controller 收参、调 Service；保存逻辑在 LendRecordServiceImpl.add 里
    @PostMapping
    public Result<LendRecord> add(@RequestBody LendRecord lendRecord){
        return lendRecordService.add(lendRecord);
    }

    //删除借阅记录：Controller 收参、调 Service；删除逻辑在 LendRecordServiceImpl.delete 里
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id){
        return lendRecordService.delete(id);
    }

    //借书：Controller 解析身份、调 Service；查书、防自借、防重复借、组装保存都在 LendRecordServiceImpl.borrow 里
    @PostMapping("/borrow")
    public Result<LendRecord> borrow(@RequestParam  Long bookId, HttpServletRequest request){
        Long borrowerId = (Long) request.getAttribute("userId");
        return lendRecordService.borrow(bookId, borrowerId);
    }

    //还书：Controller 解析身份、调 Service；查记录、本人归还校验、状态流转都在 LendRecordServiceImpl.returnRecord 里
    @PostMapping("/return")
    public Result<String>  returnRecord(@RequestParam  Long recordId, HttpServletRequest request){
        Long borrowerId = (Long) request.getAttribute("userId");
        return lendRecordService.returnRecord(recordId, borrowerId);
    }

    //我的借阅列表：Controller 解析身份、调 Service；条件查询 + 排序在 LendRecordServiceImpl.myList 里
    @GetMapping("/my")
    public Result<List<LendRecord>> myList(HttpServletRequest request){
        Long borrowerId = (Long) request.getAttribute("userId");
        return lendRecordService.myList(borrowerId);
    }
    //查询某本书当前是否在借出中：未还记录判定在 LendRecordServiceImpl.check 里
    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam Long bookId){
        return lendRecordService.check(bookId);
    }

}
