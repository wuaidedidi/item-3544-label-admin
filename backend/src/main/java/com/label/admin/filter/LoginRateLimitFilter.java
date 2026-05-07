package com.label.admin.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.label.admin.common.Result;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录接口限流过滤器 - 防止暴力破解
 * 同一IP在5分钟内最多尝试登录10次，超过后锁定5分钟
 */
@Component
@Order(0)
public class LoginRateLimitFilter implements Filter {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_MS = 5 * 60 * 1000;

    private final Map<String, long[]> attemptCache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if ("POST".equalsIgnoreCase(httpRequest.getMethod())
                && httpRequest.getRequestURI().contains("/api/auth/login")) {

            String ip = getClientIp(httpRequest);
            if (isRateLimited(ip)) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.setStatus(429);
                httpResponse.getWriter().write(objectMapper.writeValueAsString(
                        Result.error(429, "登录尝试过于频繁，请5分钟后再试")
                ));
                return;
            }
            recordAttempt(ip);
        }

        chain.doFilter(request, response);
    }

    private boolean isRateLimited(String ip) {
        long[] record = attemptCache.get(ip);
        if (record == null) return false;
        long windowStart = record[0];
        long count = (long) record[1];
        if (System.currentTimeMillis() - windowStart > WINDOW_MS) {
            attemptCache.remove(ip);
            return false;
        }
        return count >= MAX_ATTEMPTS;
    }

    private void recordAttempt(String ip) {
        attemptCache.compute(ip, (key, record) -> {
            long now = System.currentTimeMillis();
            if (record == null || now - record[0] > WINDOW_MS) {
                return new long[]{now, 1};
            }
            record[1]++;
            return record;
        });
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Forwarded-For");
            if (ip != null && ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
