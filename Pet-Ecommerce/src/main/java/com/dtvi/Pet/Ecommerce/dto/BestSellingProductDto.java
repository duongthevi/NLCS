package com.dtvi.Pet.Ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BestSellingProductDto {
    private Long productId;
    private String productName;
    private Long totalQuantity;
}