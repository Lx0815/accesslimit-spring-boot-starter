package com.sgqn.accesslimit.provider;

import java.util.function.Supplier;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-03-03 10:51:25
 * @modify:
 */

public interface ControllerUrisProvider extends Supplier<String> {

    @Override
    String get();

}
