package guru.springframework.beer.order.service.action;

import guru.springframework.beer.order.service.config.JmsConfig;
import guru.springframework.beer.order.service.domain.BeerOrder;
import guru.springframework.beer.order.service.domain.BeerOrderEvent;
import guru.springframework.beer.order.service.domain.BeerOrderStatus;
import guru.springframework.beer.order.service.repositories.BeerOrderRepository;
import guru.springframework.beer.order.service.services.BeerOrderManagerImpl;
import guru.springframework.beer.order.service.statemachine.event.ValidateBeerOrderRequest;
import guru.springframework.beer.order.service.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class ValidateOrderAction implements Action<BeerOrderStatus, BeerOrderEvent> {
    private final BeerOrderRepository beerOrderRepository;
    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> stateContext) {
        final String orderId = (String) stateContext.getMessageHeaders().get(BeerOrderManagerImpl.BEER_ORDER_ID_HDR);
        final BeerOrder beerOrder = beerOrderRepository.findOneById(UUID.fromString(orderId));

        jmsTemplate.convertAndSend(JmsConfig.BEER_ORDER_VALIDATE_QUEUE, ValidateBeerOrderRequest.builder()
                .beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrder))
                .build());

        log.debug(MessageFormat.format("Sending validation to inventory for Beer Order Id {0}", beerOrder.getId()));

    }
}
