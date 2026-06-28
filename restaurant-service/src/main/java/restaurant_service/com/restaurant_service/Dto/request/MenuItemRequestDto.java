package restaurant_service.com.restaurant_service.Dto.request;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @NotNull(message = "Category is required")
    @Min(value = 1, message = "Category ID must be between 1 and 5")
    @Max(value = 5, message = "Category ID must be between 1 and 5")
    private Integer categoryId;
}