package com.example.my_api_server.service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderCreateDto(
        Long memberId,
        List<Long> productId,
        List<Long> count,
        LocalDateTime orderTime //주문 시간
) {

    public OrderCreateDto {
        if (orderTime == null) {
            orderTime = LocalDateTime.now();
        }
    }

    public OrderCreateDto(Long memberId, List<Long> productId, List<Long> count) {
        this(memberId, productId, count, LocalDateTime.now());
    }
}
