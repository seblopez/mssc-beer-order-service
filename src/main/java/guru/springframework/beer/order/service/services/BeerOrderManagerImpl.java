package guru.springframework.beer.order.service.services;

import guru.springframework.beer.order.service.domain.BeerOrder;
import guru.springframework.beer.order.service.domain.BeerOrderEvent;
import guru.springframework.beer.order.service.domain.BeerOrderStatus;
import guru.springframework.beer.order.service.interceptor.BeerOrderStateChangeInterceptor;
import guru.springframework.beer.order.service.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {
    public static final String BEER_ORDER_ID_HDR = "beer_order";
    private final StateMachineFactory<BeerOrderStatus, BeerOrderEvent> factory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder order) {
        order.setId(null);
        order.setOrderStatus(BeerOrderStatus.NEW);

        final BeerOrder savedOrder = beerOrderRepository.save(order);

        this.sendBeerOrderEvent(savedOrder, BeerOrderEvent.VALIDATE_ORDER);

        return savedOrder;

    }

    private void sendBeerOrderEvent(BeerOrder order, BeerOrderEvent event) {
        final StateMachine<BeerOrderStatus, BeerOrderEvent> stateMachine = build(order);

        Message message = MessageBuilder.withPayload(event)
                .setHeader(BEER_ORDER_ID_HDR, order.getId().toString())
                .build();

        stateMachine.sendEvent(message);

    }

    private StateMachine<BeerOrderStatus, BeerOrderEvent> build(BeerOrder order) {
        final StateMachine<BeerOrderStatus, BeerOrderEvent> stateMachine = factory.getStateMachine(order.getId());

        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sm -> {
                    sm.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
                    sm.resetStateMachine(new DefaultStateMachineContext<>(order.getOrderStatus(), null, null, null));
                });

        stateMachine.start();

        return stateMachine;

    }
}
