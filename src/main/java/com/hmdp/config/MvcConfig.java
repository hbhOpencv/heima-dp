package com.hmdp.config;

import com.hmdp.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {
    @Resource//使用注解注入LoginInterceptor组件
    private LoginInterceptor loginInterceptor;
    //将LoginInterceptor组件添加到拦截器注册中,并指定拦截路径和排除路径
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(
                        "/user/code"
                        ,"/user/login"
                        ,"/blog/hot"
                        ,"/upload/**"
                        ,"/shop-type/**"
                        ,"/shop/**"
                        ,"/voucher/**"
                );

    }



}
