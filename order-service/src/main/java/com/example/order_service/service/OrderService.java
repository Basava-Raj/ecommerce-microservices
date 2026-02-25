package com.example.order_service.service;

import com.example.order_service.client.ProductServiceClient;
import com.example.order_service.dto.ProductDTO;
import com.example.order_service.exception.InsufficientStockException;
import com.example.order_service.exception.ResourceNotFoundException;
import com.example.order_service.model.Order;
import com.example.order_service.model.OrderStatus;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductServiceClient productServiceClient;

    public List<Order> getAllOrders() {
        logger.info("Fetching all orders");
        SecurityContext context = SecurityContext.getContext();

        if(context != null && context.isCustomer()) {
            //Customers see only their own orders
            logger.info("Customer {} fetching their orders", context.getEmail());
            return orderRepository.findByCustomerEmail(context.getEmail());
        }

        // Admin sees  all orders
        return orderRepository.findAll();
    }


    /**
     * Get order by ID - Check ownership
     */

    public Order getOrderById(Long id) {
        logger.info("Fetching order with id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Check authorization
        SecurityContext context = SecurityContext.getContext();
        if (context != null && context.isCustomer()) {
            if (!order.getCustomerEmail().equals(context.getEmail())) {
                throw new RuntimeException("Access denied: you can only view your own orders");
            }
        }
        return order;
    }

    /**
     * Create order with inventory management
     */
    @Transactional
    public Order createOrder(Order order) {
        try {
            // Set customer info from security context
            SecurityContext context = SecurityContext.getContext();
            if (context != null) {
                order.setCustomerName(context.getUsername());
                order.setCustomerEmail(context.getEmail());
                logger.info("Creating order for user: {}", context.getEmail());
            }

            logger.info("Creating order for product ID: {}, quantity: {}",
                    order.getProductId(), order.getQuantity());

            // Step 1: Get product details from Product Service
            ProductDTO product = productServiceClient.getProduct(order.getProductId());

            if (product == null) {
                throw new ResourceNotFoundException("Product not found with id: " + order.getProductId());
            }

            logger.info("Product found: {} - Price: {}, Stock: {}",
                    product.getName(), product.getPrice(), product.getStock());

            // Step 2: Check stock availability
            if (!productServiceClient.checkStock(order.getProductId(), order.getQuantity())) {
                logger.error("Insufficient stock for product: {} . Available: {}, Requested: {}",
                        product.getName(), product.getStock(), order.getQuantity()
                        );
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.getName() +
                                ". Available: " + product.getStock() + ", Requested: " + order.getQuantity()
                );
            }

            // Step 3: Reduce stock in Product Service
            logger.info("Reducing stock for product: {}", product.getName());
            ProductDTO updatedProduct = productServiceClient.reduceStock(
                    order.getProductId(),
                    order.getQuantity()
            );

            // Step 4: Set order details
            order.setProductName(product.getName());
            order.setUnitPrice(product.getPrice());
            order.setTotalPrice(product.getPrice() * order.getQuantity());
            order.setStatus(OrderStatus.CONFIRMED);
            order.setStatusMessage("Order confirmed. Stock reserved.");

            Order savedOrder = orderRepository.save(order);
            logger.info("Order created successfully: ID {}, Total: ${}",
                    savedOrder.getId(), savedOrder.getTotalPrice());

            return savedOrder;

        } catch (InsufficientStockException e) {
            logger.error("Order creation failed - Insufficient stock: {}", e.getMessage());
            throw e;
        } catch (ResourceNotFoundException e) {
            logger.error("Order creation failed - Product not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Order creation failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }

    /**
     * Update order status
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus, String message) {
        Order order = getOrderById(orderId);

        // Check authorization - only ADMIN can update status
        SecurityContext context = SecurityContext.getContext();
        if (context != null && context.isCustomer()) {
            throw new RuntimeException("Access denied: Only admins can update order status");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setStatusMessage(message != null ? message : "Status updated to " + newStatus);

        logger.info("Order {} status changed: {} -> {}", orderId, oldStatus, newStatus);

        return orderRepository.save(order);
    }

    /**
     * Cancel order and return stock
     */
    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = getOrderById(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel a delivered order");
        }

        try {
            // Return stock to Product Service
            if (order.getStatus() == OrderStatus.CONFIRMED ||
                    order.getStatus() == OrderStatus.PROCESSING) {

                logger.info("Returning stock for cancelled order: {}", orderId);
                productServiceClient.addStock(order.getProductId(), order.getQuantity());
            }

            order.setStatus(OrderStatus.CANCELLED);
            order.setStatusMessage("Order cancelled by user. Stock returned.");

            logger.info("Order {} cancelled successfully", orderId);
            return orderRepository.save(order);

        } catch (Exception e) {
            logger.error("Failed to cancel order {}: {}", orderId, e.getMessage());
            throw new RuntimeException("Failed to cancel order: " + e.getMessage(), e);
        }
    }

    /**
     * Get orders by status
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        // ADMIN sees all, CUSTOMER sees only their own
        List<Order> orders = orderRepository.findByStatus(status);

        SecurityContext context = SecurityContext.getContext();
        if (context != null && context.isCustomer()) {
            return orders.stream()
                    .filter(order -> order.getCustomerEmail().equals(context.getEmail()))
                    .collect(Collectors.toList());
        }

        return orders;
    }

    /**
     * Get orders by customer email
     */
    public List<Order> getOrdersByCustomerEmail(String email) {
        // Check authorization
        SecurityContext context = SecurityContext.getContext();
        if (context != null && context.isCustomer()) {
            if (!email.equals(context.getEmail())) {
                throw new RuntimeException("Access denied: You can only view your own orders");
            }
        }

        return orderRepository.findByCustomerEmail(email);
    }

    public void deleteOrder(Long id) {

        // Only ADMIN can delete orders
        SecurityContext context = SecurityContext.getContext();
        if (context != null && context.isCustomer()) {
            throw new RuntimeException("Access denied: Only admins can delete orders");
        }

        logger.info("Deleting order with id: {}", id);
        Order order = getOrderById(id);
        orderRepository.delete(order);
        logger.info("Order deleted successfully: {}", id);
    }
}