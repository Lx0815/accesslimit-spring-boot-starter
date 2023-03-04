package com.sgqn.accesslimit.entity;

import com.sgqn.accesslimit.utils.IpUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @description:
 * @author: Ding
 * @version: 1.0
 * @createTime: 2023-02-28 13:50:49
 * @modify:
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestInfo {

    /**
     * 访问 IP
     */
    private String ip;

    /**
     * 请求 方法
     */
    private HttpMethod method;

    /**
     * 请求 接口
     */
    private String uri;

    /**
     * 携带的请求头
     */
    private Map<String, List<String>> headers;

    /**
     * 携带的参数列表
     */
    private Map<String, String[]> parametersMap;

    /**
     * 访问时间
     */
    private LocalDateTime accessTime;

    public RequestInfo(HttpServletRequest request) {
        this.ip = IpUtil.getRealIp(request);
        this.method = HttpMethod.resolve(request.getMethod().toUpperCase());
        this.uri = request.getRequestURI();
        this.headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> enumeration = request.getHeaders(headerName);
            List<String> list = new LinkedList<>();
            while (enumeration.hasMoreElements()) {
                list.add(enumeration.nextElement());
            }
            headers.put(headerName, list);
        }
        this.parametersMap = request.getParameterMap();
        this.accessTime = LocalDateTime.now();
    }

    /**
     * 判断是否过期
     *
     * @param timeout 存活时间，单位 秒
     * @return 返回 true 表示过期
     */
    public boolean isOver(Long timeout) {
        return accessTime.plusSeconds(timeout).isBefore(LocalDateTime.now());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n")
                .append("IP: ").append(ip).append("\n")
                .append("Method: ").append(method).append("\n")
                .append("Uri: ").append(uri).append("\n")
                .append("AccessTime: ").append(accessTime).append("\n");

        stringBuilder.append("Headers: \n");
        headers.forEach((name, valueList) -> {
            stringBuilder.append("\t\t").append(name).append(": ").append(valueList).append("\n");
        });
        stringBuilder.append("Parameters: \n");
        parametersMap.forEach((name, valueArr) -> {
            stringBuilder.append("\t\t\t").append(name).append(": ").append(Arrays.toString(valueArr)).append("\n");
        });
        return stringBuilder.toString();
    }
}
