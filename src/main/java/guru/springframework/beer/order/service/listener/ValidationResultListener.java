package guru.springframework.beer.order.service.listener;

import guru.springframework.beer.order.service.config.JmsConfig;
import guru.springframework.beer.order.service.event.ValidateBeerOrderResponse;
import guru.springframework.beer.order.service.services.BeerOrderManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class ValidationResultListener {
    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.BEER_ORDER_VALIDATE_RESPONSE_QUEUE)
    public void listen(ValidateBeerOrderResponse validationResponse) {
        final UUID orderId = validationResponse.getOrderId();
        log.info(MessageFormat.format("Getting response for Order validation of order Id {0}", orderId.toString()));
        beerOrderManager.processBeerOrderValidation(orderId, validationResponse.getIsValid());
    }

}
