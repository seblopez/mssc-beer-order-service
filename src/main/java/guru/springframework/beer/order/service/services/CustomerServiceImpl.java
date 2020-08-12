package guru.springframework.beer.order.service.services;

import guru.springframework.beer.order.service.domain.Customer;
import guru.springframework.beer.order.service.exceptions.NotFoundException;
import guru.springframework.beer.order.service.repositories.CustomerRepository;
import guru.springframework.beer.order.service.web.mappers.CustomerMapper;
import guru.springframework.beer.order.service.web.model.CustomerDto;
import guru.springframework.beer.order.service.web.model.CustomerPagedList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerPagedList getCustomers(Pageable page) {
        final Page<Customer> customers = customerRepository.findAll(page);
        return new CustomerPagedList(
                customers
                        .stream()
                        .map(customerMapper::customerToCustomerDto)
                        .collect(Collectors.toList()),
                PageRequest.of(customers.getPageable().getPageNumber(),
                        customers.getPageable().getPageSize()), customers.getTotalElements());
    }

    @Override
    public CustomerDto getCustomerById(UUID customerId) {
        final Customer customer = customerRepository.findById(customerId).orElseThrow(() -> {
            final String errorMessage = MessageFormat.format("Customer Id {0} not found!", customerId.toString());
            log.error(errorMessage);
            return new NotFoundException(errorMessage);
        });

        return customerMapper.customerToCustomerDto(customer);

    }
}
