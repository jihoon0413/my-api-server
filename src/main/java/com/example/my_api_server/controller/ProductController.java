package com.example.my_api_server.controller;

import com.example.my_api_server.service.ProductService;
import com.example.my_api_server.service.dto.ProductCreateDto;
import com.example.my_api_server.service.dto.ProductResDto;
import com.example.my_api_server.service.dto.ProductUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/v1/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ProductResDto createProduct(@Validated @RequestBody ProductCreateDto dto) {
        return productService.createProduct(dto);
    }

    @GetMapping("/{id}")
    public ProductResDto findProduct(@PathVariable Long id) {
        return productService.findProduct(id);
    }


    // patchMapping -> 일부분을 수정하겠다.
    // putMapping -> 전체를 수정
    @PatchMapping
    public ProductResDto updateProduct(@Validated @RequestBody ProductUpdateDto dto) {
        return productService.updateProduct(dto);
    }

}
