package com.example.my_api_server.service.dto;

import com.example.my_api_server.entity.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ProductCreateDto {

    private String productName;

    private String productNumber;

    private ProductType productType;

    private Long price;

    private Long stock;
}
