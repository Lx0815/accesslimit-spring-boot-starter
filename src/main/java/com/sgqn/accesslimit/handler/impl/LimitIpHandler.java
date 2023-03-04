package com.sgqn.accesslimit.handler.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgqn.accesslimit.autoconfig.AccessLimitProperties;
import com.sgqn.accesslimit.entity.AccessLimitInfo;
import com.sgqn.accesslimit.entity.RequestInfo;
import com.sgqn.accesslimit.exception.UpdateAccessCountException;
import com.sgqn.accesslimit.handler.AccessLimitHandler;
import com.sgqn.accesslimit.utils.ServletStreamUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description: 默认实现了 {@link AccessLimitHandler} 。默认的处理方式为：首次超过请求频率限制后，将被拉黑4分钟，当日内再次超过限制后将会翻倍。
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-02-28 9:47:07
 * @modify:
 */

@Slf4j
public class LimitIpHandler implements AccessLimitHandler {

    /**
     * 黑名单 前缀
     */
    private static final String BLACK_LIST_REDIS_PREFIX = "black-list";

    /**
     * 当日最高封禁时间 前缀
     */
    private static final String DAY_MAX_BLOCK_TIME_REDIS_PREFIX = "day-max-block-time";

    /**
     * 请求信息列表 前缀
     */
    private static final String REQUEST_INFO_REDIS_PREFIX = "requests-info";

    /**
     * 默认封禁时间，单位为 分
     */
    private static final Long DEFAULT_BLOCK_TIME = 120L;

