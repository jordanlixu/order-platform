package org.example.order.order.dto;


import lombok.Data;

@Data
public class OrderRequest {
    private String userId;
    private String productId;
    private Integer amount;
}
