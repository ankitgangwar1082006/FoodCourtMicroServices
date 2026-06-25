package order_service.com.order_service.clientFallback;

import order_service.com.order_service.client.RestaurantServiceClient;
import order_service.com.order_service.dto.client.MenuItemClientDto;
import order_service.com.order_service.dto.client.RestaurantClientDto;
import org.springframework.stereotype.Component;

@Component
public class RestaurantFallback implements RestaurantServiceClient {
    @Override
    public MenuItemClientDto getMenuItemById(Long id) {
        throw new RuntimeException("Service Unavailable: Cannot verify restaurant details at the moment.");
    }

    @Override
    public RestaurantClientDto getRestaurantById(Long id) {
        throw new RuntimeException("Service Unavailable: Cannot verify restaurant details at the moment.");
    }
}
