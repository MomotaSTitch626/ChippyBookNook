package com.chippy.booknook.controller;

import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.User;
import com.chippy.booknook.service.UserService;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    //查询单个目标信息，通过查id
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    //查询全部的目标信息，全搜索
    @GetMapping("/list")
    public Result<List<User>> list(){
        return Result.success(userService.list());
    }

    @PostMapping
    public Result<User> add(@RequestBody User user){
        userService.save(user);
        return Result.success(user);
    }

    //删除用户(仅管理员)：Controller 只解析身份、调 Service；管理员校验、防自删、查存在性都在 UserServiceImpl.delete 里
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        return userService.delete(adminId, id);
    }

    // === 登录认证 ===
    // Controller 只做接客：收参数、调 Service、返回结果。查用户、比密码、签发 token 都在 UserServiceImpl.login 里
    @PostMapping("/login")
    public Result<String> login(@RequestBody User user){
        return userService.login(user);
    }

    // 注册和登录一样，在拦截器的排除列表里（见 WebMvcConfig），因为注册时还没有 token 可验
    // Controller 只做接客：收参数、调 Service、返回结果。校验和保存的逻辑都在 UserServiceImpl.register 里
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user){
        return userService.register(user);
    }


    //获取当前登录用户信息（token自动识别身份，不用传id）：Controller 解析身份、调 Service；查用户、抹密码在 UserServiceImpl.me 里
    @GetMapping("/me")
    public Result<User> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");   //拦截器放行时已把 userId 塞进请求属性
        return userService.me(userId);
    }

    //修改当前登录用户资料（白名单：只允许改这5个字段）
    @PutMapping("/me")
    public Result<User> updateMe(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");   //还是老套路：token 认出你是谁
        return userService.updateMe(user, userId);
    }

    //头像上传：Controller 只收文件、调 Service；类型校验、UUID 命名、保存都在 UserServiceImpl.uploadAvatar 里
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        return userService.uploadAvatar(file);
    }

}
