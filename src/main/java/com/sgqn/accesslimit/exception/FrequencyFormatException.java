package com.sgqn.accesslimit.exception;

import com.sgqn.accesslimit.annotation.AccessLimit;

/**
 * @description: 该异常表示传入 {@link AccessLimit#frequency()} 的格式错误。
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-02-28 14:39:55
 * @modify:
 */

public class FrequencyFormatException extends RuntimeException {

    public FrequencyFormatException() {
    }

    public FrequencyFormatException(String message) {
        super(message);
    }

    public FrequencyFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public FrequencyFormatException(Throwable cause) {
        super(cause);
    }
}
