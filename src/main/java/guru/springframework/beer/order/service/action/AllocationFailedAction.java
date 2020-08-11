package guru.springframework.beer.order.service.action;

import guru.springframework.beer.order.service.config.JmsConfig;
import guru.springframework.beer.order.service.domain.BeerOrderEvent;
import guru.springframework.beer.order.service.domain.BeerOrderStatus;
import guru.springframework.beer.order.service.services.BeerOrderManagerImpl;
import guru.springframework.beer.order.service.statemachine.event.AllocationFailureEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllocationFailedAction implements Action<BeerOrderStatus, BeerOrderEvent> {
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> stateContext) {
        final String orderId = (String) stateContext.getMessageHeaders().get(BeerOrderManagerImpl.BEER_ORDER_ID_HDR);
        log.error(MessageFormat.format("Allocation failed for order id {0}", orderId));
        jmsTemplate.convertAndSend(JmsConfig.ALLOCATE_ORDER_FAILURE_QUEUE,
                AllocationFailureEvent.builder()
                        .orderId(UUID.fromString(orderId))
                        .build());
    }
}
