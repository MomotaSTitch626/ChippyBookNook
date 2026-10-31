package com.chippy.booknook.interceptor;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.chippy.booknook.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //跨域预检请求(OPTIONS)直接放行，不校验token
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头里拿通行证（前端约定放在 Authorization 这个头里）
        String token = request.getHeader("Authorization");

        // 没带证，直接拒绝
        if (token == null || token.isEmpty() || !token.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }

        // 验证证的真伪和有效期，假证/过期抛出异常
        try{
            // 剥掉 "Bearer " 前缀（前 7 个字符），剩下的才是真正的 JWT
            String jwtToken = token.substring(7);
            DecodedJWT jwt = JwtUtils.parseToken(jwtToken);
            //验证通过
            request.setAttribute("userId",jwt.getClaim("userId").asLong());
            request.setAttribute("username",jwt.getClaim("username").asString());
            return  true;

        } catch (Exception e){
            response.setStatus(401);
            return false;
        }
    }

}
