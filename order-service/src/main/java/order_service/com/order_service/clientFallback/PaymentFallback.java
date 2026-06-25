package order_service.com.order_service.clientFallback;

import order_service.com.order_service.client.PaymentServiceClient;
import order_service.com.order_service.dto.client.PaymentCreateClientRequestDto;
import order_service.com.order_service.dto.client.PaymentCreateClientResponseDto;

public class PaymentFallback implements PaymentServiceClient {
    @Override
    public PaymentCreateClientResponseDto createPaymentRequest(PaymentCreateClientRequestDto requestDto) {
        throw new RuntimeException("Server down of Payment");
    }
}
