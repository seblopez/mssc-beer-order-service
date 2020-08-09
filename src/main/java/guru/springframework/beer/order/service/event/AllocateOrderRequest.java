package guru.springframework.beer.order.service.event;

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
public class AllocateOrderRequest implements Serializable {
    private static final long serialVersionUID = -2302508160775761564L
            ;
    private BeerOrderDto beerOrderDto;
}
