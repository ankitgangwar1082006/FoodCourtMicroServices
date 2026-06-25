package order_service.com.order_service.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemClientDto {
    private Long id;
    private Long restaurantId;
    private String name;
    private Double price;
    private Boolean available;
}
