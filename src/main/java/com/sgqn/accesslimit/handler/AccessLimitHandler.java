package com.sgqn.accesslimit.handler;

import com.sgqn.accesslimit.entity.AccessLimitInfo;
import com.sgqn.accesslimit.entity.RequestInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-02-28 9:45:29
 * @modify:
 */

public interface AccessLimitHandler {

    /**
     * 当该用户访问频率达到限制时，将会调用该方法对 IP 进行处理。
     *
     * @param requestInfo 请求信息
     * @param accessLimitInfo 访问限制信息
     */
    boolean handle(RequestInfo requestInfo, AccessLimitInfo accessLimitInfo);

    /**
     * 初始化方法
     */
    default void init() {
    }

    /**
     * 是否正在被处理中
     *
     * @param requestInfo 请求 IP
     * @return 返回 True 表示正在被处理
     */
    Long getRemainingProcessingTime(RequestInfo requestInfo);

    /**
     * 当该接口已经不被允许访问时响应错误消息
     *
     * @param request  请求
     * @param response 响应
     */
    void responseError(HttpServletRequest request, HttpServletResponse response);

}
