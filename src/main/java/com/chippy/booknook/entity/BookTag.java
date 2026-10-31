package com.chippy.booknook.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

@Data
@TableName("book_tag")

public class BookTag {
    @TableId(type = IdType.AUTO)

    private Long id;

    private Long bookId;

    private String tagName;

}
