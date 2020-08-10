package guru.springframework.beer.order.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.beer.order.service.statemachine.event.ValidateBeerOrderRequest;
import guru.springframework.beer.order.service.statemachine.event.ValidateBeerOrderResponse;
import guru.springframework.beer.order.service.web.model.BeerOrderDto;
import guru.springframework.beer.order.service.web.model.BeerOrderLineDto;
import guru.springframework.beer.order.service.web.model.BeerOrderStatusDto;
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
    public static final String ALLOCATE_ORDER_RESPONSE_QUEUE = "allocate-order-result";

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        Map<String, Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put("guru.springframework.msscbeerservice.service.beerorder.model.ValidateBeerOrderRequest", ValidateBeerOrderRequest.class);
        typeIdMappings.put("guru.springframework.msscbeerservice.service.beerorder.model.ValidateBeerOrderResponse", ValidateBeerOrderResponse.class);
        typeIdMappings.put("guru.springframework.msscbeerservice.service.beerorder.model.BeerOrderDto", BeerOrderDto.class);
        typeIdMappings.put("guru.springframework.msscbeerservice.service.beerorder.model.BeerOrderLineDto", BeerOrderLineDto.class);
        typeIdMappings.put("guru.springframework.msscbeerservice.service.beerorder.model.BeerOrderStatusDto", BeerOrderStatusDto.class);

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper);
        converter.setTypeIdMappings(typeIdMappings);

        return converter;

    }

}
