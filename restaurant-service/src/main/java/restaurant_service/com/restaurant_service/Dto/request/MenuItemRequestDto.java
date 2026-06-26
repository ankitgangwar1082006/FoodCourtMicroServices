package restaurant_service.com.restaurant_service.Dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuItemRequestDto {
    @NotBlank(message = "Item name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    private Double price;

    private String imageUrl;
    private Boolean vegetarian = false;
    private Boolean available = false;

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;
}