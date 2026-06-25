package payment_service.com.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import payment_service.com.payment_service.clientFallback.OrderServiceFallback;
import payment_service.com.payment_service.config.FeignClientSecurityConfig;
import payment_service.com.payment_service.dto.client.OrderClientResponseDto;

@FeignClient(name = "ORDER-SERVICE", fallback = OrderServiceFallback.class, configuration = FeignClientSecurityConfig.class)
public interface OrderServiceClient {

    @PutMapping("/api/orders/{orderId}/payment-success")
    void markPaymentSuccess(
            @PathVariable("orderId") Long orderId,
            @RequestHeader("X-Internal-Token") String internalToken);

}