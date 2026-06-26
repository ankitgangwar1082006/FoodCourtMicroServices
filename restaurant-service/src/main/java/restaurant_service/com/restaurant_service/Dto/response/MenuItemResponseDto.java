package restaurant_service.com.restaurant_service.Dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MenuItemResponseDto {
    private Long id;
    private Long restaurantId;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;

    @Builder.Default
    private boolean vegetarian = false;

    @Builder.Default
    private boolean available = false;
}