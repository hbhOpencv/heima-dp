package com.hmdp.config;

import com.hmdp.interceptor.LoginInterceptor;
import com.hmdp.interceptor.RefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.annotation.Resource;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {
    @Resource//使用注解注入LoginInterceptor组件
    private RefreshTokenInterceptor refreshTokenInterceptor;
    @Resource
    private LoginInterceptor loginInterceptor;
    //将LoginInterceptor组件添加到拦截器注册中,并指定拦截路径和排除路径
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //将LoginInterceptor组件添加到拦截器注册中,并指定拦截路径和排除路径
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(
                        "/user/code"
                        ,"/user/login"
                        ,"/blog/hot"
                        ,"/upload/**"
                        ,"/shop-type/**"
                        ,"/shop/**"
                        ,"/voucher/**"
                ).order(1);
        //将RefreshTokenInterceptor组件添加到拦截器注册中,并指定拦截路径和排除路径
        registry.addInterceptor(refreshTokenInterceptor).order(0);

    }



}
