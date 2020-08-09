package guru.springframework.beer.order.service.event;

import guru.springframework.beer.order.service.web.model.BaseItem;
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
public class AllocateOrderResponse extends BaseItem implements Serializable {
    private static final long serialVersionUID = 4107089141124025785L;

    private BeerOrderDto beerOrderDto;
    private Boolean allocationError;
    private Boolean pendingInventory;

}
