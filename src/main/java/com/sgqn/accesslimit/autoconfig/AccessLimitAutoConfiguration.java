package com.sgqn.accesslimit.autoconfig;

import com.sgqn.accesslimit.filter.AccessLimitFilter;
import com.sgqn.accesslimit.handler.impl.LimitIpHandler;
import com.sgqn.accesslimit.provider.AccessLimitInfoProvider;
import com.sgqn.accesslimit.provider.impl.AbstractAccessLimitInfoProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Collections;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-02-28 15:43:10
 * @modify:
 */

@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnClass(RedisAutoConfiguration.class)
@ConditionalOnProperty(prefix = "access-limit", name = "enable", havingValue = "true")
@EnableScheduling
public class AccessLimitAutoConfiguration {

    @Bean(name = "accessLimitFilterFilterRegistrationBean")
    public FilterRegistrationBean<AccessLimitFilter> accessLimitFilterFilterRegistrationBean(AccessLimitFilter accessLimitFilter) {
        FilterRegistrationBean<AccessLimitFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(accessLimitFilter);
        bean.setOrder(1);
        bean.setUrlPatterns(Collections.singletonList("/*"));
        return bean;
    }

    @Bean
    public LimitIpHandler limitIpHandler() {
        return new LimitIpHandler();
    }

    @Bean
    public AccessLimitFilter accessLimitFilter() {
        return new AccessLimitFilter();
    }

    @Bean
    public AccessLimitProperties accessLimitProperties() {
        return new AccessLimitProperties();
    }

    @Bean
    @ConditionalOnMissingBean(AccessLimitInfoProvider.class)
    public AccessLimitInfoProvider AccessLimitInfoProvider() {
        return new AbstractAccessLimitInfoProvider();
    }
}
