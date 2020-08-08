package guru.springframework.beer.order.service.config;

import guru.springframework.beer.order.service.event.ValidateBeerOrderResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JmsConfig {
    public static final String BEER_ORDER_SERVICE_QUEUE = "beer-order-service";
    public static final String BEER_ORDER_VALIDATE_QUEUE = "validate-order";
    public static final String BEER_ORDER_VALIDATE_RESPONSE_QUEUE = "validate-order-result";
    public static final String ALLOCATE_ORDER_REQUEST_QUEUE = "allocate-order";

    @Bean
    public MessageConverter messageConverter() {
        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("guru.springframework.msscbeerservice.service.beerorder.model.ValidateBeerOrderResponse", ValidateBeerOrderResponse.class);

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setTypeIdMappings(typeIdMappings);

        return converter;

    }

}
