package guru.springframework.beer.order.service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jenspiegsa.wiremockextension.ManagedWireMockServer;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import guru.springframework.beer.order.service.client.BeerClientRestTemplateImpl;
import guru.springframework.beer.order.service.client.model.BeerDto;
import guru.springframework.beer.order.service.domain.BeerOrder;
import guru.springframework.beer.order.service.domain.BeerOrderLine;
import guru.springframework.beer.order.service.domain.BeerOrderStatus;
import guru.springframework.beer.order.service.domain.Customer;
import guru.springframework.beer.order.service.repositories.BeerOrderRepository;
import guru.springframework.beer.order.service.repositories.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

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

    private Customer testCustomer;

    UUID beerId = UUID.randomUUID();

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
    void beerOrderAllocatedOk() throws JsonProcessingException, InterruptedException {
        final String upc = "1234567890123";
        final BeerDto beerDto = BeerDto.builder()
                .id(beerId)
                .beerName("Antares")
                .upc(upc)
                .beerStyle("IPA")
                .build();

        wireMockServer.stubFor(get(BeerClientRestTemplateImpl.BEER_UPC_API_PATH + upc)
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

    private BeerOrder createBeerOrder() {
        BeerOrder beerOrder = BeerOrder.builder()
                .customer(testCustomer)
                .build();

        Set<BeerOrderLine> lines = new HashSet<>();
        lines.add(BeerOrderLine.builder()
                        .beerId(beerId)
                        .upc("1234567890123")
                        .orderQuantity(1)
                        .beerOrder(beerOrder)
                        .build());

        beerOrder.setBeerOrderLines(lines);

        return beerOrder;

    }


}
