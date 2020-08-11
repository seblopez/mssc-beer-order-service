package guru.springframework.beer.order.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.ManagedWireMockServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import guru.springframework.beer.order.service.client.BeerClientRestTemplateImpl;
import guru.springframework.beer.order.service.client.model.BeerDto;
import guru.springframework.beer.order.service.config.JmsConfig;
import guru.springframework.beer.order.service.domain.BeerOrder;
import guru.springframework.beer.order.service.domain.BeerOrderLine;
import guru.springframework.beer.order.service.domain.BeerOrderStatus;
import guru.springframework.beer.order.service.domain.Customer;
import guru.springframework.beer.order.service.repositories.BeerOrderRepository;
import guru.springframework.beer.order.service.repositories.CustomerRepository;
import guru.springframework.beer.order.service.statemachine.event.AllocationFailureEvent;
import guru.springframework.beer.order.service.statemachine.event.DeallocateOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsTemplate;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@ExtendWith(WireMockExtension.class)
@SpringBootTest
class BeerOrderManagerImplIT {
    @Autowired
    private BeerOrderManager beerOrderManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BeerOrderRepository beerOrderRepository;

    @Autowired
    WireMockServer wireMockServer;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JmsTemplate jmsTemplate;

    private Customer testCustomer;

    UUID beerId = UUID.randomUUID();

    private static final String UPC = "1234567890123";

    private final BeerDto beerDto = BeerDto.builder()
            .id(beerId)
            .beerName("Antares")
            .upc(UPC)
            .beerStyle("IPA")
            .build();

    @TestConfiguration
    static class RestTemplateBuilderProvider {

        @Bean(destroyMethod = "stop")
        public WireMockServer wireMockServer() {
            final ManagedWireMockServer wireMockServer = with(wireMockConfig().port(8084));
            wireMockServer.start();
            return wireMockServer;
        }

    }

    @BeforeEach
    void setUp() {
        testCustomer = customerRepository.save(Customer.builder()
                .customerName("La Covacha S.A.")
                .apiKey(UUID.randomUUID())
                .build());
    }

    @Test
    void beerOrderAllocatedOk() throws JsonProcessingException {
        wireMockServer.stubFor(get(BeerClientRestTemplateImpl.BEER_UPC_API_PATH + UPC)
            .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        final BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(createBeerOrder());

        await().untilAsserted(() -> {
            final BeerOrder beerOrderFound = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatus.ALLOCATED, beerOrderFound.getOrderStatus());
        });

        await().untilAsserted(() -> {
            final BeerOrder beerOrderFound = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            final BeerOrderLine orderLine = beerOrderFound.getBeerOrderLines().iterator().next();
            assertEquals(orderLine.getOrderQuantity(), orderLine.getQuantityAllocated());
        });

        final BeerOrder retrievedBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertNotNull(retrievedBeerOrder);
        assertEquals(BeerOrderStatus.ALLOCATED, retrievedBeerOrder.getOrderStatus());
        retrievedBeerOrder.getBeerOrderLines()
                .forEach(beerOrderLine -> assertEquals(beerOrderLine.getOrderQuantity(), beerOrderLine.getQuantityAllocated()));

    }

