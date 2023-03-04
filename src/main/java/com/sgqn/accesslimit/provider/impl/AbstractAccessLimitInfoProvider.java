package com.sgqn.accesslimit.provider.impl;

import com.sgqn.accesslimit.annotation.AccessLimit;
import com.sgqn.accesslimit.entity.AccessLimitInfo;
import com.sgqn.accesslimit.handler.AccessLimitHandler;
import com.sgqn.accesslimit.provider.AccessLimitInfoProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-03-03 9:43:32
 * @modify:
 */


public class AbstractAccessLimitInfoProvider implements AccessLimitInfoProvider, ApplicationContextAware {

    private final Map<String, AccessLimitInfo> accessLimitInfoMap = new HashMap<>();
    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    private ApplicationContext context;

    @Override
    public void afterPropertiesSet() {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethodEntry : handlerMethods.entrySet()) {
            RequestMappingInfo requestMappingInfo = requestMappingInfoHandlerMethodEntry.getKey();
            HandlerMethod handlerMethod = requestMappingInfoHandlerMethodEntry.getValue();
            // 获取方法上的 @AccessLimit 注解
            AccessLimit annotation = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), AccessLimit.class);
            // 如果该方法上不存在，那么就扫描该类是否存在注解
            if (Objects.isNull(annotation)) {
                annotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), AccessLimit.class);
            }
            if (Objects.isNull(annotation)) continue;
            Set<String> patternValues = requestMappingInfo.getPatternValues();
            for (String path : patternValues) {
                AccessLimitHandler handler = context.getBean(annotation.handler());
                handler.init();
                accessLimitInfoMap.put(path, new AccessLimitInfo(annotation.frequency(), handler));
            }
        }
    }

    public AccessLimitInfo getAccessLimitInfo(String uri) {
        return accessLimitInfoMap.get(uri);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
