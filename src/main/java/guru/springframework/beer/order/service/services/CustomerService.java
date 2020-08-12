package guru.springframework.beer.order.service.services;

import guru.springframework.beer.order.service.web.model.CustomerDto;
import guru.springframework.beer.order.service.web.model.CustomerPagedList;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {

    CustomerPagedList getCustomers(Pageable page);

    CustomerDto getCustomerById(UUID customerId);

}
