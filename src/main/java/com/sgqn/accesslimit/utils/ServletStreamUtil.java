package com.sgqn.accesslimit.utils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-03-04 9:56:35
 * @modify:
 */

public class ServletStreamUtil {

    private ServletStreamUtil() {
    }

    public static void write403(HttpServletResponse response, String message) {
        write(response, HttpServletResponse.SC_FORBIDDEN, message);
    }

    public static void write(HttpServletResponse response, int statusCode, String message) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("content-type", "text/html;charset=utf-8");
        try {
            ServletOutputStream out = response.getOutputStream();
            response.setStatus(statusCode);
            out.write(message.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
