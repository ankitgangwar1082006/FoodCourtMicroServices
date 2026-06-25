package payment_service.com.payment_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackDto {

    @NotBlank(message = "Razorpay Order ID is required for verification")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay Payment ID is required for verification")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay Signature is required for verification")
    private String razorpaySignature;
}