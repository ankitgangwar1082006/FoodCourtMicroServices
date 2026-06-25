package order_service.com.order_service.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantClientDto {
    private Long id;
    private String name;
    private Long ownerId;
    private Boolean open;
}