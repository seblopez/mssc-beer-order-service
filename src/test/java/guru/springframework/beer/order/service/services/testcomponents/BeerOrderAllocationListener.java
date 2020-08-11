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

        if(customerRef == null) {
            log.debug("Allocation listener will allocate this order");

            beerOrderDto.getBeerOrderLines()
                    .forEach(beerOrderLineDto -> {
                            beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
                        });

            jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                    AllocateOrderResponse.builder()
                            .beerOrderDto(beerOrderDto)
                            .allocationError(false)
                            .pendingInventory(false)
                            .build());
        } else if(!customerRef.equals("cancel-order-allocation")) {
            final boolean allocationError = customerRef.equals("allocation-failed");
            final boolean pendingInventory = customerRef.equals("pending-inventory");

            log.debug("Allocation listener will either fail the allocation of this order or it will leave it pending");

            beerOrderDto.getBeerOrderLines()
                    .forEach(beerOrderLineDto -> {
                        if(pendingInventory) {
                            beerOrderLineDto.setQuantityAllocated(beerOrderLineDto.getOrderQuantity() - 1);
                        }
                    });

            jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                    AllocateOrderResponse.builder()
                            .beerOrderDto(beerOrderDto)
                            .allocationError(allocationError)
                            .pendingInventory(pendingInventory)
                            .build());
        } else {

        }

    }

}
