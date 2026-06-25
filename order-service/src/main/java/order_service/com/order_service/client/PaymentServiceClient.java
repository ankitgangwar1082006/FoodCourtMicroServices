package order_service.com.order_service.client;

import order_service.com.order_service.clientFallback.PaymentFallback;
import order_service.com.order_service.dto.client.PaymentCreateClientRequestDto;
import order_service.com.order_service.dto.client.PaymentCreateClientResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "PAYMENT-SERVICE",fallback = PaymentFallback.class,configuration = FeignClientSecurityConfig.class)
public interface PaymentServiceClient {

    @PostMapping("/api/payments/create-order")
    PaymentCreateClientResponseDto createPaymentRequest(@RequestBody PaymentCreateClientRequestDto requestDto);
}
