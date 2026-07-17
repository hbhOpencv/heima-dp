package com.hmdp.interceptor;

import com.hmdp.dto.UserDTO;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;

@Component
public class RefreshTokenInterceptor implements HandlerInterceptor {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("authorization");
        if (token == null) {
            return true;
        }

        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(LOGIN_USER_KEY + token);
        if (entries.isEmpty()) {
            return true;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setIcon((String) entries.get("icon"));
        userDTO.setNickName((String) entries.get("nickName"));
        userDTO.setId(Long.parseLong((String) entries.get("id")));

        UserHolder.saveUser(userDTO);
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        UserHolder.removeUser();
    }
}
