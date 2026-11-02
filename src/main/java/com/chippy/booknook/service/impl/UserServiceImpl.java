package com.chippy.booknook.service.impl;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.User;
import com.chippy.booknook.mapper.UserMapper;
import com.chippy.booknook.service.UserService;
import com.chippy.booknook.util.JwtUtils;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 上传目录：从配置读取（application.yml 的 file.upload-dir），配置里写的是相对路径
    @Value("${file.upload-dir}")
    private String uploadDir;

    // 相对路径解析后的绝对路径：IDEA 里跑基于项目根，打 jar 跑基于 jar 所在目录
    private File uploadRoot;

    // 启动时把相对路径转成绝对路径——transferTo 遇到相对路径会被 Tomcat 拼到它的临时目录导致找不到路径
    @PostConstruct
    public void initUploadRoot() {
        this.uploadRoot = new File(uploadDir).getAbsoluteFile();
    }

    //注册
    @Override
    public Result<String> register(User user) {
        // 第 1 步：基本校验，用户名和密码都不能为空
        if (user.getUsername() == null || user.getUsername().isEmpty()
                || user.getPassword() == null || user.getPassword().isEmpty()) {
            return Result.error("用户名和密码不能为空");
        }
        // 第 2 步：重名校验，按用户名查一遍数据库，已经存在就拒绝
        User dbUser = this.lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .one();
        if (dbUser != null) {
            return Result.error("用户名已存在");
        }
        // 第 3 步：保存新用户，注册成功
        this.save(user);
        return Result.success("注册成功");
    }

    //登录
    @Override
    public Result<String> login(User user) {
        // 第 1 步：按用户名查数据库里的用户
        User dbUser = this.lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .one();
        // 第 2 步：用户名不存在或密码不匹配，统一返回同一个错误（不暴露是用户名错还是密码错）
        if (dbUser == null || !dbUser.getPassword().equals(user.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        // 第 3 步：认证通过，签发 token（包含 userId 和 username），这就是登录的"通行证"
        String token = JwtUtils.generateToken(dbUser.getId(), dbUser.getUsername());
        return Result.success(token);
    }

    //字段白名单更新
    @Override
    public Result<User> updateMe(User user, Long userId) {

        User dbUser = this.getById(userId);             //查出数据库里的原记录
        if (dbUser == null) {
            return Result.error("用户不存在");
        }
        //白名单：前端传了什么才改什么，没传的保持原样
        if (user.getNickName() != null) dbUser.setNickName(user.getNickName());
        if (user.getAvatar() != null)   dbUser.setAvatar(user.getAvatar());
        if (user.getPhone() != null)    dbUser.setPhone(user.getPhone());
        if (user.getSex() != null)      dbUser.setSex(user.getSex());
        if (user.getAddress() != null)  dbUser.setAddress(user.getAddress());
        this.updateById(dbUser);                        //把改好的对象存回去
        dbUser.setPassword(null);                              //返回前抹密码
        return  Result.success(dbUser);
    }

    //删除用户（仅管理员）
    @Override
    public Result<String> delete(Long adminId, Long id) {
        // 第 1 步：先确定操作者是不是管理员
        User admin = this.getById(adminId);
        if (admin.getRole() == null || admin.getRole() != 1) {
            return Result.error("没有管理员权限");
        }
        // 第 2 步：防止误触自删
        if (adminId.equals(id)) {
            return Result.error("不能删除自己");
        }
        // 第 3 步：校验目标用户存在
        User target = this.getById(id);
        if (target == null) {
            return Result.error("用户不存在");
        }
        // 第 4 步：删掉
        this.removeById(id);
        return Result.success("删除成功");
    }

    //获取当前登录用户信息
    @Override
    public Result<User> me(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);   //密码是明文，返回前抹掉
        return Result.success(user);
    }

    //头像上传：类型校验、UUID 命名、保存到 uploads 目录
    @Override
    public Result<String> uploadAvatar(MultipartFile file) throws IOException {
        // 第 1 步：校验图片类型，只允许常见图片格式
        String ext = getExt(file.getOriginalFilename());
        if (!ext.matches("png|jpg|jpeg|gif|webp")) {
            return Result.error("头像只支持 png/jpg/jpeg/gif/webp");
        }
        // 第 2 步：生成唯一文件名，防止重名覆盖
        String uuid = UUID.randomUUID().toString();
        String avatarPath = "avatars/" + uuid + "." + ext;
        // 第 3 步：保存到 uploads 目录（目录不存在就创建），用绝对路径避免被 Tomcat 拼到临时目录
        File targetFile = new File(uploadRoot, avatarPath);
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        file.transferTo(targetFile);
        // 第 4 步：返回相对路径，前端展示时拼 /uploads/ 前缀
        return Result.success(avatarPath);
    }

    //取文件扩展名的小工具（从 Controller 搬过来）
    private String getExt(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
