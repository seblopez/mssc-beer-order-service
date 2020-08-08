package guru.springframework.beer.order.service.services;

import guru.springframework.beer.order.service.domain.BeerOrder;

import java.util.UUID;

public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder order);

    void processBeerOrderValidation(UUID beerOrderId, Boolean isValid);

}
