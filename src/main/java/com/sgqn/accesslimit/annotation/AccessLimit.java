package com.sgqn.clubonline.web.config.requestfrequency;

import org.checkerframework.checker.units.qual.C;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-02-27 21:21:01
 * @modify:
 */

public @interface AccessLimit {

    /**
     * 接口频率。格式必须为： {@code /^\\d+/\\d+[smhd]/} 即 次数/单位时间。例如 5/1m 表示 一分钟最多请求 5 次接口。<br>
     * 单位时间中：                                                                                       <br>
     *      - s : 秒                                                                                     <br>
     *      - m : 分                                                                                     <br>
     *      - h : 小时                                                                                    <br>
     *      - d : 天                                                                                     <br>
     *
     */
    String frequency() default "5/1m";


}
