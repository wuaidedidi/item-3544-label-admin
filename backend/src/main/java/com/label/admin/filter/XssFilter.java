package com.label.admin.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * XSS防护过滤器 - 对请求参数和Header进行HTML转义，防止XSS攻击
 */
@Component
@Order(1)
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String contentType = httpRequest.getContentType();

        // 文件上传请求不过滤
        if (contentType != null && contentType.contains("multipart/form-data")) {
            chain.doFilter(request, response);
            return;
        }

        chain.doFilter(new XssRequestWrapper(httpRequest), response);
    }

    private static class XssRequestWrapper extends HttpServletRequestWrapper {

        private static final java.util.Set<String> SKIP_HEADERS = java.util.Set.of(
                "accept", "content-type", "authorization", "cookie", "host",
                "user-agent", "referer", "origin", "connection", "cache-control",
                "accept-encoding", "accept-language", "content-length", "if-modified-since"
        );

        public XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return value != null ? cleanXss(value) : null;
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;
            String[] cleaned = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                cleaned[i] = cleanXss(values[i]);
            }
            return cleaned;
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            if (name != null && SKIP_HEADERS.contains(name.toLowerCase())) {
                return value;
            }
            return value != null ? cleanXss(value) : null;
        }

        private String cleanXss(String value) {
            if (value == null || value.isEmpty()) return value;
            value = value.replace("&", "&amp;");
            value = value.replace("<", "&lt;");
            value = value.replace(">", "&gt;");
            value = value.replace("\"", "&quot;");
            value = value.replace("'", "&#x27;");
            // 移除javascript:协议
            value = value.replaceAll("(?i)javascript:", "");
            value = value.replaceAll("(?i)on\\w+\\s*=", "");
            return value;
        }
    }
}
