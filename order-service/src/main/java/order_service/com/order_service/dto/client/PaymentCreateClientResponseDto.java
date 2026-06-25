package order_service.com.order_service.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import order_service.com.order_service.enums.PaymentStatus;
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PaymentCreateClientResponseDto {
    private String razorpayOrderId;
    private PaymentStatus paymentStatus;
    private String message;
}
