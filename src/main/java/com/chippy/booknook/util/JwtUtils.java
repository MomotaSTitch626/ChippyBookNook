package com.chippy.booknook.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

// JWT 工具类：负责"签发通行证"和"验证通行证"
// 加了 @Component，Spring 启动时会自动创建本类的实例，并把配置里的密钥注入进来
@Component
public class JwtUtils {

    // 签名密钥：给 token 盖章的印章，只有服务器知道，不能泄露
    // 密钥不再写死在代码里，改为从 application.yml 的 jwt.secret 配置读取
    @Value("${jwt.secret}")
    private String secret;

    // 静态字段：下面两个静态方法只能访问静态字段，init() 会把配置值搬到这里
    private static String SECRET;

    // Spring 创建实例后自动调用这个方法，把配置里的密钥赋给静态字段
    @PostConstruct
    public void init() {
        SECRET = this.secret;
    }

    // 有效期：7 天（7 × 24 × 60 × 60 × 1000 毫秒）
    private static final long EXPIRE = 7 * 24 * 60 * 60 * 1000L;

    // === 第 1 个方法：签发通行证（登录成功后调用） ===
    public static String generateToken(Long userId, String username) {
        return JWT.create()
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE))
                .sign(Algorithm.HMAC256(SECRET));
    }

    // === 第 2 个方法：验证通行证（拦截器里调用） ===
    public static DecodedJWT parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET)).build().verify(token);
    }

}
