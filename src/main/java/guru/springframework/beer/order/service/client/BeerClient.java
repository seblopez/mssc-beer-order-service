package guru.springframework.beer.order.service.client;

import guru.springframework.beer.order.service.client.model.BeerDto;

import java.util.Optional;
import java.util.UUID;

public interface BeerClient {

    Optional<BeerDto> getBeerById(UUID beerId);

    Optional<BeerDto> getBeerByUpc(String upc);

}
