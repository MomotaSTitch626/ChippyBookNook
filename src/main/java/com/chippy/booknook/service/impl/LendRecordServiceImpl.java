package com.chippy.booknook.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chippy.booknook.entity.LendRecord;
import com.chippy.booknook.mapper.LendRecordMapper;
import com.chippy.booknook.service.LendRecordService;

import org.springframework.stereotype.Service;

@Service
public class LendRecordServiceImpl extends ServiceImpl<LendRecordMapper,LendRecord> implements LendRecordService {
}
