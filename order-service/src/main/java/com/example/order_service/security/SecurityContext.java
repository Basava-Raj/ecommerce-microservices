package com.example.order_service.security;

import lombok.Data;

@Data
public class SecurityContext {
    private Long userId;
    private String username;
    private String email;
    private String role;

    private static final ThreadLocal<SecurityContext> contextHolder = new ThreadLocal<>();

    public static void setContext(SecurityContext context) {
        contextHolder.set(context);
    }

    public static SecurityContext getContext() {
        return contextHolder.get();
    }

    public static void clear() {
        contextHolder.remove();
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isCustomer() {
        return "CUSTOMER".equals(role);
    }
}
