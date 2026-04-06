package com.example.my_api_server.service;

import com.example.my_api_server.entity.Product;
import com.example.my_api_server.repo.ProductRepo;
import com.example.my_api_server.service.dto.ProductCreateDto;
import com.example.my_api_server.service.dto.ProductResDto;
import com.example.my_api_server.service.dto.ProductUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepo productRepo;

    //상품 등록
    //하이버네이트는 DB랑 통신하기위해, DB의 ACID가 되기 위해서 begin tran: commit 무조건 되어야한다.
    @Transactional
    public ProductResDto createProduct(ProductCreateDto dto) {
        Product product = Product.builder()
                .productName(dto.getProductName())
                .productType(dto.getProductType())
                .productNumber(dto.getProductNumber())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .build();

        Product savedProduct = productRepo.save(product);

        ProductResDto resDto = ProductResDto.builder()
                .productNumber(savedProduct.getProductNumber())
                .stock(savedProduct.getStock())
                .price(savedProduct.getPrice())
                .build();

        return resDto;
    }

    //상품 조회, Transactional X
    public ProductResDto findProduct(Long productId) {
        // DB에서 조회한거를 바탕으로 조회해서 영속성 컨텍스트에 저장하고(1차 캐시 캐싱) 그 값을리턴해준다.
        Product product = productRepo.findById(productId).orElseThrow();

        ProductResDto resDto = ProductResDto.builder()
                .productNumber(product.getProductNumber())
                .stock(product.getStock())
                .price(product.getPrice())
                .build();

        return resDto;
    }

    //상품 수정
    @Transactional
    public ProductResDto updateProduct(ProductUpdateDto dto) {
        Product product = productRepo.findById(dto.productId()).orElseThrow();

        product.changeProductName(dto.changeProductName());
        product.increaseStock(dto.changeStock());

        ProductResDto resDto = ProductResDto.builder()
                .productNumber(product.getProductNumber())
                .stock(product.getStock())
                .price(product.getPrice())
                .build();

        return resDto;
    }
}
