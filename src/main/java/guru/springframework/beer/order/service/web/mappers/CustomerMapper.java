package guru.springframework.beer.order.service.web.mappers;

import guru.springframework.beer.order.service.domain.Customer;
import guru.springframework.beer.order.service.web.model.CustomerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {DateMapper.class})
public interface CustomerMapper {

    @Mapping(source = "name", target = "customerName")
    Customer customerDtoToCustomer(CustomerDto customerDto);

    @Mapping(source = "customerName", target = "name")
    CustomerDto customerToCustomerDto(Customer customer);

}
