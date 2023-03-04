package com.sgqn.accesslimit.provider;

import com.sgqn.accesslimit.entity.AccessLimitInfo;
import org.springframework.beans.factory.InitializingBean;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-03-03 10:51:25
 * @modify:
 */

public interface AccessLimitInfoProvider extends InitializingBean {

    @Override
    void afterPropertiesSet() throws Exception;

    AccessLimitInfo getAccessLimitInfo(String uri);
}
