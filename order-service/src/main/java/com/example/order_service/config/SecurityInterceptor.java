package com.example.order_service.config;

import com.example.order_service.security.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SecurityInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Extract user info from headers set by API Gateway
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-User-Name");
        String email = request.getHeader("X-User-Email");
        String role = request.getHeader("X-User-Role");

        // Set security context
        if (userId != null) {
            SecurityContext context = new SecurityContext();
            context.setUserId(Long.parseLong(userId));
            context.setUsername(username);
            context.setEmail(email);
            context.setRole(role);
            SecurityContext.setContext(context);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SecurityContext.clear();
    }
}