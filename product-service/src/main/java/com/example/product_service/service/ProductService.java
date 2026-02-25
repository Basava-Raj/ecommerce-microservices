package com.example.product_service.service;

import com.example.product_service.exception.InsufficientStockException;
import com.example.product_service.exception.ResourceNotFoundException;
import com.example.product_service.model.Product;
import com.example.product_service.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        logger.info("Fetching all products");
        List<Product> products = productRepository.findAll();
        logger.info("Found {} products", products.size());
        return products;
    }

    public Product getProductById(Long id) {  // ← CHANGED: Return Product instead of Optional
        logger.info("Fetching product with id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public Product createProduct(Product product) {
        logger.info("Creating new product: {}", product.getName());
        product.setAvailability(product.getStock() > 0 ? "In Stock" : "Out of Stock");  // ← ADD THIS
        Product saved = productRepository.save(product);
        logger.info("Product created with id: {}", saved.getId());
        return saved;
    }

    public Product updateProduct(Long id, Product productDetails) {
        logger.info("Updating product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product not found with id: {}", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStock(productDetails.getStock());
        product.setAvailability(productDetails.getStock() > 0 ? "In Stock" : "Out of Stock");  // ← ADD THIS

        Product updated = productRepository.save(product);
        logger.info("Product updated successfully: {}", id);
        return updated;
    }

    public void deleteProduct(Long id) {
        logger.info("Deleting product with id: {}", id);
        if (!productRepository.existsById(id)) {
            logger.info("Cannot delete - Product not found with id: {}", id);
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        logger.info("Product deleted successfully: {}", id);
    }

    @Transactional
    public Product reduceStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getStock() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.getName() +
                            ". Available: " + product.getStock() + ", Requested: " + quantity
            );
        }

        product.setStock(product.getStock() - quantity);
        product.setAvailability(product.getStock() > 0 ? "In Stock" : "Out of Stock");

        return productRepository.save(product);
    }

    @Transactional
    public Product addStock(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setStock(product.getStock() + quantity);
        product.setAvailability(product.getStock() > 0 ? "In Stock" : "Out of Stock");

        return productRepository.save(product);
    }

    public boolean hasStock(Long productId, int quantity) {
        Product product = getProductById(productId);  // ← CHANGED: Now uses the updated method
        return product.getStock() >= quantity;
    }
}