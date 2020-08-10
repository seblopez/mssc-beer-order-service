package guru.springframework.beer.order.service.services;

import guru.springframework.beer.order.service.domain.BeerOrder;
import guru.springframework.beer.order.service.web.model.BeerOrderDto;

import java.util.UUID;

public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder order);

    void processBeerOrderValidation(UUID beerOrderId, Boolean isValid);

    void processBeerOrderAllocation(BeerOrderDto order, Boolean orderAllocated, Boolean error);

    void processBeerOrderPickUp(UUID beerOrderId);

}
