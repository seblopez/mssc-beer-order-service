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

@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderAllocationListener {
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_REQUEST_QUEUE)
    public void listener(Message msg) {
        AllocateOrderRequest request = (AllocateOrderRequest) msg.getPayload();

        final BeerOrderDto beerOrderDto = request.getBeerOrderDto();

        final String customerRef = beerOrderDto.getCustomerRef();
        final boolean allocationError = customerRef != null && customerRef.equals("allocation-failed");
        final boolean pendingInventory = customerRef != null && customerRef.equals("pending-inventory");

        beerOrderDto.getBeerOrderLines()
                .forEach(beerOrderLineDto -> {
                    if(pendingInventory) {
                        beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity() - 1);
                    } else {
                        beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
                    }});

        log.debug("Testing allocation listener run");
        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                AllocateOrderResponse.builder()
                        .beerOrderDto(beerOrderDto)
                        .allocationError(allocationError)
                        .pendingInventory(pendingInventory)
                        .build());
    }

}
