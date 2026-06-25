package order_service.com.order_service.client;

import order_service.com.order_service.clientFallback.RestaurantFallback;
import order_service.com.order_service.dto.client.MenuItemClientDto;
import order_service.com.order_service.dto.client.RestaurantClientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "RESTAURANT-SERVICE", fallback = RestaurantFallback.class)
public interface RestaurantServiceClient {

    @GetMapping("/api/menu-items/{id}")
    MenuItemClientDto getMenuItemById(@PathVariable("id") Long id);

    @GetMapping("/api/restaurants/{id}")
    RestaurantClientDto getRestaurantById(@PathVariable("id") Long id);
}