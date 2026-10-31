package com.chippy.booknook.exception;

import com.chippy.booknook.common.Result;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //`Exception.class` 是所有异常的老祖宗 → "不管什么错，我都接"
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e){
        //把错误完整打印到 IDEA 控制台（红色堆栈）——**程序员排查看的**，浏览器看不到
        e.printStackTrace();
        return Result.error("系统错误: "+e.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public Result<String> handleDuplicateKeyException(DuplicateKeyException e){
        return Result.error("数据已存在");
    }
}
