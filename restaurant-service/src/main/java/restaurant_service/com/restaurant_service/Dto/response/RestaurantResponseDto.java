package restaurant_service.com.restaurant_service.Dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RestaurantResponseDto {
    private Long id;
    private Long ownerId;
    private String name;
    private String address;
    private String contactNumber;
    private String imageUrl;

    @Builder.Default
    private boolean open = false;

    private List<MenuItemResponseDto> menuItems;
}