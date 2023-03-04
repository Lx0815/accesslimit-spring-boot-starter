package com.sgqn.accesslimit.filter;

import com.sgqn.accesslimit.entity.AccessLimitInfo;
import com.sgqn.accesslimit.entity.RequestInfo;
import com.sgqn.accesslimit.handler.AccessLimitHandler;
import com.sgqn.accesslimit.provider.AccessLimitInfoProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * @description: 访问频率过滤器，本过滤器通过 {@link com.sgqn.accesslimit.handler.AccessLimitHandler} 进行具体的管理操作
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-02-28 9:58:04
 * @modify:
 */

@Slf4j
public class AccessLimitFilter implements Filter {


    @Autowired
    private AccessLimitInfoProvider accessLimitInfoProvider;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = ((HttpServletRequest) servletRequest);
        // 获取该接口上的 @AccessLimit 注解所定义的信息
        AccessLimitInfo accessLimitInfo = accessLimitInfoProvider.getAccessLimitInfo(request.getRequestURI());
        if (Objects.nonNull(accessLimitInfo)) {
            RequestInfo requestInfo = new RequestInfo(request);
            // 获取 @AccessLimit 注解中定义的处理器类对象
            AccessLimitHandler handler = accessLimitInfo.getHandler();
            // 调用 handle 进行处理
            if (handler.handle(requestInfo, accessLimitInfo)) {
                // 本次请求被视为异常请求
                handler.responseError(request, (HttpServletResponse) response);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
