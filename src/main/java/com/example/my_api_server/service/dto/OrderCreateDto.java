package com.example.my_api_server.service.dto;

import java.util.List;

public record OrderCreateDto(
        Long memberId,
        List<Long> productId,
        List<Long> count
) {
}
