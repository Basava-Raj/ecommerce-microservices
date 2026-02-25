package com.example.product_service.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 100)
    private String name;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 100)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01")
    private Double price;

    @NotNull(message = "stock is required")
    @Min(value = 0)
    private Integer stock;
}
