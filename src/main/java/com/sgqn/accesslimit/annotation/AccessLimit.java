package com.sgqn.accesslimit.annotation;

import com.sgqn.accesslimit.handler.AccessLimitHandler;
import com.sgqn.accesslimit.handler.impl.LimitIpHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-02-27 21:21:01
 * @modify:
 */

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AccessLimit {

    /**
     * 接口频率。格式必须为： {@code /^\\d+/\\d+[smhd]/} 即 次数/单位时间。例如 5/1 表示 一分钟最多请求 5 次接口。<br>
     */
    String frequency() default "5/120";

    /**
     * IP 对接口的访问达到频率限制后的处理器。
     */
    Class<? extends AccessLimitHandler> handler() default LimitIpHandler.class;
}
