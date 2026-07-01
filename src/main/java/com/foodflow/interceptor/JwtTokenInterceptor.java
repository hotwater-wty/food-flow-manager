package com.foodflow.interceptor;

import com.foodflow.common.constant.JwtClaimConstants;
import com.foodflow.common.context.LoginContext;
import com.foodflow.common.context.LoginInfo;
import com.foodflow.common.enums.EmployeeRoleEnum;
import com.foodflow.common.enums.EmployeeStatusEnum;
import com.foodflow.common.enums.LoginTypeEnum;
import com.foodflow.common.enums.UserStatusEnum;
import com.foodflow.common.result.CacheResult;
import com.foodflow.common.utils.AccountStatusCacheClient;
import com.foodflow.common.utils.JwtUtil;
import com.foodflow.module.employee.entity.Employee;
import com.foodflow.module.employee.mapper.EmployeeMapper;
import com.foodflow.module.user.entity.User;
import com.foodflow.module.user.mapper.UserMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    // JWT token 前缀
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserMapper userMapper;
    private final EmployeeMapper employeeMapper;
    private final AccountStatusCacheClient accountStatusCacheClient;

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

            // 当前项目登录时会把 userId 或 employeeId 写入 Token，后续业务依赖它识别当前用户或员工。
            Object userIdClaim = claims.get(JwtClaimConstants.USER_ID);
            Object employeeIdClaim = claims.get(JwtClaimConstants.EMPLOYEE_ID);
            Long userId = userIdClaim == null ? null : Long.valueOf(userIdClaim.toString());
            Long employeeId = employeeIdClaim == null ? null : Long.valueOf(employeeIdClaim.toString());
            if (userId == null && employeeId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            // 校验登录类型是否匹配当前请求路径
            String path = request.getRequestURI();
            String loginTypeValue = claims.get(JwtClaimConstants.LOGIN_TYPE, String.class);
            if (loginTypeValue == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            LoginTypeEnum loginType = LoginTypeEnum.valueOf(loginTypeValue);
            if (path.startsWith("/api/user/")
                    && LoginTypeEnum.USER != loginType) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
            if (path.startsWith("/api/admin/")
                    && LoginTypeEnum.EMPLOYEE != loginType) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }

            // 校验用户或员工账号
            if (loginType == LoginTypeEnum.USER && !isNormalUser(userId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }
            if (loginType == LoginTypeEnum.EMPLOYEE && !isNormalEmployee(employeeId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }

            // 校验店长权限是否匹配当前请求路径
            String roleValue = claims.get(JwtClaimConstants.ROLE, String.class);
            EmployeeRoleEnum employeeRole = roleValue == null ? null : EmployeeRoleEnum.valueOf(roleValue);
            if (isManagerOnlyPath(request) && EmployeeRoleEnum.MANAGER != employeeRole) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return false;
            }

            // 校验通过后，把当前用户 ID 存入请求线程上下文，供后续 Controller/Service 使用。
            LoginContext.set(LoginInfo.builder()
                    .userId(userId)
                    .employeeId(employeeId)
                    .loginType(loginType)
                    .employeeRole(employeeRole)
                    .build());
                    
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.info("Invalid JWT token", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, 
            HttpServletResponse response, Object handler, Exception ex) {
        // 请求处理完成后清理 ThreadLocal，避免 Tomcat 线程复用造成用户信息串用。
        LoginContext.clear();
    }

    // 查用户账号缓存，并校验状态是否正常
    private boolean isNormalUser(Long userId) {
        CacheResult<String> cacheResult = accountStatusCacheClient.getUserStatusCache(userId);
        if (cacheResult.isHit()) {
            return String.valueOf(UserStatusEnum.NORMAL.getCode()).equals(cacheResult.getData());
        }
        if (cacheResult.isEmpty()) {
            return false;
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            accountStatusCacheClient.setUserStatusEmptyCache(userId);
            return false;
        }

        accountStatusCacheClient.setUserStatusCache(userId, user.getStatus().getCode());

        return user.getStatus() == UserStatusEnum.NORMAL;
    }

    // 查员工账号缓存，并校验状态是否正常
    private boolean isNormalEmployee(Long employeeId) {
        CacheResult<String> cacheResult = accountStatusCacheClient.getEmployeeStatusCache(employeeId);
        if (cacheResult.isHit()) {
            return String.valueOf(EmployeeStatusEnum.NORMAL.getCode()).equals(cacheResult.getData());
        }
        if (cacheResult.isEmpty()) {
            return false;
        }

        Employee employee = employeeMapper.selectById(employeeId);
        if (employee == null) {
            accountStatusCacheClient.setEmployeeStatusEmptyCache(employeeId);
            return false;
        }

        accountStatusCacheClient.setEmployeeStatusCache(employeeId, employee.getStatus().getCode());

        return employee.getStatus() == EmployeeStatusEnum.NORMAL;
    }

    // 校验请求路径是否为店长权限路径
    private boolean isManagerOnlyPath(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("POST".equals(method) && "/api/admin/auth/register".equals(path)) {
            return true;
        }

        if ("DELETE".equals(method) && path.matches("^/api/admin/tables/\\d+$")) {
            return true;
        }

        return path.startsWith("/api/admin/employees");
    }
}
