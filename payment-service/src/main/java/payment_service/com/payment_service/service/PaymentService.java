package payment_service.com.payment_service.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import payment_service.com.payment_service.client.OrderServiceClient;
import payment_service.com.payment_service.dto.client.OrderClientResponseDto;
import payment_service.com.payment_service.dto.request.PaymentCallbackDto;
import payment_service.com.payment_service.dto.request.PaymentRequestDto;
import payment_service.com.payment_service.dto.response.PaymentResponseDto;
import payment_service.com.payment_service.entity.Payment;
import payment_service.com.payment_service.enums.PaymentStatus;
import payment_service.com.payment_service.repository.PaymentRepository;

@Service
@RefreshScope
@RequiredArgsConstructor
public class PaymentService {
    @Value("${secret.token}")
    private String internalSecretToken;
    private final RazorpayClient razorpayClient;
    private final OrderServiceClient orderServiceClient;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    private final PaymentRepository repository;

    @Transactional(rollbackOn = Exception.class)
    public PaymentResponseDto createRazorpayOrder(PaymentRequestDto dto) throws RazorpayException {
        JSONObject order = new JSONObject();

        Double amt=dto.getAmount();
        Long amtPaisa = (long) (amt * 100);
        order.put("amount", amtPaisa);
        order.put("currency", "INR");
        order.put("receipt", "recpt:" + dto.getOrderId());

        Order razorOrder = razorpayClient.orders.create(order);

        Payment payment = createPayment(dto,amt);
        payment.setRazorpayOrderId(razorOrder.get("id"));
        repository.save(payment);

        return PaymentResponseDto.builder()
                .razorpayOrderId(razorOrder.get("id"))
                .orderId(dto.getOrderId())
                .paymentStatus(PaymentStatus.PENDING)
                .message("Request is Created")
                .amount(amt)
                .build();
    }

    @Transactional(rollbackOn = Exception.class)
    public boolean verifyPayment(PaymentCallbackDto callbackDto) {
        try {
            JSONObject object = new JSONObject();
            object.put("razorpay_order_id", callbackDto.getRazorpayOrderId());
            object.put("razorpay_payment_id", callbackDto.getRazorpayPaymentId());
            object.put("razorpay_signature", callbackDto.getRazorpaySignature());
            boolean isSignatureValid = Utils.verifyPaymentSignature(object, keySecret);

            Payment payment = repository.
                    findByRazorpayOrderId(callbackDto.getRazorpayOrderId()).
                    orElseThrow(() -> new RuntimeException("RazorPay Order Id is wrong: " + callbackDto.getRazorpayOrderId()));

            if(isSignatureValid){
                if(payment.getStatus() == PaymentStatus.COMPLETED){
                    return true;
                }
                orderServiceClient.markPaymentSuccess(payment.getOrderId(),internalSecretToken);
                payment.setRazorpayPaymentId(callbackDto.getRazorpayPaymentId());
                payment.setStatus(PaymentStatus.COMPLETED);
            }
            else{
                payment.setStatus(PaymentStatus.FAILED);
            }
            repository.save(payment);
            return isSignatureValid;

        } catch (RazorpayException e) {
            System.out.println("Payment Verification Failed: " + e.getMessage());
            return false;
        }
    }

    private Payment createPayment(PaymentRequestDto dto,double amt) {
        return Payment.builder()
                .amount(amt)
                .status(PaymentStatus.PENDING)
                .orderId(dto.getOrderId())
                .build();
    }
}