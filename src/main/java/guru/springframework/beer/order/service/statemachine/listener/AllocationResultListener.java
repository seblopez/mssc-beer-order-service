package guru.springframework.beer.order.service.statemachine.listener;

import guru.springframework.beer.order.service.config.JmsConfig;
import guru.springframework.beer.order.service.services.BeerOrderManager;
import guru.springframework.beer.order.service.statemachine.event.AllocateOrderResponse;
import guru.springframework.beer.order.service.web.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class AllocationResultListener {
    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void listen(AllocateOrderResponse allocateOrderResponse) {
        final BeerOrderDto beerOrderDto = allocateOrderResponse.getBeerOrderDto();
        final UUID orderId = beerOrderDto.getId();
        log.debug(MessageFormat.format("Getting response for Order Allocation of order Id {0}", orderId.toString()));
        beerOrderManager.processBeerOrderAllocation(beerOrderDto, allocateOrderResponse.getPendingInventory(), allocateOrderResponse.getAllocationError());
    }

}
