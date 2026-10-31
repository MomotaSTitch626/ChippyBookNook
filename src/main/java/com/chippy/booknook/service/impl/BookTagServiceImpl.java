package com.chippy.booknook.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chippy.booknook.service.BookTagService;
import com.chippy.booknook.entity.BookTag;
import com.chippy.booknook.mapper.BookTagMapper;

import org.springframework.stereotype.Service;

@Service
public class BookTagServiceImpl extends ServiceImpl<BookTagMapper, BookTag> implements BookTagService {
}
