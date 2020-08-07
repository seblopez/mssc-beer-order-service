package guru.springframework.beer.order.service.services;

import guru.springframework.beer.order.service.domain.BeerOrder;
import guru.springframework.beer.order.service.domain.BeerOrderLine;
import guru.springframework.beer.order.service.domain.BeerOrderStatus;
import guru.springframework.beer.order.service.domain.Customer;
import guru.springframework.beer.order.service.repositories.BeerOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SpringBootTest
class BeerOrderManagerImplTest {

    @Autowired
    private BeerOrderManager beerOrderManager;

    @Autowired
    private BeerOrderRepository beerOrderRepository;

    private BeerOrder order;

    @BeforeEach
    void setUp() {
        order = BeerOrder.builder()
                .customer(Customer.builder()
                        .customerName("La Covacha S.A.")
                        .build())
                .beerOrderLines(Set.of(BeerOrderLine.builder()
                        .beerId(UUID.randomUUID())
                        .orderQuantity(200)
                        .upc("0121122232323")
                        .build()))
                .build();
    }

    @Transactional
    @Test
    void newBeerOrder() {
        final BeerOrder beerOrder = beerOrderManager.newBeerOrder(order);

        assertNotNull(beerOrder);
        assertEquals(BeerOrderStatus.NEW, beerOrder.getOrderStatus());

    }
}
