package com.example.product_service.controller;

import com.example.product_service.dto.ProductRequest;
import com.example.product_service.dto.ProductResponse;
import com.example.product_service.dto.StockUpdateRequest;
import com.example.product_service.model.Product;
import com.example.product_service.security.SecurityContext;
import com.example.product_service.service.ProductService;
import com.example.product_service.util.ProductMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Get all products - Public access (all authenticated users)
     */

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts()
                .stream()
                .map(ProductMapper::toResponse)
                .toList();
        return ResponseEntity.ok(products);
    }

    /**
     * Get product by ID - Public access
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);  // ← CHANGED
        return ResponseEntity.ok(ProductMapper.toResponse(product));  // ← CHANGED
    }

    /**
     * Create product - ADMIN ONLY
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        // Check if user is ADMIN
        SecurityContext context = SecurityContext.getContext();
        if (context == null || !context.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Product product = ProductMapper.toEntity(request);
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductMapper.toResponse(created));
    }

    /**
     * Update product - ADMIN ONLY
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {

        // Check if user is ADMIN
        SecurityContext context = SecurityContext.getContext();
        if (context == null || !context.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Product product = ProductMapper.toEntity(request);
        Product updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(ProductMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {  // ← CHANGED

        // Check if user is ADMIN
        SecurityContext context = SecurityContext.getContext();
        if (context == null || !context.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        productService.deleteProduct(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product deleted successfully");
        return ResponseEntity.ok(response);  // ← CHANGED
    }

    /**
     * Reduce stock - Internal use by Order Service
     */

    @PostMapping("/reduce-stock")
    public ResponseEntity<Product> reduceStock(@Valid @RequestBody StockUpdateRequest request) {
        Product updatedProduct = productService.reduceStock(
                request.getProductId(),
                request.getQuantity()
        );
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Add stock - Internal use by Order Service
     */
    @PostMapping("/add-stock")
    public ResponseEntity<Product> addStock(@Valid @RequestBody StockUpdateRequest request) {
        Product updatedProduct = productService.addStock(
                request.getProductId(),
                request.getQuantity()
        );
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Check stock - Public access
     */
    @GetMapping("/{id}/check-stock")
    public ResponseEntity<Map<String, Object>> checkStock(
            @PathVariable Long id,
            @RequestParam int quantity) {

        Product product = productService.getProductById(id);  // ← CHANGED: Now throws exception if not found
        boolean available = productService.hasStock(id, quantity);

        Map<String, Object> response = new HashMap<>();
        response.put("productId", id);
        response.put("productName", product.getName());
        response.put("requestedQuantity", quantity);
        response.put("availableStock", product.getStock());
        response.put("isAvailable", available);

        return ResponseEntity.ok(response);
    }
}