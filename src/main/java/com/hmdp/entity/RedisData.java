package com.hmdp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
//缓存数据类,泛型T表示缓存的数据类型
public class RedisData<T> {
    private LocalDateTime expireTime;
    private T data;
}
