package guru.springframework.beer.order.service.web.mappers;


import guru.springframework.beer.order.service.client.BeerClient;
import guru.springframework.beer.order.service.client.model.BeerDto;
import guru.springframework.beer.order.service.domain.BeerOrderLine;
import guru.springframework.beer.order.service.web.model.BeerOrderLineDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Slf4j
public abstract class BeerOrderLineMapperDecorator implements BeerOrderLineMapper {
    private BeerClient beerClient;
    private BeerOrderLineMapper beerOrderLineMapper;

    @Autowired
    public void setBeerClient(BeerClient beerClient) {
        this.beerClient = beerClient;
    }

    @Autowired
    public void setBeerOrderLineMapper(BeerOrderLineMapper beerOrderLineMapper) {
        this.beerOrderLineMapper = beerOrderLineMapper;
    }

    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        BeerOrderLineDto beerOrderLineDto = beerOrderLineMapper.beerOrderLineToDto(line);
        final String upc = line.getUpc();
        if(upc != null) {
            final Optional<BeerDto> optionalBeerDto = beerClient.getBeerByUpc(upc);
            optionalBeerDto.ifPresent(beerDto -> {
                beerOrderLineDto.setBeerName(beerDto.getBeerName());
                beerOrderLineDto.setBeerStyle(beerDto.getBeerStyle());
                beerOrderLineDto.setBeerId(beerDto.getId());
            });
        }

        return beerOrderLineDto;

    }




}

