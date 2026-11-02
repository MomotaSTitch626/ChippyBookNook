package com.chippy.booknook.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService extends IService<User> {
    // 注册：接口只声明"有这个能力"，具体业务逻辑在 UserServiceImpl 里实现
    Result<String> register(User user);
    // 登录：业务逻辑（查用户、比密码、签发 token）在 UserServiceImpl 里实现
    Result<String> login(User user);
    // 字段白名单更新
    Result<User> updateMe(User user, Long userId);
    // 删除用户（仅管理员）：管理员校验、防自删、查目标存在性都在 UserServiceImpl 里
    Result<String> delete(Long adminId, Long id);
    // 获取当前登录用户信息：查用户、抹密码在 UserServiceImpl 里
    Result<User> me(Long userId);
    // 头像上传：类型校验、UUID 命名、保存文件都在 UserServiceImpl 里
    Result<String> uploadAvatar(MultipartFile file) throws IOException;
}
