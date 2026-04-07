package com.example.my_api_server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.my_api_server.common.MemberFixture;
import com.example.my_api_server.common.ProductFixture;
import com.example.my_api_server.config.TestContainerConfig;
import com.example.my_api_server.entity.Member;
import com.example.my_api_server.entity.OrderStatus;
import com.example.my_api_server.entity.Product;
import com.example.my_api_server.repo.MemberDBRepo;
import com.example.my_api_server.repo.OrderProductRepo;
import com.example.my_api_server.repo.OrderRepo;
import com.example.my_api_server.repo.ProductRepo;
import com.example.my_api_server.service.dto.OrderCreateDto;
import com.example.my_api_server.service.dto.OrderResponseDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest //Sping DI를 통해 빈(Bean)주입 해주는 어노테이션
@Import(TestContainerConfig.class)
@ActiveProfiles("test") // application-test.yaml 값을 읽는다!
public class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private MemberDBRepo memberDBRepo;

    @Autowired
    private OrderProductRepo orderProductRepo;

    private static List<Long> getProductIds(List<Product> products) {
        return products.stream()
                .map(Product::getId)
                .toList();
    }

    @BeforeEach
    public void setup() {
        orderProductRepo.deleteAllInBatch();
        productRepo.deleteAllInBatch();
        orderRepo.deleteAllInBatch();
        memberDBRepo.deleteAllInBatch();
    }

    private Member getSavedMember(String password) {

        return memberDBRepo.save(MemberFixture
                .defaultMember()
                .password(password)
                .build());
    }

    private List<Product> getProducts() {
        return productRepo.saveAll(ProductFixture.defaultProducts());
    }

    @Nested()
    @DisplayName("주문 생성 TC")
    class OrderCreateTest {

        @Test
        @DisplayName("주문 생성 시 DB에 저장되고 주문시간이 Null이 아니다.")
        public void createOrderPersistAndReturn() {
            //given
            List<Long> counts = List.of(1L, 1L);

            Member savedMember = getSavedMember("1234");
            List<Product> products = getProducts();
            List<Long> productIds = getProductIds(products);
            OrderCreateDto createDto = new OrderCreateDto(savedMember.getId(), productIds, counts);

            //when
            OrderResponseDto retDto = orderService.createOrder(createDto);

            //then
            assertThat(retDto.getOrderCompletedTime()).isNotNull();
        }

        @Test
        @DisplayName("주문 생성 시 재고가 정상적으로 차감이 된다.")
        public void createOrderStockDecreaseSuccess() {
            //given
            List<Long> counts = List.of(1L, 1L);

            Member savedMember = getSavedMember("1234");
            List<Product> products = getProducts();
            List<Long> productIds = getProductIds(products);
            OrderCreateDto createDto = new OrderCreateDto(savedMember.getId(), productIds, counts);

            //when
            OrderResponseDto retDto = orderService.createOrder(createDto);

            //then
            List<Product> resultProducts = productRepo.findAllById(productIds);
            for (int i = 0; i < products.size(); i++) {
                Product beforeProduct = products.get(i);
                Product nowProduct = resultProducts.get(i);
                Long orderStock = counts.get(i);

                assertThat(beforeProduct.getStock() - orderStock).isEqualTo(nowProduct.getStock());
            }

        }

        @Test
        @DisplayName("주문 생성 시 재고가 부족하면 예외가 정상 동작한다.")
        public void createOrderStockValidation() {
            //given
            List<Long> counts = List.of(10L, 10L);
            Member savedMember = getSavedMember("1234");
            List<Product> products = getProducts();
            List<Long> productIds = getProductIds(products);
            OrderCreateDto createDto = new OrderCreateDto(savedMember.getId(), productIds, counts);

            //when

            //then
            assertThatThrownBy(() -> orderService.createOrder(createDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("재고가 부족하여 주문할 수 없습니다!");

        }

        @Test
        @DisplayName("주문 생성 시 상품 개수")
        public void createOrderCheckProductCount() {

            List<Long> counts = List.of(1L, 2L);
            Member savedMember = getSavedMember("1234");
            List<Product> products = getProducts();
            List<Long> productIds = getProductIds(products);
            OrderCreateDto createDto = new OrderCreateDto(savedMember.getId(), productIds, counts);

            List<Product> beforeProduct = productRepo.findAll();
            OrderResponseDto retDto = orderService.createOrder(createDto);
            List<Product> nowProduct = productRepo.findAll();

            assertThat(retDto.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
            assertThat(beforeProduct.size()).isEqualTo(nowProduct.size());

        }


        /*
        1. 주문 생성 시 3개가 잘 insert 되는지 확인
        2. 조회시 phantom Read가 잘 방지되는지 확인한다
            - 해당 코드가 phantom Read를 보장해줘야하는지에 따라 달
         */
    }

    @Nested()
    @DisplayName("주문과 연관된 도메인 에외 TC")
    class OrderRelatedExceptionTest {

        @Test
        @DisplayName("주문 시 회원이 존재하지 않으면 예외가 발생한다.")
        public void validateMemberWhenCreateOrder() {
            //given
            List<Long> counts = List.of(1L, 1L);
            Member savedMember = getSavedMember("1234"); //멤버 저장
            List<Product> products = getProducts(); //상품 저장
            List<Long> productIds = getProductIds(products); //productId 추출 작업

            OrderCreateDto createDto = new OrderCreateDto(1234L, productIds, counts);

            //when, then
            assertThatThrownBy(() -> orderService.createOrder(createDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("회원이 존재하지 않습니다.");
        }

        @Test
        @DisplayName("존재하지 않는 상품에 대한 예외가 발생한다.")
        public void validateProductWhenCreateOrder() {
            List<Long> counts = List.of(1L, 2L);
            Member savedMember = getSavedMember("1234");
            List<Product> products = getProducts();
            List<Long> productIds = List.of(1L, 3L);

            OrderCreateDto createDto = new OrderCreateDto(savedMember.getId(), productIds, counts);

            assertThatThrownBy(() -> orderService.createOrder(createDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("존재하지 않은 상품이 포함되어 있습니다.");

        }


    }
}
