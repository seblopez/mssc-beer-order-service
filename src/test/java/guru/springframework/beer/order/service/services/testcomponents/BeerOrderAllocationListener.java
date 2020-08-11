package guru.springframework.beer.order.service.services.testcomponents;

import guru.springframework.beer.order.service.config.JmsConfig;
import guru.springframework.beer.order.service.statemachine.event.AllocateOrderRequest;
import guru.springframework.beer.order.service.statemachine.event.AllocateOrderResponse;
import guru.springframework.beer.order.service.web.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_REQUEST_QUEUE)
    public void listener(Message msg) {
        AllocateOrderRequest request = (AllocateOrderRequest) msg.getPayload();

        final BeerOrderDto beerOrderDto = request.getBeerOrderDto();

        AtomicBoolean allocationError = new AtomicBoolean(false);
        AtomicBoolean pendingInventory = new AtomicBoolean(false);

        beerOrderDto.getBeerOrderLines()
                .forEach(beerOrderLineDto -> {
                    final Integer orderQuantity = beerOrderLineDto.getOrderQuantity();
                    if(orderQuantity <= 0) {
                        allocationError.set(true);
                    } else if(orderQuantity > 1000) {
                        pendingInventory.set(true);
                    } else {
                        beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
                    }});

        log.debug("Testing allocation listener run");
        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                AllocateOrderResponse.builder()
                        .beerOrderDto(beerOrderDto)
                        .allocationError(allocationError.getPlain())
                        .pendingInventory(pendingInventory.getPlain())
                        .build());
    }

}
