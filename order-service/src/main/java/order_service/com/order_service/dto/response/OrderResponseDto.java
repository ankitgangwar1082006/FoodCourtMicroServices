package order_service.com.order_service.dto.response;

import lombok.Builder;
import lombok.Data;
import order_service.com.order_service.enums.OrderStatus;
import order_service.com.order_service.enums.PaymentMethod;
import order_service.com.order_service.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponseDto {

    private Long id;
    private String deliveryOtp;
    private Long userId;
    private Long restaurantId;
    private Double totalPrice;
    private String deliveryAddress;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
    private List<OrderItemResponseDto> items;
    private String razorpayOrderId;
    private String msg;
}