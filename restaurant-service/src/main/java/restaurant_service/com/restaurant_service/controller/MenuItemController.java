package restaurant_service.com.restaurant_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import restaurant_service.com.restaurant_service.Dto.request.MenuItemRequestDto;
import restaurant_service.com.restaurant_service.Dto.response.MenuItemResponseDto;
import restaurant_service.com.restaurant_service.Dto.response.MenuItemResponsePageDto;
import restaurant_service.com.restaurant_service.security.JwtService;
import restaurant_service.com.restaurant_service.service.MenuItemService;

@RestController
@RequestMapping("/api/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<MenuItemResponseDto> createMenuItem(
            @Valid @RequestBody MenuItemRequestDto requestDto,
            @RequestHeader("Authorization") String authHeader) {

        Long loggedInId = jwtService.fetchId(authHeader.substring(7));
        MenuItemResponseDto responseDto = menuItemService.createMenuItem(requestDto, loggedInId);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponseDto> getMenuItemById(@PathVariable Long id) {
        MenuItemResponseDto responseDto = menuItemService.getMenuItemById(id);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<MenuItemResponsePageDto> getAllMenuItems(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
            @RequestParam(value = "categoryId", required = false) Integer categoryId) {

        MenuItemResponsePageDto responsePageDto = menuItemService.getAllMenuItems(pageNo, pageSize, categoryId);
        return new ResponseEntity<>(responsePageDto, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuItemResponseDto> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequestDto requestDto,
            @RequestHeader("Authorization") String authHeader) {

        Long loggedInId = jwtService.fetchId(authHeader.substring(7));
        MenuItemResponseDto responseDto = menuItemService.updateMenuItem(id, requestDto, loggedInId);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMenuItem(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        Long loggedInId = jwtService.fetchId(authHeader.substring(7));
        menuItemService.deleteMenuItem(id, loggedInId);
        return new ResponseEntity<>("Menu item deleted successfully", HttpStatus.OK);
    }
}