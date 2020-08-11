package guru.springframework.beer.order.service.statemachine.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class AllocationFailureEvent {
    private UUID orderId;
}
