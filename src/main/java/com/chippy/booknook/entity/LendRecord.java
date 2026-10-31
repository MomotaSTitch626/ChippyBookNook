package com.chippy.booknook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("lend_record")

public class LendRecord {
    @TableId(type = IdType.AUTO)

    private Long id;

    private Long bookId;

    private Long lenderId;

    private Long borrowerId;

    private LocalDateTime lendTime;

    private LocalDateTime returnTime;

    private Integer status;

    private String remark;

}
