package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmdp.dto.Result;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.UserHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {
    @Override
    public Result follow(Long id, Boolean flag) {
        Long userId = UserHolder.getUser().getId();
        if(BooleanUtil.isTrue(flag)){
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(id);
            boolean save = save(follow);
            if(save){
                return Result.ok("关注成功");
            }
            return Result.fail("关注失败");
            // 关注
        }else{
            // 取消关注，就是删除关注记录
            QueryWrapper<Follow> followQueryWrapper = new QueryWrapper<>();
            boolean remove = remove(followQueryWrapper.eq("user_id", userId).eq("follow_user_id", id));
            if(remove){
                return Result.ok("取消关注成功");
            }
            return Result.fail("取消关注失败");
        }

    }

    @Override
    public Result isFollow(Long id) {
        Long userId = UserHolder.getUser().getId();
        //根据用户id和关注用户id查询关注记录
        QueryWrapper<Follow> followQueryWrapper = new QueryWrapper<>();
        Integer count = query().eq("user_id", userId).eq("follow_user_id", id).count();
        return Result.ok(count > 0);//如果count大于0，说明关注了该用户,返回true
    }

    @Override
    public Result commonFollow(Long id) {
        return null;
    }
}
