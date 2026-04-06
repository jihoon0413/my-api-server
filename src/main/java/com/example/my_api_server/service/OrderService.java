package com.example.my_api_server.service;

import com.example.my_api_server.entity.Member;
import com.example.my_api_server.entity.Order;
import com.example.my_api_server.entity.OrderProduct;
import com.example.my_api_server.entity.OrderStatus;
import com.example.my_api_server.entity.Product;
import com.example.my_api_server.repo.MemberDBRepo;
import com.example.my_api_server.repo.OrderRepo;
import com.example.my_api_server.repo.ProductRepo;
import com.example.my_api_server.service.dto.OrderCreateDto;
import com.example.my_api_server.service.dto.OrderResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepo orderRepo;
    private final MemberDBRepo memberRepo;
    private final ProductRepo productRepo;

    //주문 생성
    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto dto) {

        Member member = memberRepo.findById(dto.memberId()).orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        Order order = Order.createOrder(member, LocalDateTime.now());
        List<Product> products = productRepo.findAllById(dto.productId());

        List<OrderProduct> orderProducts = IntStream.range(0, dto.count().size())
                .mapToObj(idx -> {

                    Product product = products.get(idx);
                    Long orderCount = dto.count().get(idx);

                    product.buyProductWithStock(orderCount);

                    return order.createOrderProduct(orderCount, product);
                })
                .toList();

        order.addOrderProducts(orderProducts);

        Order savedOrder = orderRepo.save(order);

        return OrderResponseDto.of(
                savedOrder.getOrderTime(),
                OrderStatus.COMPLETED,
                true);

    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(includes = ObjectOptimisticLockingFailureException.class, maxRetries = 3) // 이 에러가 발생했을 때 재시도 3번
    public OrderResponseDto createOrderOptLock(OrderCreateDto dto) {
        log.info("@Retryable 테스트");
        Member member = memberRepo.findById(dto.memberId()).orElseThrow();

        LocalDateTime orderTime = LocalDateTime.now();

        Order order = Order.builder()
                .buyer(member)
                .orderStatus(OrderStatus.PENDING)
                .orderTime(orderTime)
                .build();

        List<Product> products = productRepo.findAllById(dto.productId());

        List<OrderProduct> orderProducts = IntStream.range(0, dto.count().size())
                .mapToObj(idx -> {

                    Product product = products.get(idx);

                    if (product.getStock() - dto.count().get(idx) < 0) {
                        throw new RuntimeException("재고가 부족하여 주문할 수 없습니다!");
                    }
                    product.decreaseStock(dto.count().get(idx));

                    return OrderProduct.builder()
                            .order(order)
                            .number(dto.count().get(idx))
                            .product(products.get(idx))
                            .build();
                })
                .toList();

        order.addOrderProducts(orderProducts);

        Order savedOrder = orderRepo.save(order);

        OrderResponseDto orderResponseDto = OrderResponseDto.of(
                savedOrder.getOrderTime(),
                OrderStatus.COMPLETED,
                true);

        return orderResponseDto;
    }


    @Transactional
    public OrderResponseDto createOrderPLock(OrderCreateDto dto) {

        Member member = memberRepo.findById(dto.memberId()).orElseThrow();

        LocalDateTime orderTime = LocalDateTime.now();

        Order order = Order.builder()
                .buyer(member)
                .orderStatus(OrderStatus.PENDING)
                .orderTime(orderTime)
                .build();

        List<Product> products = productRepo.findAllByIdsWithXLock(dto.productId());

        List<OrderProduct> orderProducts = IntStream.range(0, dto.count().size())
                .mapToObj(idx -> {

                    Product product = products.get(idx);

                    if (product.getStock() - dto.count().get(idx) < 0) {
                        throw new RuntimeException("재고가 부족하여 주문할 수 없습니다!");
                    }
                    product.decreaseStock(dto.count().get(idx));

                    return OrderProduct.builder()
                            .order(order)
                            .number(dto.count().get(idx))
                            .product(products.get(idx))
                            .build();
                })
                .toList();

        order.addOrderProducts(orderProducts);

        Order savedOrder = orderRepo.save(order);

        OrderResponseDto orderResponseDto = OrderResponseDto.of(
                savedOrder.getOrderTime(),
                OrderStatus.COMPLETED,
                true);

        return orderResponseDto;
    }

    //주문 조회

    /*
     * JPA는 내부적으로 캐시(중간 지점의 미니 창고) 매커니즘
     * -내부에 1차캐시, 2차 캐시
     * -1차캐시 내부적으로 영속화(내 자식으로 만들어 관리하겠다.) .save()
     * readOnly = true --> 내부 하이버네이트 동작원리가 간소화된다.(더티 체킹(자동 변경) X)
     * 조회하는 곳에서 주로 최적화 할 때 사용
     */
    @Transactional(readOnly = true)
    public OrderResponseDto findOrder(Long orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow();

        OrderResponseDto orderResponseDto = OrderResponseDto.of(
                order.getOrderTime(),
                order.getOrderStatus(),
                true);

        return orderResponseDto;
    }

    @Transactional
    public OrderResponseDto confirmOrder(Long orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow();

        order.changeOrderStatus(OrderStatus.COMPLETED);

        return OrderResponseDto.builder()
                .orderCompletedTime(order.getOrderTime())
                .orderStatus(order.getOrderStatus())
                .isSuccess(true)
                .build();
    }
}
