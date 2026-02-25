package com.example.order_service.controller;

import com.example.order_service.model.Order;
import com.example.order_service.model.OrderStatus;
import com.example.order_service.security.SecurityContext;
import com.example.order_service.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));  // ← CHANGED: Now throws exception if not found
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody Order order) {
        // Customer info is set from SecurityContext in service
        Order created = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        Order cancelledOrder = orderService.cancelOrder(id);
        return ResponseEntity.ok(cancelledOrder);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            @RequestParam(required = false) String message) {
        Order updatedOrder = orderService.updateOrderStatus(id, status, message);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @GetMapping("/customer/{email}")
    public ResponseEntity<List<Order>> getOrdersByCustomerEmail(@PathVariable String email) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerEmail(email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Order deleted successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getOrderStats() {

        // Only ADMIN can access stats
        SecurityContext context = SecurityContext.getContext();
        if (context != null && context.isCustomer()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orderService.getAllOrders().size());
        stats.put("pendingOrders", orderService.getOrdersByStatus(OrderStatus.PENDING).size());
        stats.put("confirmedOrders", orderService.getOrdersByStatus(OrderStatus.CONFIRMED).size());
        stats.put("processingOrders", orderService.getOrdersByStatus(OrderStatus.PROCESSING).size());
        stats.put("shippedOrders", orderService.getOrdersByStatus(OrderStatus.SHIPPED).size());
        stats.put("deliveredOrders", orderService.getOrdersByStatus(OrderStatus.DELIVERED).size());
        stats.put("cancelledOrders", orderService.getOrdersByStatus(OrderStatus.CANCELLED).size());
        stats.put("failedOrders", orderService.getOrdersByStatus(OrderStatus.FAILED).size());

        return ResponseEntity.ok(stats);
    }
}