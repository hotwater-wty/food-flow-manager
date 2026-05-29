package com.foodflow.interceptor;

import com.foodflow.common.context.LoginUserContext;
import com.foodflow.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    // JWT token 前缀
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 浏览器跨域预检请求不携带业务 Token，这里直接放行。
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从标准认证请求头中读取 Token，接口规范要求格式为：Authorization: Bearer xxx。
        String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            log.info("Missing or invalid Authorization header (未携带令牌)");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 去掉 Bearer 前缀，只保留真正的 JWT 字符串。
        String token = authorization.substring(BEARER_PREFIX.length());
        try {
            // 解析并校验 Token；签名错误、过期、格式错误都会抛出异常。
            Claims claims = JwtUtil.parseToken(token);

            // 当前项目登录时会把 userId 写入 Token，后续业务依赖它识别当前用户。
            Object userId = claims.get("userId");
            if (userId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 校验通过后，把当前用户 ID 存入请求线程上下文，供后续 Controller/Service 使用。
            LoginUserContext.setUserId(Long.valueOf(userId.toString()));
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.info("Invalid JWT token", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求处理完成后清理 ThreadLocal，避免 Tomcat 线程复用造成用户信息串用。
        LoginUserContext.clear();
    }
}
