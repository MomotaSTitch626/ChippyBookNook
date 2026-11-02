package com.chippy.booknook.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.LendRecord;

import java.util.List;

public interface LendRecordService extends IService<LendRecord> {
    // 借书：查书存在、防自借、防重复借、组装保存都在 LendRecordServiceImpl 里
    Result<LendRecord> borrow(Long bookId, Long borrowerId);
    // 还书：查记录、本人归还校验、状态流转都在 LendRecordServiceImpl 里
    Result<String> returnRecord(Long recordId, Long borrowerId);
    // 新增借阅记录：保存逻辑在 LendRecordServiceImpl 里
    Result<LendRecord> add(LendRecord lendRecord);
    // 删除借阅记录：删除逻辑在 LendRecordServiceImpl 里
    Result<String> delete(Long id);
    // 我的借阅列表：条件查询 + 排序在 LendRecordServiceImpl 里
    Result<List<LendRecord>> myList(Long borrowerId);
    // 查某本书是否借出中：未还记录判定在 LendRecordServiceImpl 里
    Result<Boolean> check(Long bookId);
}
