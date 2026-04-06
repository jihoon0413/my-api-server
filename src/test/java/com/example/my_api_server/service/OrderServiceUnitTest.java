package com.example.my_api_server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.my_api_server.entity.Member;
import com.example.my_api_server.entity.Order;
import com.example.my_api_server.entity.OrderStatus;
import com.example.my_api_server.entity.Product;
import com.example.my_api_server.entity.ProductType;
import com.example.my_api_server.repo.MemberDBRepo;
import com.example.my_api_server.repo.OrderRepo;
import com.example.my_api_server.repo.ProductRepo;
import com.example.my_api_server.service.dto.OrderCreateDto;
import com.example.my_api_server.service.dto.OrderResponseDto;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class) // Mockito 활성화
class OrderServiceUnitTest {

    @Mock
    ProductRepo productRepo;

    @Mock
    MemberDBRepo memberDBRepo;

    @Mock // 가짜 객체
    OrderRepo orderRepo;

    // 주입할 애들

    @InjectMocks // 테스트 할 실제 대상 클래스(Mock 개체를 자동으로 주입받는다.)
    OrderService orderService;

    InitData initData;

    OrderCreateDto orderCreateDto;

    @BeforeEach
    public void init() {
        initData = new InitData();

        initData.memberId = 1L;
        initData.productIds = List.of(1L, 2L);
        initData.counts = List.of(1L, 2L);

        initData.product1 = Product.builder()
                .productNumber("TEST1")
                .productName("티셔츠1")
                .price(1000L)
                .stock(2L)
                .productType(ProductType.CLOTHES)
                .build();

        initData.product2 = Product.builder()
                .productNumber("TEST2")
                .productName("티셔츠2")
                .price(2000L)
                .stock(4L)
                .productType(ProductType.CLOTHES)
                .build();

        initData.member = Member.builder()
                .email("test1@gmail.com")
                .password("1234")
                .build();

        orderCreateDto = new OrderCreateDto(initData.memberId, initData.productIds, initData.counts);
    }


    @Test
    @DisplayName("주문 요청이 정상적으로 잘 등록된다.")
    public void createOrderSuccess() {
        //given

        when(productRepo.findAllById(initData.productIds)).thenReturn(List.of(initData.product1, initData.product2));
        when(memberDBRepo.findById(initData.memberId)).thenReturn(Optional.of(initData.member));
        when(orderRepo.save(any())).thenAnswer(invocation ->
                invocation.getArgument(0));

        //when(테스트할 메서드)
        OrderResponseDto dto = orderService.createOrder(orderCreateDto);

        //then(값 검증)
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepo).save(captor.capture()); // orderRepo save() 가 호풀되었는지 확인

        assertThat(dto.isSuccess()).isTrue();
        assertThat(dto.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED); // 주문 상태 검증
    }

    @Test
    @DisplayName("[Exception]주문 요청 시 재고 부족하면 예외 처리가 정상 동작한다.")
    public void productStockValid() {
        //given
        Long memberId = 1L;
        List<Long> productIds = List.of(1L, 2L);
        List<Long> counts = List.of(10L, 20L);

        Product product1 = Product.builder()
                .productNumber("TEST1")
                .productName("티셔츠 1")
                .productType(ProductType.CLOTHES)
                .price(1000L)
                .stock(1L)
                .build();

        Product product2 = Product.builder()
                .productNumber("TEST2")
                .productName("티셔츠 2")
                .productType(ProductType.CLOTHES)
                .price(2000L)
                .stock(2L)
                .build();

        Member member = Member.builder()
                .email("test1@gmail.com")
                .password("1234")
                .build();

        OrderCreateDto createDto = new OrderCreateDto(memberId, productIds, counts);

        when(productRepo.findAllById(productIds)).thenReturn(List.of(product1, product2));
        when(memberDBRepo.findById(memberId)).thenReturn(Optional.of(member));

        //when(테스트할 메서드)

        //then(값 검증)
        assertThatThrownBy(() -> orderService.createOrder(createDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("재고가 부족하여 주문할 수 없습니다!");
    }

    //    @Test
    @DisplayName("[Exception]주문 시간 날짜 오류 테스트")
    public void orderTimeException() {
        //given

        when(productRepo.findAllById(initData.productIds)).thenReturn(List.of(initData.product1, initData.product2));
        when(memberDBRepo.findById(initData.memberId)).thenReturn(Optional.of(initData.member));

        //when(테스트할 메서드)
        OrderResponseDto order = orderService.createOrder(orderCreateDto);

        //then(값 검증)
        assertThat(order).isNotNull();


    }

    //테스트용 초기 클래스
    public class InitData {
        public Long memberId;
        public List<Long> productIds;
        public List<Long> counts;

        public Product product1;
        public Product product2;
        public Member member;
    }
}