package payment_service.com.payment_service.clientFallback;

import payment_service.com.payment_service.client.OrderServiceClient;
import payment_service.com.payment_service.dto.client.OrderClientResponseDto;

public class OrderServiceFallback implements OrderServiceClient {


    @Override
    public void markPaymentSuccess(Long orderId, String internalToken) {
    }

}
