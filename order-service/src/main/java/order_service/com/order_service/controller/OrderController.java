package order_service.com.order_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import order_service.com.order_service.dto.request.OrderRequestDto;
import order_service.com.order_service.dto.response.OrderPageResponseDto;
import order_service.com.order_service.dto.response.OrderResponseDto;
import order_service.com.order_service.enums.OrderStatus;
import order_service.com.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    @Value("${secret.token}")
    String secretToken;

    @PostMapping("/create")
    public ResponseEntity<OrderResponseDto> createOrder(
            @RequestBody OrderRequestDto orderRequestDto,
            HttpServletRequest request) {

        Long authUserId = (Long) request.getAttribute("authenticated_user_id");
        if (authUserId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        OrderResponseDto createdOrder = orderService.createOrder(orderRequestDto, authUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDto> getOrderById(
            @PathVariable Long orderId,
            HttpServletRequest request) {

        Long authUserId = (Long) request.getAttribute("authenticated_user_id");
        if (authUserId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        OrderResponseDto order = orderService.getOrderById(orderId, authUserId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/all")
    public ResponseEntity<OrderPageResponseDto> getAllOrders(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        Long authUserId = (Long) request.getAttribute("authenticated_user_id");
        OrderPageResponseDto orders = orderService.getAllOrders(pageNumber, pageSize,authUserId);
        return ResponseEntity.ok(orders);
    }


    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus newStatus,
            HttpServletRequest request) {

        Long authUserId = (Long) request.getAttribute("authenticated_user_id");
        if (authUserId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        OrderResponseDto updatedOrder = orderService.updateOrderStatus(orderId, newStatus, authUserId);
        return ResponseEntity.ok(updatedOrder);
    }


    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> deleteOrder(
            @PathVariable Long orderId,
            HttpServletRequest request) {

        Long authUserId = (Long) request.getAttribute("authenticated_user_id");
        if (authUserId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        orderService.deleteOrder(orderId, authUserId);
        return ResponseEntity.ok("Order successfully deleted.");
    }


    @PostMapping("/{orderId}/verify-delivery")
    public ResponseEntity<OrderResponseDto> verifyDelivery(
            @PathVariable Long orderId,
            @RequestParam String otp,
            HttpServletRequest request) {

        Long authUserId = (Long) request.getAttribute("authenticated_user_id");
        if (authUserId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        OrderResponseDto completedOrder = orderService.verifyDeliveryAndCompleteOrder(orderId, otp, authUserId);
        return ResponseEntity.ok(completedOrder);
    }

    @PutMapping("/{orderId}/payment-success")
    public ResponseEntity<OrderResponseDto> makePaymentSuccessFull(@PathVariable("orderId") Long orderId,
                                                                   HttpServletRequest request,
                                                                   @RequestHeader(value = "X-Internal-Token", required = false) String internalToken)
    {
        Long authUserID = (Long) request.getAttribute("authenticated_user_id");
        if(!secretToken.equals(internalToken)) throw new RuntimeException("Unauthorized User");
        OrderResponseDto responseDto = orderService.updatePaymentStatusToSuccess(orderId,authUserID);
        return new ResponseEntity<>(responseDto,HttpStatus.OK);
    }

}