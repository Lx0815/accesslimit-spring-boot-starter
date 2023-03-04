package com.sgqn.accesslimit.exception;

/**
 * @description: 更新访问次数异常。
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-02-28 14:17:32
 * @modify:
 */

public class UpdateAccessCountException extends RuntimeException {
    public UpdateAccessCountException() {
    }

    public UpdateAccessCountException(String message) {
        super(message);
    }

    public UpdateAccessCountException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpdateAccessCountException(Throwable cause) {
        super(cause);
    }
}
