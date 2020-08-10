package guru.springframework.beer.order.service.services.testcomponents;

import guru.springframework.beer.order.service.config.JmsConfig;
import guru.springframework.beer.order.service.statemachine.event.ValidateBeerOrderRequest;
import guru.springframework.beer.order.service.statemachine.event.ValidateBeerOrderResponse;
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
public class BeerOrderValidationListener {
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.BEER_ORDER_VALIDATE_QUEUE)
    public void listener(Message msg) {
        ValidateBeerOrderRequest request = (ValidateBeerOrderRequest) msg.getPayload();

        log.debug("Testing validation listener run");

        final BeerOrderDto beerOrderDto = request.getBeerOrderDto();

        final String customerRef = beerOrderDto.getCustomerRef();
        final boolean isValid = customerRef == null ||
                (customerRef != null && !customerRef.equals("fail-validation"));

        jmsTemplate.convertAndSend(JmsConfig.BEER_ORDER_VALIDATE_RESPONSE_QUEUE,
                ValidateBeerOrderResponse.builder()
                        .isValid(isValid)
                        .orderId(beerOrderDto.getId())
                        .build());

    }
}
