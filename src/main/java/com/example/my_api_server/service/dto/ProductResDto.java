package com.example.my_api_server.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ProductResDto {
    private String productNumber;

    private Long price;

    private Long stock;
}
