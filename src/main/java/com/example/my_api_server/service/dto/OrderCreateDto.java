package com.example.my_api_server.service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderCreateDto(
        Long memberId,
        List<Long> productId,
        List<Long> count,
        LocalDateTime orderTime
) {
    public OrderCreateDto {
        if (orderTime == null) {
            orderTime = LocalDateTime.now();
        }
    }

    // 기존에 사용하던 생성자에서 LocalDateTime.now()를 추가
    public OrderCreateDto(Long memberId, List<Long> productId, List<Long> count) {
        this(memberId, productId, count, LocalDateTime.now());
    }
}
