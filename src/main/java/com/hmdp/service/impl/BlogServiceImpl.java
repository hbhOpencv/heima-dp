package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.service.IUserService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.BLOG_LIKED_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {
    @Autowired
    private IUserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryId(Long id) {
        Blog blog = getById(id);
        if(blog == null){
            return Result.fail("探店博文不存在");
        }
        extracted(blog);
        // 查询用户点赞状态
        isBlogLiked(blog);
        return Result.ok(blog);
    }

    private void isBlogLiked(Blog blog) {
        UserDTO user = UserHolder.getUser();
        if(user == null){
            return;
        }
        Long id = UserHolder.getUser().getId();
        String key = BLOG_LIKED_KEY + blog.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, String.valueOf(id));
        blog.setIsLike(score!=null);
    }

    @Override
    public Result likeBlog(Long id) {
        String key = BLOG_LIKED_KEY + id;
        Long userid = UserHolder.getUser().getId();
        String userId = String.valueOf(userid);
       // Boolean success = stringRedisTemplate.opsForSet().isMember(key, userId);
        Double score = stringRedisTemplate.opsForZSet().score(key, userId);
        // 如果用户没有点赞过
        if(score == null){
            // 点赞数量加1
            boolean flag = update().setSql("liked = liked + 1").eq("id", id).update();
            // 加入点赞集合
            if(flag) {
                //stringRedisTemplate.opsForSet().add(key, userid.toString());
                //使用有序集合存储点赞用户，点赞时间作为分数
                stringRedisTemplate.opsForZSet().add(key, userId,System.currentTimeMillis());
                return Result.ok("点赞成功");
            }
            return Result.fail("点赞失败");
        }
        boolean flag = update().setSql("liked = liked - 1").eq("id", id).update();
        if(flag) {
            stringRedisTemplate.opsForZSet().remove(key, userId);
            return Result.ok("取消点赞成功");
        }
        return Result.fail("取消点赞失败");
    }

    @Override
    public Result queryHotId(Integer current) {
        // 根据用户查询

        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog ->{
            extracted(blog);
            isBlogLiked(blog);
        });
        return Result.ok(records, page.getTotal());
    }

    @Override
    public Result queryLikes(Long id) {
        String key = BLOG_LIKED_KEY + id;
        // 查询点赞前5的用户
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        // 如果点赞集合为空，返回空列表
        if (top5 == null || top5.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        // 查询用户信息
        List<Long> userIds = new ArrayList<>();
        for (String s : top5) {
            Long l = Long.valueOf(s);
            userIds.add(l);
        }
        // 使用，分隔用户id
//        String idsStr = StrUtil.join(",", userIds);
        //
        List<UserDTO> userDTOS = new ArrayList<>();
        for (Long userId : userIds) {
            User user = userService.getById(userId);
            UserDTO userDTO = new UserDTO();
            userDTO.setIcon(user.getIcon());
            userDTO.setNickName(user.getNickName());
            userDTO.setId(user.getId());
            userDTOS.add(userDTO);
        }
        return Result.ok(userDTOS);




//        List<UserDTO> userDTOS = userService.query().in("id", userIds)
//                .last("order by field(id," + idsStr + ")")
//                .list().stream()
//                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
//                .collect(Collectors.toList());
//        return Result.ok(userDTOS);

    }

    private void extracted(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }
}
