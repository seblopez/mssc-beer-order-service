package guru.springframework.beer.order.service.event;

import guru.springframework.beer.order.service.web.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ValidateBeerOrderRequest {
    private BeerOrderDto beerOrderDto;
}
