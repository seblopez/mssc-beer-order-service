package guru.springframework.beer.order.service.services;

import groovy.lang.Tuple2;
import guru.springframework.beer.order.service.bootstrap.BeerOrderBootStrap;
import guru.springframework.beer.order.service.client.BeerClient;
import guru.springframework.beer.order.service.domain.Customer;
import guru.springframework.beer.order.service.exceptions.NotFoundException;
import guru.springframework.beer.order.service.repositories.BeerOrderRepository;
import guru.springframework.beer.order.service.repositories.CustomerRepository;
import guru.springframework.beer.order.service.web.model.BeerOrderDto;
import guru.springframework.beer.order.service.web.model.BeerOrderLineDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class TastingRoomService {

    private final CustomerRepository customerRepository;
    private final BeerOrderService beerOrderService;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerClient beerClient;
    private final List<String> beerUpcs = new ArrayList<>(3);

    public TastingRoomService(CustomerRepository customerRepository, BeerOrderService beerOrderService,
                              BeerOrderRepository beerOrderRepository, BeerClient beerClient) {
        this.customerRepository = customerRepository;
        this.beerOrderService = beerOrderService;
        this.beerOrderRepository = beerOrderRepository;
        this.beerClient = beerClient;

        beerUpcs.add(BeerOrderBootStrap.BEER_1_UPC);
        beerUpcs.add(BeerOrderBootStrap.BEER_2_UPC);
        beerUpcs.add(BeerOrderBootStrap.BEER_3_UPC);
    }

    @Transactional
    @Scheduled(fixedRate = 2000) //run every 2 seconds
    public void placeTastingRoomOrder(){

        List<Customer> customerList = customerRepository.findAllByCustomerNameLike(BeerOrderBootStrap.TASTING_ROOM);

        if (customerList.size() == 1){ //should be just one
            doPlaceOrder(customerList.get(0));
        } else {
            log.error("Too many or too few tasting room customers found");
        }
    }

    private void doPlaceOrder(Customer customer) {
        Tuple2<UUID, String> beerToOrder = getRandomBeer();

        BeerOrderLineDto beerOrderLine = BeerOrderLineDto.builder()
                .beerId(beerToOrder.getFirst())
                .upc(beerToOrder.getSecond())
                .orderQuantity(new Random().nextInt(6) + 1) //todo externalize value to property
                .build();

        List<BeerOrderLineDto> beerOrderLineSet = new ArrayList<>();
        beerOrderLineSet.add(beerOrderLine);

        BeerOrderDto beerOrder = BeerOrderDto.builder()
                .customerId(customer.getId())
                .customerRef(UUID.randomUUID().toString())
                .beerOrderLines(beerOrderLineSet)
                .build();

        BeerOrderDto savedOrder = beerOrderService.placeOrder(customer.getId(), beerOrder);

    }

    private Tuple2<UUID, String> getRandomBeer() {
        final String upc = beerUpcs.get(new Random().nextInt(beerUpcs.size() - 0));
        final UUID beerId = this.beerClient.getBeerByUpc(upc)
                .orElseThrow(() -> {
                    final String errorMessage = MessageFormat.format("UPC {0} not found", upc);
                    log.error(errorMessage);
                    return new NotFoundException(errorMessage);
                })
                .getId();
        return new Tuple2<>(beerId, upc);
    }
}
