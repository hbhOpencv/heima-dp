package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.Result;
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

import java.util.List;

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
        Long id = UserHolder.getUser().getId();
        String key = BLOG_LIKED_KEY + blog.getId();
        Boolean member = stringRedisTemplate.opsForSet().isMember(key, id.toString());
        blog.setIsLike(BooleanUtil.isTrue(member));
    }

    @Override
    public Result likeBlog(Long id) {
        String key = BLOG_LIKED_KEY + id;
        Long userid = UserHolder.getUser().getId();
        Boolean success = stringRedisTemplate.opsForSet().isMember(key, userid.toString());
        // 如果用户没有点赞过
        if(!BooleanUtil.isTrue(success)){
            // 点赞数量加1
            boolean flag = update().setSql("liked = liked + 1").eq("id", id).update();
            // 加入点赞集合
            if(flag) {
                stringRedisTemplate.opsForSet().add(key, userid.toString());
                return Result.ok("点赞成功");
            }
            return Result.fail("点赞失败");
        }
        boolean flag = update().setSql("liked = liked - 1").eq("id", id).update();
        if(flag) {
            stringRedisTemplate.opsForSet().remove(key, userid.toString());
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

    private void extracted(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }
}
