package com.hmdp.interceptor;

import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.utils.UserHolder;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// 登录拦截器,把他放到ioc容器中怎么做?
import org.springframework.stereotype.Component;
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取session
        HttpSession session = request.getSession();
        //获取session中的用户信息
        Object user = session.getAttribute("user");
        //判断用户是否存在
        if(user == null){
            //用户不存在，返回401状态码
            response.setStatus(401);
            return false;
        }
            //用户存在，将用户信息保存到UserHolder中,UserHolder是一个ThreadLocal，用于在不同线程之间传递数据
            UserDTO user1 = (UserDTO) user;
            UserHolder.saveUser(user1);
        //放行，继续执行下一个拦截器或处理器;
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        //从UserHolder中移除用户信息
        UserHolder.removeUser();
    }
}
