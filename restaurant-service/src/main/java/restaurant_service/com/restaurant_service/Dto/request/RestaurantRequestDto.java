package restaurant_service.com.restaurant_service.Dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RestaurantRequestDto {
    @NotBlank(message = "Restaurant name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    private String contactNumber;
    private String imageUrl;
    private Boolean isOpen;
}