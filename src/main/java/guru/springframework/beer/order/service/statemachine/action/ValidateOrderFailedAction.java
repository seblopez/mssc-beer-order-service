package guru.springframework.beer.order.service.statemachine.action;

import guru.springframework.beer.order.service.domain.BeerOrderEvent;
import guru.springframework.beer.order.service.domain.BeerOrderStatus;
import guru.springframework.beer.order.service.services.BeerOrderManagerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;

@Slf4j
@RequiredArgsConstructor
@Component
public class ValidateOrderFailedAction implements Action<BeerOrderStatus, BeerOrderEvent> {
    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> stateContext) {
        final String orderId = (String) stateContext.getMessageHeaders().get(BeerOrderManagerImpl.BEER_ORDER_ID_HDR);
        log.error(MessageFormat.format("Validation failed for order id {0}", orderId));
    }
}
