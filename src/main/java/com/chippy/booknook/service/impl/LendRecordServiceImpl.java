package com.chippy.booknook.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.Book;
import com.chippy.booknook.entity.LendRecord;
import com.chippy.booknook.mapper.BookMapper;
import com.chippy.booknook.mapper.LendRecordMapper;
import com.chippy.booknook.service.LendRecordService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LendRecordServiceImpl extends ServiceImpl<LendRecordMapper, LendRecord> implements LendRecordService {

    // 借书业务要先查书（存在性、书主是谁）。这是纯单表查询，直接依赖 BookMapper 就够了，
    // 不再注入 BookService —— 否则 BookServiceImpl 注 LendRecordService、这里又注 BookService 会形成循环依赖
    @Autowired
    private BookMapper bookMapper;

    //借书
    @Override
    public Result<LendRecord> borrow(Long bookId, Long borrowerId) {
        // 第 1 步：确认书是否存在（BaseMapper.selectById 按主键查）
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            return Result.error("图书不存在");
        }
        // 第 2 步：不能借自己的书（书主和借阅人是同一个人直接拒绝）
        if (book.getOwnerId().equals(borrowerId)) {
            return Result.error("不能借自己的书");
        }
        // 第 3 步：这本书有没有被借走（status=0 表示未还）
        LendRecord exist = this.lambdaQuery()
                .eq(LendRecord::getBookId, bookId)
                .eq(LendRecord::getStatus, 0)
                .one();
        if (exist != null) {
            return Result.error("该书已经被借走");
        }
        // 第 4 步：组装借阅记录并保存
        LendRecord record = new LendRecord();
        record.setBookId(bookId);
        record.setLenderId(book.getOwnerId());
        record.setBorrowerId(borrowerId);
        record.setLendTime(LocalDateTime.now());
        record.setStatus(0);
        this.save(record);
        return Result.success(record);
    }

    //还书
    @Override
    public Result<String> returnRecord(Long recordId, Long borrowerId) {
        // 第 1 步：查阅借书记录是否存在
        LendRecord record = this.getById(recordId);
        if (record == null) {
            return Result.error("借阅记录不存在");
        }
        // 第 2 步：校验只能借阅人本人归还
        if (!record.getBorrowerId().equals(borrowerId)) {
            return Result.error("必须由借阅人本人归还");
        }
        // 第 3 步：校验已经还过不能再还
        if (record.getStatus() == 1) {
            return Result.error("该书已归还");
        }
        // 第 4 步：更新归还时间和状态（status 1 = 已归还）
        record.setReturnTime(LocalDateTime.now());
        record.setStatus(1);
        this.updateById(record);
        return Result.success("归还成功");
    }

    //新增借阅记录
    @Override
    public Result<LendRecord> add(LendRecord lendRecord) {
        // 第 1 步：保存
        this.save(lendRecord);
        return Result.success(lendRecord);
    }

    //删除借阅记录
    @Override
    public Result<String> delete(Long id) {
        // 第 1 步：删掉
        this.removeById(id);
        return Result.success("删除成功");
    }

    //我的借阅列表
    @Override
    public Result<List<LendRecord>> myList(Long borrowerId) {
        // 第 1 步：查该用户所有借阅记录，按借出时间倒序
        List<LendRecord> records = this.lambdaQuery()
                .eq(LendRecord::getBorrowerId, borrowerId)
                .orderByDesc(LendRecord::getLendTime)
                .list();
        return Result.success(records);
    }

    //查询某本书当前是否在借出中（详情页显示借阅状态用）
    @Override
    public Result<Boolean> check(Long bookId) {
        // 第 1 步：找这本书的"借出中"记录：status=0 表示还没还
        LendRecord exist = this.lambdaQuery()
                .eq(LendRecord::getBookId, bookId)
                .eq(LendRecord::getStatus, 0)
                .one();
        // exist 不是 null 说明有人正借着，返回 true；否则 false
        return Result.success(exist != null);
    }
}
