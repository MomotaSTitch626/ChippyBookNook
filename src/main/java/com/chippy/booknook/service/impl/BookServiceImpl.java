package com.chippy.booknook.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chippy.booknook.entity.Book;
import com.chippy.booknook.mapper.BookMapper;
import com.chippy.booknook.service.BookService;

import org.springframework.stereotype.Service;


@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {
}
