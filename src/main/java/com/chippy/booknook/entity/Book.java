package com.chippy.booknook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("book")
public class Book {
    @TableId(type = IdType.AUTO)

    private Long id;

    private Long ownerId;

    private String isbn;

    private String name;

    private String author;

    private String publisher;

    private BigDecimal price;

    private String cover;

    private String description;

    private Integer readStatus;

    private Integer progress;

    private Integer rating;

    private Integer totalPages;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String filePath;

    private String fileFormat;

    private Long fileSize;

    private LocalDate publishTime;

    private String category;

    private String status;

    private Integer wordCount;

    @TableField(exist = false)
    private String source;

    @TableField(exist = false)
    private Long recordId;

}
