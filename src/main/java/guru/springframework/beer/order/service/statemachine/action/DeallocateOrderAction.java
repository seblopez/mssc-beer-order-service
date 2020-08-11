package guru.springframework.beer.order.service.statemachine.action;

import guru.springframework.beer.order.service.config.JmsConfig;
import guru.springframework.beer.order.service.domain.BeerOrder;
import guru.springframework.beer.order.service.domain.BeerOrderEvent;
import guru.springframework.beer.order.service.domain.BeerOrderStatus;
import guru.springframework.beer.order.service.repositories.BeerOrderRepository;
import guru.springframework.beer.order.service.services.BeerOrderManagerImpl;
import guru.springframework.beer.order.service.statemachine.event.DeallocateOrderRequest;
import guru.springframework.beer.order.service.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeallocateOrderAction implements Action<BeerOrderStatus, BeerOrderEvent> {
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> stateContext) {
        final String orderId = (String) stateContext.getMessageHeaders().get(BeerOrderManagerImpl.BEER_ORDER_ID_HDR);
        if (orderId != null) {
            final Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(UUID.fromString(orderId));

            beerOrderOptional.ifPresentOrElse(beerOrder ->
                            jmsTemplate.convertAndSend(JmsConfig.DEALLOCATE_ORDER_REQUEST_QUEUE,
                                    DeallocateOrderRequest.builder()
                                            .beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrder))
                                            .build()),
                    () -> log.error(MessageFormat.format("Order id {0} not found", orderId)));
        } else {
            log.error("Beer Order not provided in context");
        }

    }
}
