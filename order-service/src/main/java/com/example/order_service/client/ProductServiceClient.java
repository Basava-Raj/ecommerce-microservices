package com.example.order_service.client;

import com.example.order_service.dto.ProductDTO;
import com.example.order_service.dto.StockUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class ProductServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    private static final String PRODUCT_SERVICE_URL = "http://product-service";


    /**
     * Get product details by ID
     */
    public ProductDTO getProduct(Long productId) {
        String url = PRODUCT_SERVICE_URL + "/products/" + productId;
        return restTemplate.getForObject(url, ProductDTO.class);
    }


    /**
     * Check if product has sufficient stock
     */
    public boolean checkStock(Long productId, int quantity) {
        try {
            String url = PRODUCT_SERVICE_URL + "/products/" + productId + "/check-stock?quantity=" + quantity;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            return response != null && (Boolean) response.get("isAvailable");
        } catch (Exception e) {
            throw new RuntimeException("Failed to check stock availability: " + e.getMessage());
        }
    }

    /**
     * Reduce stock when order is placed
     */
    public ProductDTO reduceStock(Long productId, int quantity) {
        String url = PRODUCT_SERVICE_URL + "/products/reduce-stock";
        StockUpdateRequest request = new StockUpdateRequest(productId, quantity);
        return restTemplate.postForObject(url, request, ProductDTO.class);
    }

    /**
     * Add stock back when order is cancelled
     */
    public ProductDTO addStock(Long productId, int quantity) {
        String url = PRODUCT_SERVICE_URL + "/products/add-stock";
        StockUpdateRequest request = new StockUpdateRequest(productId, quantity);
        return restTemplate.postForObject(url, request, ProductDTO.class);
    }

}