    @Test
    void beerOrderValidationFailed() throws JsonProcessingException {
        wireMockServer.stubFor(get(BeerClientRestTemplateImpl.BEER_UPC_API_PATH + UPC)
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        final BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef("fail-validation");

        final BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            final BeerOrder beerOrderFound = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatus.VALIDATION_EXCEPTION, beerOrderFound.getOrderStatus());
        });


        final BeerOrder beerOrderWithException = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertNotNull(beerOrderWithException);
        assertEquals(BeerOrderStatus.VALIDATION_EXCEPTION, beerOrderWithException.getOrderStatus());

    }

    @Test
    void beerOrderAllocationFailed() throws JsonProcessingException {
        wireMockServer.stubFor(get(BeerClientRestTemplateImpl.BEER_UPC_API_PATH + UPC)
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        final BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef("allocation-failed");

        final BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            final BeerOrder beerOrderFound = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatus.ALLOCATION_EXCEPTION, beerOrderFound.getOrderStatus());
        });

        final BeerOrder beerOrderWithException = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        final AllocationFailureEvent allocationFailureEvent = (AllocationFailureEvent) jmsTemplate.receiveAndConvert(JmsConfig.ALLOCATE_ORDER_FAILURE_QUEUE);

        assertNotNull(beerOrderWithException);
        assertEquals(BeerOrderStatus.ALLOCATION_EXCEPTION, beerOrderWithException.getOrderStatus());
        assertNotNull(allocationFailureEvent);
        assertEquals(beerOrderWithException.getId(), allocationFailureEvent.getOrderId());

    }

    @Test
    void beerOrderCancelWhileValidationPendingOk() throws JsonProcessingException {
        wireMockServer.stubFor(get(BeerClientRestTemplateImpl.BEER_UPC_API_PATH + UPC)
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        final BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef("cancel-order-validation");

        final BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            final BeerOrder beerOrderFound = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatus.VALIDATION_PENDING, beerOrderFound.getOrderStatus());
        });

        beerOrderManager.processBeerOrderCancellation(savedBeerOrder.getId());

        await().untilAsserted(() -> {
            final BeerOrder beerOrderFound = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatus.CANCELLED, beerOrderFound.getOrderStatus());
        });

        final BeerOrder cancelledBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertNotNull(cancelledBeerOrder);
        assertEquals(BeerOrderStatus.CANCELLED, cancelledBeerOrder.getOrderStatus());

    }

    @Test
    void beerOrderCancelAllocatedOk() throws JsonProcessingException {
        wireMockServer.stubFor(get(BeerClientRestTemplateImpl.BEER_UPC_API_PATH + UPC)
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        final BeerOrder beerOrder = createBeerOrder();

        final BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            final BeerOrder beerOrderFound = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatus.ALLOCATED, beerOrderFound.getOrderStatus());
        });

        beerOrderManager.processBeerOrderCancellation(savedBeerOrder.getId());

        await().untilAsserted(() -> {
            final BeerOrder beerOrderFound = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatus.CANCELLED, beerOrderFound.getOrderStatus());
        });

        final BeerOrder cancelledBeerOrder = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        final DeallocateOrderRequest deallocateOrderRequest = (DeallocateOrderRequest) jmsTemplate.receiveAndConvert(JmsConfig.DEALLOCATE_ORDER_REQUEST_QUEUE);

        assertNotNull(cancelledBeerOrder);
        assertEquals(BeerOrderStatus.CANCELLED, cancelledBeerOrder.getOrderStatus());
        assertNotNull(deallocateOrderRequest);
        assertEquals(cancelledBeerOrder.getId(), deallocateOrderRequest.getBeerOrderDto().getId());

    }

    @Test
    void beerOrderAllocationHasPendingInventory() throws JsonProcessingException {
        wireMockServer.stubFor(get(BeerClientRestTemplateImpl.BEER_UPC_API_PATH + UPC)
                .willReturn(okJson(objectMapper.writeValueAsString(beerDto))));

        final BeerOrder beerOrder = createBeerOrder();
        beerOrder.setCustomerRef("pending-inventory");

        final BeerOrder savedBeerOrder = beerOrderManager.newBeerOrder(beerOrder);

        await().untilAsserted(() -> {
            final BeerOrder beerOrderFound = beerOrderRepository.findById(savedBeerOrder.getId()).get();
            assertEquals(BeerOrderStatus.PENDING_INVENTORY, beerOrderFound.getOrderStatus());
        });

        final BeerOrder beerOrderWithException = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        assertNotNull(beerOrderWithException);
        assertEquals(BeerOrderStatus.PENDING_INVENTORY, beerOrderWithException.getOrderStatus());

    }

    @Test
    void beerOrderPickedUpOk() {
        final BeerOrder beerOrderToSave = createBeerOrder();
        beerOrderToSave.setOrderStatus(BeerOrderStatus.ALLOCATED);

        final BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrderToSave);

        final BeerOrder beerOrderFound = beerOrderRepository.findById(savedBeerOrder.getId()).get();

        beerOrderManager.processBeerOrderPickUp(beerOrderFound.getId());

        await().untilAsserted(() -> {
            final BeerOrder beerOrderPickedUp = beerOrderRepository.findById(beerOrderFound.getId()).get();
            assertEquals(BeerOrderStatus.PICKED_UP, beerOrderPickedUp.getOrderStatus());
        });

        final BeerOrder beerOrderPickedUp = beerOrderRepository.findById(beerOrderFound.getId()).get();

        assertNotNull(beerOrderPickedUp);
        assertEquals(BeerOrderStatus.PICKED_UP, beerOrderPickedUp.getOrderStatus());

    }

    private BeerOrder createBeerOrder() {
        BeerOrder beerOrder = BeerOrder.builder()
                .customer(testCustomer)
                .build();

        Set<BeerOrderLine> lines = new HashSet<>();
        lines.add(BeerOrderLine.builder()
                        .beerId(beerId)
                        .upc("1234567890123")
                        .orderQuantity(10)
                        .beerOrder(beerOrder)
                        .build());

        beerOrder.setBeerOrderLines(lines);

        return beerOrder;

    }


}
