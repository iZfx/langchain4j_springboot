package com.example.langchain4j_springboot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 安全配置类
 * 用于处理非法HTTP请求和安全相关配置
 */
@Configuration
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * 请求过滤器，用于拦截和记录非法请求
     */
    @Bean
    public FilterRegistrationBean<RequestValidationFilter> requestValidationFilter() {
        FilterRegistrationBean<RequestValidationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestValidationFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    /**
     * 请求验证过滤器
     */
    public static class RequestValidationFilter implements Filter {
        
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            
            // 记录可疑请求
            String method = httpRequest.getMethod();
            String uri = httpRequest.getRequestURI();
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // 检查是否为明显的HTTPS流量被错误发送到HTTP端口
            if (method != null && method.length() > 10) {
                // 检查TLS握手特征
                if (method.startsWith("\u0016\u0003") || 
                    method.contains("\u0000") || 
method.matches(".*[\\x00-\\x1F&&[^\\r\\n\\t]].*")) {
                    logger.warn("检测到非法HTTP请求 - 可能是HTTPS流量被发送到HTTP端口");
                    logger.warn("请求详情 - Method: {}, URI: {}, User-Agent: {}", 
                               method.substring(0, Math.min(method.length(), 50)), uri, userAgent);
                    
                    // 拒绝此类请求，避免Tomcat继续处理导致错误
                    jakarta.servlet.http.HttpServletResponse httpResponse = 
                        (jakarta.servlet.http.HttpServletResponse) response;
                    httpResponse.sendError(400, "Bad Request - Invalid protocol");
                    return;
                }
            }
            
            chain.doFilter(request, response);
        }
    }
}