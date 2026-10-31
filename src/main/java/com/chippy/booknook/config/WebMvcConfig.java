package com.chippy.booknook.config;

import com.chippy.booknook.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/api/user/register",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**"
                );

    }

    //跨域配置：允许 Vue 前端(5173端口)访问后端接口

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")                    //哪些路径放行
                .allowedOrigins("http://localhost:5173")  //允许哪个前端来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  //允许哪些请求方法
                .allowedHeaders("*")                      //允许哪些请求头(含 Authorization)
                .allowCredentials(true);                  //允许携带凭证(token)
    }
}
