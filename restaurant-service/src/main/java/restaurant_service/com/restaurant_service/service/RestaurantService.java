package restaurant_service.com.restaurant_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import restaurant_service.com.restaurant_service.Dto.request.RestaurantRequestDto;
import restaurant_service.com.restaurant_service.Dto.response.MenuItemResponseDto;
import restaurant_service.com.restaurant_service.Dto.response.RestaurantResponseDto;
import restaurant_service.com.restaurant_service.Dto.response.RestaurantResponsePageDto;
import restaurant_service.com.restaurant_service.Entity.MenuItem;
import restaurant_service.com.restaurant_service.Entity.Restaurant;
import restaurant_service.com.restaurant_service.Repository.RestaurantRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantResponseDto createRestaurant(RestaurantRequestDto requestDto, Long id) {
        Restaurant restaurant = mapToEntity(requestDto);
        restaurant.setOwnerId(id);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return mapToResponseDto(savedRestaurant);
    }

    public RestaurantResponseDto getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));
        return mapToResponseDto(restaurant);
    }

    public RestaurantResponsePageDto getAllRestaurants(int pageNo, int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<Restaurant> restaurantPage = restaurantRepository.findAll(pageable);

        List<Restaurant> restaurants = restaurantPage.getContent();
        List<RestaurantResponseDto> responseDtos = new ArrayList<>();

        for (Restaurant restaurant : restaurants) {
            responseDtos.add(mapToResponseDto(restaurant));
        }

        return RestaurantResponsePageDto.builder()
                .content(responseDtos)
                .pageNo(restaurantPage.getNumber())
                .pageSize(restaurantPage.getSize())
                .totalElements(restaurantPage.getTotalElements())
                .totalPages(restaurantPage.getTotalPages())
                .last(restaurantPage.isLast())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public RestaurantResponseDto updateRestaurant(Long id, RestaurantRequestDto requestDto, Long loggedId) {
        Restaurant existingRestaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));

        if (!existingRestaurant.getOwnerId().equals(loggedId)) {
            throw new RuntimeException("Unauthorized: Tu is dukaan ka maalik nahi hai!");
        }

        existingRestaurant.setName(requestDto.getName());
        existingRestaurant.setAddress(requestDto.getAddress());
        existingRestaurant.setContactNumber(requestDto.getContactNumber());
        existingRestaurant.setOpen(Boolean.TRUE.equals(requestDto.getOpen()));
        existingRestaurant.setImageUrl(requestDto.getImageUrl());

        Restaurant updatedRestaurant = restaurantRepository.save(existingRestaurant);
        return mapToResponseDto(updatedRestaurant);
    }

    public void deleteRestaurant(Long id, Long loggedId) {
        Restaurant existingRestaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + id));

        if (!existingRestaurant.getOwnerId().equals(loggedId)) {
            throw new RuntimeException("Unauthorized: Tu is dukaan ka maalik nahi hai!");
        }

        restaurantRepository.deleteById(id);
    }

    private Restaurant mapToEntity(RestaurantRequestDto dto) {
        return Restaurant.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .contactNumber(dto.getContactNumber())
                .open(Boolean.TRUE.equals(dto.getOpen()))
                .imageUrl(dto.getImageUrl())
                .build();
    }

    private RestaurantResponseDto mapToResponseDto(Restaurant restaurant) {
        List<MenuItemResponseDto> menuItemDtos = null;

        if (restaurant.getMenuItems() != null) {
            menuItemDtos = new ArrayList<>();
            for (MenuItem item : restaurant.getMenuItems()) {
                menuItemDtos.add(mapMenuItemToResponseDto(item, restaurant));
            }
        }

        return RestaurantResponseDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .contactNumber(restaurant.getContactNumber())
                .open(Boolean.TRUE.equals(restaurant.getOpen()))
                .imageUrl(restaurant.getImageUrl())
                .menuItems(menuItemDtos)
                .ownerId(restaurant.getOwnerId())
                .build();
    }

    private MenuItemResponseDto mapMenuItemToResponseDto(MenuItem item, Restaurant restaurant) {
        return MenuItemResponseDto.builder()
                .id(item.getId())
                .restaurantId(restaurant.getId())
                .restaurantName(restaurant.getName())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .vegetarian(Boolean.TRUE.equals(item.getVegetarian()))
                .available(Boolean.TRUE.equals(item.getAvailable()))
                .imageUrl(item.getImageUrl())
                .categoryId(item.getCategoryId())
                .build();
    }
}