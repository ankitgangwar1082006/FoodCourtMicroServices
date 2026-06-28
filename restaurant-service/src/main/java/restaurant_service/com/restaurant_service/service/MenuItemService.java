package restaurant_service.com.restaurant_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import restaurant_service.com.restaurant_service.Dto.request.MenuItemRequestDto;
import restaurant_service.com.restaurant_service.Dto.response.MenuItemResponseDto;
import restaurant_service.com.restaurant_service.Dto.response.MenuItemResponsePageDto;
import restaurant_service.com.restaurant_service.Entity.MenuItem;
import restaurant_service.com.restaurant_service.Entity.Restaurant;
import restaurant_service.com.restaurant_service.Repository.MenuItemRepository;
import restaurant_service.com.restaurant_service.Repository.RestaurantRepository;
import restaurant_service.com.restaurant_service.enums.FoodCategory;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuItemResponseDto createMenuItem(MenuItemRequestDto requestDto, Long loggedInId) {
        Restaurant restaurant = restaurantRepository.findById(requestDto.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + requestDto.getRestaurantId()));

        if (!restaurant.getOwnerId().equals(loggedInId)) {
            throw new RuntimeException("Unauthorized: Tu is dukaan ka maalik nahi hai!");
        }

        MenuItem menuItem = mapToEntity(requestDto, restaurant);
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        return mapToResponseDto(savedMenuItem);
    }

    public MenuItemResponseDto getMenuItemById(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));
        return mapToResponseDto(menuItem);
    }

    public MenuItemResponsePageDto getAllMenuItems(int pageNo, int pageSize, Integer categoryId) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<MenuItem> menuItemPage;

        if (categoryId != null) {
            validateCategoryId(categoryId);
            menuItemPage = menuItemRepository.findByCategoryId(categoryId, pageable);
        } else {
            menuItemPage = menuItemRepository.findAll(pageable);
        }

        List<MenuItem> menuItems = menuItemPage.getContent();
        List<MenuItemResponseDto> responseDtos = new ArrayList<>();

        for (MenuItem item : menuItems) {
            responseDtos.add(mapToResponseDto(item));
        }

        return MenuItemResponsePageDto.builder()
                .content(responseDtos)
                .pageNo(menuItemPage.getNumber())
                .pageSize(menuItemPage.getSize())
                .totalElements(menuItemPage.getTotalElements())
                .totalPages(menuItemPage.getTotalPages())
                .last(menuItemPage.isLast())
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public MenuItemResponseDto updateMenuItem(Long id, MenuItemRequestDto requestDto, Long loggedInId) {
        MenuItem existingItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));

        Restaurant restaurant = restaurantRepository.findById(requestDto.getRestaurantId())
                .orElseThrow(() -> new RuntimeException("Restaurant not found with id: " + requestDto.getRestaurantId()));

        if (!existingItem.getRestaurant().getOwnerId().equals(loggedInId)) {
            throw new RuntimeException("Unauthorized!");
        }

        if (!restaurant.getOwnerId().equals(loggedInId)) {
            throw new RuntimeException("Unauthorized!");
        }

        existingItem.setName(requestDto.getName());
        existingItem.setDescription(requestDto.getDescription());
        existingItem.setPrice(requestDto.getPrice());
        existingItem.setImageUrl(requestDto.getImageUrl());
        existingItem.setVegetarian(Boolean.TRUE.equals(requestDto.getVegetarian()));
        existingItem.setAvailable(Boolean.TRUE.equals(requestDto.getAvailable()));
        existingItem.setCategoryId(requestDto.getCategoryId());
        existingItem.setRestaurant(restaurant);

        MenuItem updatedItem = menuItemRepository.save(existingItem);
        return mapToResponseDto(updatedItem);
    }

    public void deleteMenuItem(Long id, Long loggedInId) {
        MenuItem existingItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + id));

        if (!existingItem.getRestaurant().getOwnerId().equals(loggedInId)) {
            throw new RuntimeException("Unauthorized!");
        }

        menuItemRepository.deleteById(id);
    }

    private MenuItem mapToEntity(MenuItemRequestDto dto, Restaurant restaurant) {
        return MenuItem.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imageUrl(dto.getImageUrl())
                .vegetarian(Boolean.TRUE.equals(dto.getVegetarian()))
                .available(Boolean.TRUE.equals(dto.getAvailable()))
                .categoryId(dto.getCategoryId())
                .restaurant(restaurant)
                .build();
    }

    private MenuItemResponseDto mapToResponseDto(MenuItem item) {
        return MenuItemResponseDto.builder()
                .id(item.getId())
                .restaurantId(item.getRestaurant().getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .imageUrl(item.getImageUrl())
                .vegetarian(Boolean.TRUE.equals(item.getVegetarian()))
                .available(Boolean.TRUE.equals(item.getAvailable()))
                .categoryId(item.getCategoryId())
                .build();
    }

    private void validateCategoryId(Integer categoryId) {
        if (!FoodCategory.isValid(categoryId)) {
            throw new RuntimeException("Category ID must be between 1 and 5");
        }
    }
}