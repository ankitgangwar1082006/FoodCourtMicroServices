package order_service.com.order_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemResponseDto {
    private Long id;
    private Long menuItemId;
    private Integer quantity;
    private Double priceAtTimeOfOrder;
    private String menuItemName;
}