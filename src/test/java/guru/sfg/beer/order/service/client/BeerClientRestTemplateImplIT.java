package guru.sfg.beer.order.service.client;

import guru.sfg.beer.order.service.client.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// @Disabled
@Slf4j
@SpringBootTest
class BeerClientRestTemplateImplIT {

    @Autowired
    BeerClient beerClient;

    @Test
    void getBeerByIdOk() {
        final Optional<BeerDto> optionalBeerDto = beerClient.getBeerById(UUID.fromString("0a818933-087d-47f2-ad83-2f986ed087eb"));

        assertTrue(optionalBeerDto.isPresent());
        assertEquals("Mango Bobs", optionalBeerDto.get().getBeerName());
    }

    @Test
    void getBeerByUpcOk() {
        final Optional<BeerDto> optionalBeerDto = beerClient.getBeerByUpc("0631234200036");

        assertTrue(optionalBeerDto.isPresent());
        assertEquals("Mango Bobs", optionalBeerDto.get().getBeerName());
    }
}
