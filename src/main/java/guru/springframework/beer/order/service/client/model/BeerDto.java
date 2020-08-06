package guru.springframework.beer.order.service.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class BeerDto implements Serializable {
    static final long serialVersionUID = 2117894318882735868L;

    @Null
    private UUID id;

    @NotBlank
    private String beerName;

    @NotNull
    private String upc;

    @NotNull
    private String beerStyle;
}
