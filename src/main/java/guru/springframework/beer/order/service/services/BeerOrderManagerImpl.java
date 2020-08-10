package guru.springframework.beer.order.service.services;

import guru.springframework.beer.order.service.domain.BeerOrder;
import guru.springframework.beer.order.service.domain.BeerOrderEvent;
import guru.springframework.beer.order.service.domain.BeerOrderStatus;
import guru.springframework.beer.order.service.repositories.BeerOrderRepository;
import guru.springframework.beer.order.service.statemachine.interceptor.BeerOrderStateChangeInterceptor;
import guru.springframework.beer.order.service.web.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    @Transactional
    @Override
    public void processBeerOrderValidation(UUID beerOrderId, Boolean isValid) {
        beerOrderRepository.findById(beerOrderId)
                .ifPresentOrElse(order -> {
                    if(isValid) {
                        this.sendBeerOrderEvent(order, BeerOrderEvent.VALIDATION_PASSED);
                        final BeerOrder validatedOrder = this.beerOrderRepository.findOneById(beerOrderId);
                        this.sendBeerOrderEvent(validatedOrder, BeerOrderEvent.ALLOCATE_ORDER);
                    } else {
                        this.sendBeerOrderEvent(order, BeerOrderEvent.VALIDATION_FAILED);
                    }
                }, () -> log.error(MessageFormat.format("Order Id {0} not found!", beerOrderId)));

    }

    @Transactional
    @Override
    public void processBeerOrderAllocation(BeerOrderDto order, Boolean orderAllocated, Boolean error) {
        if(error) {
            processAllocationError(order);
        } else if(orderAllocated) {
            processSuccessfulAllocation(order);
        } else {
            processPendingAllocation(order);
        }

    }

    private void processAllocationError(BeerOrderDto beerOrderDto) {
        final UUID orderId = beerOrderDto.getId();
        beerOrderRepository.findById(orderId)
                .ifPresentOrElse(beerOrder -> {
                    log.error(MessageFormat.format("There was an error processing the allocation request for Beer Order {0}", beerOrder.getId()));
                    sendBeerOrderEvent(beerOrder, BeerOrderEvent.ALLOCATION_FAILED);
                    }
                    , () -> log.error(MessageFormat.format("Order Id {0} not found while processing allocation error", orderId)));
    }

    private void processPendingAllocation(BeerOrderDto beerOrderDto) {
        final UUID orderId = beerOrderDto.getId();
        beerOrderRepository.findById(orderId)
                .ifPresentOrElse(beerOrder -> {
                            log.error(MessageFormat.format("Beer Order {0} was not completely allocated", beerOrder.getId()));
                            sendBeerOrderEvent(beerOrder, BeerOrderEvent.ALLOCATION_NO_INVENTORY);
                            updateAllocatedQty(beerOrderDto);
                        }
                        , () -> log.error(MessageFormat.format("Order Id {0} not found while processing allocation error", orderId)));
    }

    private void processSuccessfulAllocation(BeerOrderDto beerOrderDto) {
        final UUID orderId = beerOrderDto.getId();
        beerOrderRepository.findById(orderId)
                .ifPresentOrElse(beerOrder -> {
                            log.error(MessageFormat.format("Beer Order {0} was not completely allocated", beerOrder.getId()));
                            sendBeerOrderEvent(beerOrder, BeerOrderEvent.ALLOCATION_SUCCESS);
                            updateAllocatedQty(beerOrderDto);
                        }
                        , () -> log.error(MessageFormat.format("Order Id {0} not found while processing allocation error", orderId)));
    }

    private void updateAllocatedQty(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> allocatedOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        allocatedOrderOptional.ifPresentOrElse(allocatedOrder -> {
            allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
                beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
                    if(beerOrderLine.getId() .equals(beerOrderLineDto.getId())){
                        beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
                    }
                });
            });

            beerOrderRepository.saveAndFlush(allocatedOrder);
        }, () -> log.error("Order Not Found. Id: " + beerOrderDto.getId()));
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
                    Map<String, Object> eventHeaders = new HashMap<>();
                    eventHeaders.put(BEER_ORDER_ID_HDR, order);
                    sm.resetStateMachine(new DefaultStateMachineContext<>(order.getOrderStatus(), null, eventHeaders, null));
                });

        stateMachine.start();

        return stateMachine;

    }
}
