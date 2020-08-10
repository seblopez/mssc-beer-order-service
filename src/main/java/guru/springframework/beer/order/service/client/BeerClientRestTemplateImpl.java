package guru.springframework.beer.order.service.client;

import guru.springframework.beer.order.service.client.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@ConfigurationProperties(prefix = "sfg.brewery", ignoreUnknownFields = true)
@Component
public class BeerClientRestTemplateImpl implements BeerClient {
    public static final String BEER_ID_API_PATH = "/api/v1/beer/";
    public static final String BEER_UPC_API_PATH = "/api/v1/beerUpc/";
    private final RestTemplate restTemplate;
    private String beerServiceHost;

    public void setBeerServiceHost(String beerServiceHost) {
        this.beerServiceHost = beerServiceHost;
    }

    public BeerClientRestTemplateImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public Optional<BeerDto> getBeerById(UUID beerId) {
        log.debug("Calling Beer Client operation getBeerById");
        return Optional.of(restTemplate
                .getForObject(beerServiceHost + BEER_ID_API_PATH + beerId.toString(), BeerDto.class));

    }

    @Override
    public Optional<BeerDto> getBeerByUpc(String upc) {
        log.debug("Calling Beer Client operation getBeerByUpc");
        return Optional.of(restTemplate
                .getForObject(beerServiceHost + BEER_UPC_API_PATH + upc, BeerDto.class));
    }
}