    @Autowired
    private AccessLimitProperties accessLimitProperties;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 黑名单键。<br>
     * 键：{@link AccessLimitProperties#getRedisBasePath()} + ":" + {@link #BLACK_LIST_REDIS_PREFIX} <br>
     * 哈希键：{@link #getHashKey(RequestInfo)} <br>
     * 哈希值：到期时间 <br>
     */
    private String blackListKey;

    /**
     * 当日最大封禁时间。 <br>
     * 键：{@link AccessLimitProperties#getRedisBasePath()} + ":" + {@link #DAY_MAX_BLOCK_TIME_REDIS_PREFIX} <br>
     * 哈希键：{@link #getHashKey(RequestInfo)} <br>
     * 哈希值：最大时间（秒） <br>
     */
    private String dayMaxBlockingTimeKey;

    /**
     * 存储请求列表的 key。 <br>
     * 键：{@link AccessLimitProperties#getRedisBasePath()} + ":" + {@link #REQUEST_INFO_REDIS_PREFIX} <br>
     * 哈希键：{@link #getHashKey(RequestInfo)} <br>
     * 哈希值：该 IP 在限定时间内的请求信息集合 <br>
     */
    private String requestInfoListKey;

    /**
     * 获取当前请求对应的 HashKey
     *
     * @param requestInfo 当前请求信息
     * @return 返回对应的 HashKey
     */
    private static String getHashKey(RequestInfo requestInfo) {
        return requestInfo.getIp() + "_:_" + requestInfo.getUri();
    }

    @Override
    public void init() {
        blackListKey = String.format("%s:%s", accessLimitProperties.getRedisBasePath(), BLACK_LIST_REDIS_PREFIX);
        dayMaxBlockingTimeKey = String.format("%s:%s", accessLimitProperties.getRedisBasePath(), DAY_MAX_BLOCK_TIME_REDIS_PREFIX);
        requestInfoListKey = String.format("%s:%s", accessLimitProperties.getRedisBasePath(), REQUEST_INFO_REDIS_PREFIX);
    }

    /**
     * 默认实现中，本方法将对 IP 的访问进行限制。
     * 限制策略默认为：加入黑名单，拉黑时长 = 上一次拉黑时长 * 2。初始拉黑时长为 4 分钟
     *
     * @param requestInfo 请求信息
     * @param accessLimitInfo 访问限制信息
     */
    @Override
    public boolean handle(RequestInfo requestInfo, AccessLimitInfo accessLimitInfo) {
        // 使用过期时间判断是否已经被 Handler 处理
        Long remainingProcessingTime = getRemainingProcessingTime(requestInfo);
        if (remainingProcessingTime > 0) {
            log.info(requestInfo + "\nERROR: 已被封禁。剩余 " + remainingProcessingTime + " s");
            return true;
        }
        // 更新访问次数
        updateAccessCount(requestInfo);
        // 移除过期的请求信息
        List<RequestInfo> requestInfoList = removeExpired(accessLimitInfo, requestInfo);
        if (requestInfoList.size() > accessLimitInfo.getCount()) {
            doProcess(requestInfo);
            logAllRequestInfo(requestInfo);
            return true;
        }
        return false;
    }

    /**
     * 封禁用户对该接口的访问
     *
     * @param requestInfo 当前请求信息
     */
    private void doProcess(RequestInfo requestInfo) {
        long blockingTime;
        String dayMaxBlockingTime = redisTemplate.<String, String>opsForHash().get(dayMaxBlockingTimeKey, getHashKey(requestInfo));
        if (Objects.isNull(dayMaxBlockingTime)) {
            blockingTime = DEFAULT_BLOCK_TIME;
        } else {
            blockingTime = Long.parseLong(dayMaxBlockingTime) >> 1;
        }
        redisTemplate.<String, String>opsForHash().put(
                blackListKey,
                getHashKey(requestInfo),
                String.valueOf(LocalDateTime.now().plusMinutes(blockingTime))
        );
        redisTemplate.<String, String>opsForHash().put(
                dayMaxBlockingTimeKey,
                getHashKey(requestInfo),
                String.valueOf(blockingTime)
        );
    }

    /**
     * 获取剩余封禁时间
     *
     * @param requestInfo 请求 信息
     * @return 返回 剩余封禁时间，单位为 秒
     */
    public Long getRemainingProcessingTime(RequestInfo requestInfo) {
        String timeStr = redisTemplate.<String, String>opsForHash().get(blackListKey, getHashKey(requestInfo));
        if (Objects.isNull(timeStr)) {
            return -1L;
        }
        LocalDateTime localDateTime = LocalDateTime.parse(timeStr);
        return Duration.between(localDateTime, LocalDateTime.now()).getSeconds();
    }

    /**
     * 当用户被封禁后，响应错误信息
     *
     * @param request  请求
     * @param response 响应
     */
    @Override
    public void responseError(HttpServletRequest request, HttpServletResponse response) {
        ServletStreamUtil.write403(response, "服务器繁忙，请稍后再试。");
    }

    /**
     * 打印全部请求信息
     *
     * @param requestInfo 当前请求信息
     */
    private void logAllRequestInfo(RequestInfo requestInfo) {
        List<RequestInfo> requestInfoList = getRequestInfoList(requestInfo);
        Assert.notNull(requestInfoList, "此时不应该为空，但请求信息列表为空。");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(">>> 接口被恶意请求。以下是拦截的请求信息：");
        for (RequestInfo info : requestInfoList) {
            stringBuilder.append("\n").append(info);
        }
        log.info(stringBuilder.append("\n").toString());
    }

    /**
     * 更新 Redis 中的访问次数
     *
     * @param requestInfo 请求对象信息
     */
    public void updateAccessCount(RequestInfo requestInfo) {
        List<RequestInfo> requestInfoList = getRequestInfoList(requestInfo);
        if (Objects.isNull(requestInfoList)) {
            requestInfoList = new LinkedList<>();
        }
        requestInfoList.add(requestInfo);
        updateRequestInfoList(requestInfo, requestInfoList);
    }

    /**
     * 移除已过期的请求信息
     *
     * @param requestInfo 当前请求信息
     */
    public List<RequestInfo> removeExpired(AccessLimitInfo accessLimitInfo, RequestInfo requestInfo) {
        // 获取历史访问信息
        List<RequestInfo> requestInfoList = getRequestInfoList(requestInfo);
        if (Objects.nonNull(requestInfoList)) {
            // 去掉过期的
            requestInfoList = requestInfoList.stream()
                    .filter(info -> !requestInfo.isOver(accessLimitInfo.getTimeout()))
                    .collect(Collectors.toList());
            updateRequestInfoList(requestInfo, requestInfoList);
        } else {
            requestInfoList = new LinkedList<>();
        }
        return requestInfoList;
    }

    /**
     * 获取历史请求信息
     *
     * @param requestInfo 当前请求信息
     * @return 返回包含历史请求信息的列表，始终不为 null
     */
    @SuppressWarnings("unchecked")
    @Nullable
    private List<RequestInfo> getRequestInfoList(RequestInfo requestInfo) {
        try {
            String json = redisTemplate.<String, String>opsForHash().get(requestInfoListKey, getHashKey(requestInfo));
            JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, RequestInfo.class);
            return Objects.isNull(json) ? null : (List<RequestInfo>) objectMapper.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将新的请求信息列表更新到 Redis 中
     *
     * @param requestInfo     当前请求信息
     * @param requestInfoList 新的请求信息列表
     */
    private void updateRequestInfoList(RequestInfo requestInfo, List<RequestInfo> requestInfoList) {
        try {
            redisTemplate.<String, String>opsForHash().put(
                    requestInfoListKey,
                    getHashKey(requestInfo),
                    objectMapper.writeValueAsString(requestInfoList)
            );
        } catch (JsonProcessingException e) {
            throw new UpdateAccessCountException(e);
        }
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
