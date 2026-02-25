package com.example.order_service.model;

public enum OrderStatus {
    PENDING,        // Order created, awaiting stock confirmation
    CONFIRMED,      // Stock reserved successfully
    PROCESSING,     // Order being prepared
    SHIPPED,        // Order dispatched
    DELIVERED,      // Order completed
    CANCELLED,      // Order cancelled (stock released)
    FAILED          // Order failed (stock not available or other error)
}
