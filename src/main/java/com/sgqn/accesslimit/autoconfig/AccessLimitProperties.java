package com.sgqn.accesslimit.autoconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-02-28 22:21:49
 * @modify:
 */

@Data
@ConfigurationProperties(prefix = "access-limit")
public class AccessLimitProperties {

    /**
     * 是否开启
     */
    private Boolean enable;

    /**
     * 数据存入 redis 的基本路径
     */
    private String redisBasePath;

}
