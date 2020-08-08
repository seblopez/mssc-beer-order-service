package guru.springframework.beer.order.service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ValidateBeerOrderResponse implements Serializable {
    private final static long serialVersionUID = 2497457735979346484L;

    private UUID orderId;
    private Boolean isValid;
            ;
}
