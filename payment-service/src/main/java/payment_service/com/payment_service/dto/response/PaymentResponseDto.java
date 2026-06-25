package payment_service.com.payment_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import payment_service.com.payment_service.enums.PaymentStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDto {
    private String razorpayOrderId;
    private String transactionId;
    private Long orderId;
    private Double amount;
    private PaymentStatus paymentStatus;
    private String message;
}