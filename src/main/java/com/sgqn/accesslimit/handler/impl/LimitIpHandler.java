package com.sgqn.accesslimit.handler.impl;

import com.sgqn.accesslimit.autoconfig.AccessLimitProperties;
import com.sgqn.accesslimit.entity.RequestInfo;
import com.sgqn.accesslimit.handler.AccessLimitHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @description: 默认实现了 {@link AccessLimitHandler} 。默认的处理方式为：首次超过请求频率限制后，将被拉黑4分钟，当日内再次超过限制后将会翻倍。
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-02-28 9:47:07
 * @modify:
 */

@Slf4j
public class DefaultAccessLimitHandler implements AccessLimitHandler {

    /**
     * 黑名单 前缀
     */
    private static final String BLACK_LIST_REDIS_PREFIX = "black-list";

    /**
     * 当日最高封禁时间 前缀
     */
    private static final String DAY_MAX_BLOCK_TIME_PREFIX = "day-max-block-time";

    /**
     * 默认封禁时间，单位为 分
     */
    private static final Long DEFAULT_BLOCK_TIME = 120L;

    @Autowired
    private AccessLimitProperties accessLimitProperties;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 黑名单键。
     * 键：{@link AccessLimitProperties#getRedisBasePath()} + ":" + {@link #BLACK_LIST_REDIS_PREFIX}
     * 哈希键：IP
     * 哈希值：到期时间戳
     */
    private String blackListKey;

    /**
     * 当日最大封禁时间。
     * 键：{@link AccessLimitProperties#getRedisBasePath()} + ":" + {@link #DAY_MAX_BLOCK_TIME_PREFIX}
     * 哈希键：IP
     * 哈希值：最大时间（秒）
     */
    private String dayMaxBlockingTimeKey;

    @Override
    public void init() {
        blackListKey = String.format("%s:%s", accessLimitProperties.getRedisBasePath(), BLACK_LIST_REDIS_PREFIX);
        dayMaxBlockingTimeKey = String.format("%s:%s", accessLimitProperties.getRedisBasePath(), DAY_MAX_BLOCK_TIME_PREFIX);
    }

    /**
     * 默认实现中，本方法将对 IP 的访问进行限制。
     * 限制策略默认为：加入黑名单，拉黑时长 = 上一次拉黑时长 * 2。初始拉黑时长为 4 分钟
     */
    @Override
    public void process(List<RequestInfo> requestInfo, String ip) {
        long blockingTime;
        String dayMaxBlockingTime = redisTemplate.<String, String>opsForHash().get(dayMaxBlockingTimeKey, ip);
        if (Objects.isNull(dayMaxBlockingTime)) {
            blockingTime = DEFAULT_BLOCK_TIME;
        } else {
            blockingTime = Long.parseLong(dayMaxBlockingTime) >> 1;
        }
        redisTemplate.<String, String>opsForHash().put(
                blackListKey,
                ip,
                String.valueOf(Timestamp.valueOf(LocalDateTime.now()).getTime() / 1000 + blockingTime)
        );
        redisTemplate.<String, String>opsForHash().put(
                dayMaxBlockingTimeKey,
                ip,
                String.valueOf(blockingTime)
        );
        logAllRequestInfo(requestInfo);
    }

    /**
     * 获取剩余封禁时间
     *
     * @param ip 请求 IP
     * @return 返回 剩余封禁时间，单位为 秒
     */
    @Override
    public Long getRemainingProcessingTime(String ip) {
        String timeStr = redisTemplate.<String, String>opsForHash().get(blackListKey, ip);
        if (Objects.isNull(timeStr)) {
            return -1L;
        }
        long time = Long.parseLong(timeStr);
        time = time - (System.currentTimeMillis() / 1000);
        return time > 0 ? time : -1;
    }

    private void logAllRequestInfo(List<RequestInfo> requestInfoList) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> 接口被恶意请求。以下是拦截的请求信息：");
        for (RequestInfo info : requestInfoList) {
            stringBuilder.append("\n").append(info);
        }
        log.info(stringBuilder.append("\n").toString());
    }

    /**
     * 每天凌晨四点清空
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void clearRedis() {
        redisTemplate.delete(blackListKey);
        redisTemplate.delete(dayMaxBlockingTimeKey);
    }
}
