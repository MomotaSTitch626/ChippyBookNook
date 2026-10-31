package com.chippy.booknook.controller;

import com.chippy.booknook.common.Result;
import com.chippy.booknook.entity.User;
import com.chippy.booknook.service.UserService;

import java.io.IOException;
import java.util.List;
import java.io.File;
import java.util.UUID;

import com.chippy.booknook.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;
    // 上传目录：从配置读取（application.yml 的 file.upload-dir），不再硬编码个人电脑路径
    @Value("${file.upload-dir}")
    private String uploadDir;

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

    //删除用户(仅管理员)
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        //先确定是否为管理员
        Long adminId = (Long) request.getAttribute("userId");
        User admin = userService.getById(adminId);
        if(admin.getRole() == null || admin.getRole() != 1){
            return Result.error("没有管理员权限");
        }
        //防止误触自删
        if(adminId.equals(id)){
            return Result.error("不能删除自己");
        }
        //校验完后开始删用户
        User target = userService.getById(id);
        if(target == null ){
            return Result.error("用户不存在");
        }
        userService.removeById(id);
        return Result.success("删除成功");

    }

    // === 登录认证 ===
    @PostMapping("/login")
    public Result<String> login(@RequestBody User user){
        //按用户名查找
        User dbUser = userService.lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .one();

        //如果用户名或密码错误
        if(dbUser == null || !dbUser.getPassword().equals(user.getPassword())){
            return Result.error("用户名或密码错误");
        }

        //用户名密码都正确，签发通行证通过认证
        String token = JwtUtils.generateToken(dbUser.getId(), dbUser.getUsername());
        return Result.success(token);

    }

    // === 用户注册 ===
    // 注册和登录一样，在拦截器的排除列表里（见 WebMvcConfig），因为注册时还没有 token 可验
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user){
        //第 1 步：基本校验，用户名和密码都不能为空
        if(user.getUsername() == null || user.getUsername().isEmpty()
                || user.getPassword() == null || user.getPassword().isEmpty()){
            return Result.error("用户名和密码不能为空");
        }
        //第 2 步：重名校验，按用户名查一遍数据库，已经存在就拒绝
        User dbUser = userService.lambdaQuery()
                .eq(User::getUsername, user.getUsername())
                .one();
        if(dbUser != null){
            return Result.error("用户名已存在");
        }
        //第 3 步：保存新用户，注册成功
        userService.save(user);
        return Result.success("注册成功");
    }

    //获取当前登录用户信息（token自动识别身份，不用传id）
    @GetMapping("/me")
    public Result<User> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");   //拦截器放行时已把 userId 塞进请求属性
        User user = userService.getById(userId);               //按 id 查数据库
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);                                //密码是明文，返回前抹掉
        return Result.success(user);
    }

    //修改当前登录用户资料（白名单：只允许改这5个字段）
    @PutMapping("/me")
    public Result<User> updateMe(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");   //还是老套路：token 认出你是谁
        User dbUser = userService.getById(userId);             //查出数据库里的原记录
        if (dbUser == null) {
            return Result.error("用户不存在");
        }
        //白名单：前端传了什么才改什么，没传的保持原样
        if (user.getNickName() != null) dbUser.setNickName(user.getNickName());
        if (user.getAvatar() != null)   dbUser.setAvatar(user.getAvatar());
        if (user.getPhone() != null)    dbUser.setPhone(user.getPhone());
        if (user.getSex() != null)      dbUser.setSex(user.getSex());
        if (user.getAddress() != null)  dbUser.setAddress(user.getAddress());
        userService.updateById(dbUser);                        //把改好的对象存回去
        dbUser.setPassword(null);                              //返回前抹密码
        return Result.success(dbUser);
    }

    //头像上传：存到 uploads/avatars/ 下，返回相对路径（前端展示时拼 /uploads/ 前缀）
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        //1.校验图片类型：只允许常见图片格式
        String ext = getExt(file.getOriginalFilename());
        if (!ext.matches("png|jpg|jpeg|gif|webp")) {
            return Result.error("头像只支持 png/jpg/jpeg/gif/webp");
        }
        //2.生成唯一文件名，防止重名覆盖
        String uuid = UUID.randomUUID().toString();
        String avatarPath = "avatars/" + uuid + "." + ext;
        //3.保存到 uploads 目录（目录不存在就创建）
        File targetFile = new File(uploadDir, avatarPath);
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        file.transferTo(targetFile);
        //4.返回相对路径，前端显示时拼 /uploads/ 前缀
        return Result.success(avatarPath);
    }

    //取文件扩展名的小工具（和 BookController 里那个一模一样，从 BookController 复制过来）
    private String getExt(String filename){
        if(filename == null || !filename.contains(".")){
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }


}
