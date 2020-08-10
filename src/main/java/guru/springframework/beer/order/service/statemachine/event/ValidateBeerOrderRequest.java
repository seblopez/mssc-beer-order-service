package guru.springframework.beer.order.service.statemachine.event;

import guru.springframework.beer.order.service.web.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ValidateBeerOrderRequest implements Serializable {
    private static final long serialVersionUID = -4177171501608081404L;

    private BeerOrderDto beerOrderDto;
}
