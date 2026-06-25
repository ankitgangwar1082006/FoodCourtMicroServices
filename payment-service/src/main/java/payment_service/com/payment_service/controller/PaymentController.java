package payment_service.com.payment_service.controller;

import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment_service.com.payment_service.dto.request.PaymentCallbackDto;
import payment_service.com.payment_service.dto.request.PaymentRequestDto;
import payment_service.com.payment_service.dto.response.PaymentResponseDto;
import payment_service.com.payment_service.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<PaymentResponseDto> createOrder(@Valid @RequestBody PaymentRequestDto request) throws RazorpayException {
        PaymentResponseDto response = paymentService.createRazorpayOrder(request);
        return ResponseEntity.ok(response);

    }
    @PostMapping("/verify")
    public ResponseEntity<String> verifyPayment(@RequestBody PaymentCallbackDto callbackDto) {
        boolean isValid = paymentService.verifyPayment(callbackDto);
        if (isValid) {
            return ResponseEntity.ok("Payment Verified Successfully! Txn ID: " + callbackDto.getRazorpayPaymentId());
        } else {
            return ResponseEntity.badRequest().body("Payment Verification Failed! Fake Request.");
        }
    }
}