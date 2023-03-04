package com.sgqn.accesslimit.entity;

import com.sgqn.accesslimit.exception.FrequencyFormatException;
import com.sgqn.accesslimit.handler.AccessLimitHandler;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-02-28 10:23:24
 * @modify:
 */

@Data
@NoArgsConstructor
public class AccessLimitInfo {
    private final String reg = "^\\d+/\\d+$";

    /**
     * 接口频率。格式必须为： {@code /^\\d+/\\d+$/} 即 x次/y秒。例如 5/60 表示 一分钟最多请求 5 次接口。<br>
     */
    private String frequency;

    /**
     * 用于实际处理访问频率限制。
     */
    private AccessLimitHandler handler;

    /**
     * 访问次数
     */
    private Integer count;

    /**
     * 单位时间（秒）
     */
    private Long timeout;

    public AccessLimitInfo(String frequency, AccessLimitHandler handler) {
        setFrequency(frequency);
        this.handler = handler;
    }

    public void setFrequency(String frequency) {
        checkFrequency(frequency);
        String[] strings = frequency.split("/");
        this.count = Integer.parseInt(strings[0]);
        this.timeout = Long.parseLong(strings[1]);
        this.frequency = frequency;
    }

    private void checkFrequency(String frequency) {
        if (!frequency.matches(reg)) {
            throw new FrequencyFormatException("接口频率格式错误。错误的格式为：" + frequency);
        }
    }
}
