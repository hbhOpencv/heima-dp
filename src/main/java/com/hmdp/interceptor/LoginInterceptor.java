package com.hmdp.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

// 登录拦截器,把他放到ioc容器中怎么做?
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取session
       // HttpSession session = request.getSession();
        String token = request.getHeader("authorization");
        if(token.isEmpty()){
            //token为空，返回401状态码
            response.setStatus(401);
            return false;
        }
        //获取session中的用户信息
       // Object user = session.getAttribute("user");
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(LOGIN_USER_KEY + token);
        //判断用户是否存在
        if(entries.isEmpty()){
            //用户不存在，返回401状态码
            response.setStatus(401);
            return false;
        }
        //将用户信息转换到UserDTO对象
        UserDTO userDTO = new UserDTO();
        userDTO.setIcon((String) entries.get("icon"));
        userDTO.setNickName((String) entries.get("nickName"));
        userDTO.setId(Long.parseLong((String) entries.get("id")));
        //UserDTO userDTO = BeanUtil.fillBeanWithMap(entries, new UserDTO(),false);这个是使用BeanUtil工具类将Map转换为UserDTO对象，false表示不忽略空值

        //将用户信息保存到UserHolder中
        UserHolder.saveUser(userDTO);
        //刷新token过期时间
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);
        return true;


//            //用户存在，将用户信息保存到UserHolder中,UserHolder是一个ThreadLocal，用于在不同线程之间传递数据
//            UserDTO user1 = (UserDTO) user;
//            UserHolder.saveUser(user1);
//        //放行，继续执行下一个拦截器或处理器;
        //return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        //从UserHolder中移除用户信息
        UserHolder.removeUser();
    }
}
