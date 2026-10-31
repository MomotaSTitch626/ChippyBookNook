package com.chippy.booknook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("note")
public class Note {
    @TableId(type = IdType.AUTO)

    private Long id;

    private Long bookId;

    private Long userId;

    private String content;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
