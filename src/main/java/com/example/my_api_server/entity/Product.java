package com.example.my_api_server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@Getter
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;

    private String productNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType productType;

    private Long price;

    private Long stock;

    @Version
    private Long version;

    public void changeProductName(String changeProductName) {
        this.productName = changeProductName;
    }

    public void increaseStock(Long addStock) {
        this.stock += addStock;
    }

    public void decreaseStock(Long subStock) {
        this.stock -= subStock;
    }

    public void buyProductWithStock(Long orderCount) {
        if (this.getStock() - orderCount < 0) {
            throw new RuntimeException("재고가 부족하여 주문할 수 없습니다!");
        }
        this.decreaseStock(orderCount);
    }

}
