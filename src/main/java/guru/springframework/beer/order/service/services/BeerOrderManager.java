package guru.springframework.beer.order.service.services;

import guru.springframework.beer.order.service.domain.BeerOrder;

public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder order);

    void validateBeerOrder(BeerOrder order);

}
