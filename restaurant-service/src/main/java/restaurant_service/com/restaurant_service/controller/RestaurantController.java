package restaurant_service.com.restaurant_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import restaurant_service.com.restaurant_service.Dto.request.RestaurantRequestDto;
import restaurant_service.com.restaurant_service.Dto.response.RestaurantResponseDto;
import restaurant_service.com.restaurant_service.Dto.response.RestaurantResponsePageDto;
import restaurant_service.com.restaurant_service.security.JwtService;
import restaurant_service.com.restaurant_service.service.RestaurantService;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<RestaurantResponseDto> createRestaurant(@RequestBody RestaurantRequestDto requestDto,@RequestHeader("Authorization") String authHeader) {
        Long id = jwtService.fetchId(authHeader.substring(7));
        RestaurantResponseDto responseDto = restaurantService.createRestaurant(requestDto,id);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponseDto> getRestaurantById(@PathVariable Long id) {
        RestaurantResponseDto responseDto = restaurantService.getRestaurantById(id);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<RestaurantResponsePageDto> getAllRestaurants(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {

        RestaurantResponsePageDto responsePageDto = restaurantService.getAllRestaurants(pageNo, pageSize);
        return new ResponseEntity<>(responsePageDto, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponseDto> updateRestaurant(
            @PathVariable Long id,
            @RequestBody RestaurantRequestDto requestDto,@RequestHeader("Authorization") String authHeader) {
        Long loggedInId=jwtService.fetchId(authHeader.substring(7));
        RestaurantResponseDto responseDto = restaurantService.updateRestaurant(id, requestDto,loggedInId);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRestaurant(@PathVariable Long id,@RequestHeader("Authorization") String authHeader) {
        Long loggedInId=jwtService.fetchId(authHeader.substring(7));
        restaurantService.deleteRestaurant(id,loggedInId);
        return new ResponseEntity<>("Restaurant deleted successfully", HttpStatus.OK);
    }
}