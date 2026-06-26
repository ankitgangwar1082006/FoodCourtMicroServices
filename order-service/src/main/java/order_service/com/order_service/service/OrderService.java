package order_service.com.order_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import order_service.com.order_service.client.PaymentServiceClient;
import order_service.com.order_service.client.RestaurantServiceClient;
import order_service.com.order_service.dto.client.MenuItemClientDto;
import order_service.com.order_service.dto.client.PaymentCreateClientRequestDto;
import order_service.com.order_service.dto.client.PaymentCreateClientResponseDto;
import order_service.com.order_service.dto.client.RestaurantClientDto;
import order_service.com.order_service.dto.request.OrderItemRequestDto;
import order_service.com.order_service.dto.request.OrderRequestDto;
import order_service.com.order_service.dto.response.OrderItemResponseDto;
import order_service.com.order_service.dto.response.OrderPageResponseDto;
import order_service.com.order_service.dto.response.OrderResponseDto;
import order_service.com.order_service.entity.Order;
import order_service.com.order_service.entity.OrderItem;
import order_service.com.order_service.enums.OrderStatus;
import order_service.com.order_service.enums.PaymentMethod;
import order_service.com.order_service.enums.PaymentStatus;
import order_service.com.order_service.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final RestaurantServiceClient restaurantServiceClient;
    private final OrderRepository orderRepository;
    private final PaymentServiceClient paymentServiceClient;

    @Transactional(rollbackOn = Exception.class)
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto, Long id) {
        if(!orderRequestDto.getUserId().equals(id)){
            throw new RuntimeException("Invalid User");
        }

        Order order = mapToOrder(orderRequestDto);
        List<OrderItem> orderItems = createOrderItem(orderRequestDto.getItems(), order);

        double totalPrice = 0.0;
        for (OrderItem item : orderItems) {
            totalPrice += (item.getPriceAtTimeOfOrder() * item.getQuantity());
        }
        order.setTotalPrice(totalPrice);
        order.setListOrderItems(orderItems);
        order.setUserId(id);

        order.setOrderStatus(OrderStatus.PENDING);


        Order savedOrder = orderRepository.save(order);

        OrderResponseDto orderResponseDto = mapOrderToResponse(savedOrder);

        if(savedOrder.getPaymentMethod().equals(PaymentMethod.COD)) {

            savedOrder.setOrderStatus(OrderStatus.CONFIRMED);
            orderRepository.save(savedOrder); // Status change kiya toh wapas update karke save kar do

            orderResponseDto.setMsg("Order placed successfully with Cash on Delivery.");

        } else {
            PaymentCreateClientResponseDto responseDto = paymentServiceClient.createPaymentRequest(
                    createPaymentClientRequestDto(savedOrder)
            );

            orderResponseDto.setRazorpayOrderId(responseDto.getRazorpayOrderId());
            orderResponseDto.setPaymentStatus(responseDto.getPaymentStatus());
            orderResponseDto.setMsg(responseDto.getMessage());
        }

        return orderResponseDto;
    }

    public OrderResponseDto getOrderById(Long orderId,Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Error: Order not found with ID: " + orderId));
        OrderResponseDto orderResponseDto = mapOrderToResponse(order);
        if(!order.getOrderStatus().equals(OrderStatus.DELIVERED) && order.getUserId().equals(userId))
        {
            orderResponseDto.setDeliveryOtp(order.getDeliveryOtp());
        }
        return orderResponseDto;
    }

    public OrderPageResponseDto getAllOrders(int pageNumber, int pageSize,Long userId) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);

        List<OrderResponseDto> orderDtos = new ArrayList<>();

        for (Order order : orderPage.getContent()) {
            OrderResponseDto dto = mapOrderToResponse(order);
            orderDtos.add(dto);
        }

        return OrderPageResponseDto.builder()
                .content(orderDtos)
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .isLastPage(orderPage.isLast())
                .build();
    }

    public OrderResponseDto updateOrderStatus(Long orderId, OrderStatus newStatus,Long userId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Error: Order not found with ID: " + orderId));
        RestaurantClientDto dto=restaurantServiceClient.getRestaurantById(order.getRestaurantId());
        if(!dto.getOwnerId().equals(userId)){
            throw new RuntimeException("Invalid User");
        }
        order.setOrderStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        return mapOrderToResponse(updatedOrder);
    }

    public void deleteOrder(Long orderId,Long userId) {

        Order order=orderRepository.findById(orderId).
                orElseThrow(()->new RuntimeException("Error: Order not found with ID: " + orderId));
        if(!order.getUserId().equals(userId)){
            throw new RuntimeException("Invalid details");
        }
        orderRepository.deleteById(orderId);
    }

    private PaymentCreateClientRequestDto createPaymentClientRequestDto(Order order)
    {
        return PaymentCreateClientRequestDto.builder().amount(order.getTotalPrice()).orderId(order.getId()).build();
    }
    private List<OrderItem> createOrderItem(List<OrderItemRequestDto> list, Order order) {
        List<OrderItem> listOrderItem = new ArrayList<>();
        for(OrderItemRequestDto orderItemRequestDto : list) {
            listOrderItem.add(mapDtoToOrderItem(orderItemRequestDto, order));
        }
        return listOrderItem;
    }

    private OrderItem mapDtoToOrderItem(OrderItemRequestDto requestDto, Order order) {
        Long menuItemId = requestDto.getMenuItemId();
        MenuItemClientDto menuItemClientDto = restaurantServiceClient.getMenuItemById(menuItemId);


        if (menuItemClientDto.getId() == 0L) {
            throw new RuntimeException("Server Error: Unable to fetch details for Item ID " + menuItemId);
        }

        if (!Boolean.TRUE.equals(menuItemClientDto.getAvailable())) {
            throw new RuntimeException("Sorry! Item '" + menuItemClientDto.getName() + "' is currently out of stock.");
        }

        if (!menuItemClientDto.getRestaurantId().equals(order.getRestaurantId())) {
            throw new RuntimeException("Security Alert! Item '" + menuItemClientDto.getName() + "' does not belong to the selected restaurant.");
        }

        return OrderItem.builder()
                .menuItemId(menuItemId)
                .priceAtTimeOfOrder(menuItemClientDto.getPrice())
                .quantity(requestDto.getQuantity())
                .order(order)
                .build();
    }

    private Order mapToOrder(OrderRequestDto requestDto) {
        Long restaurantId = requestDto.getRestaurantId();
        RestaurantClientDto restaurantDto = restaurantServiceClient.getRestaurantById(restaurantId);

        if (restaurantDto.getId() == 0L) {
            throw new RuntimeException("Server Error: Details for Restaurant ID " + restaurantId + " not found.");
        }

        if (!Boolean.TRUE.equals(restaurantDto.getOpen())) {
            throw new RuntimeException("Sorry! Restaurant '" + restaurantDto.getName() + "' is currently closed. Order cannot be placed.");
        }

        return Order.builder()
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(requestDto.getPaymentStatus())
                .restaurantId(restaurantId)
                .createdAt(LocalDateTime.now())
                .paymentMethod(requestDto.getPaymentMethod())
                .deliveryAddress(requestDto.getDeliveryAddress())
                .deliveryOtp(generateDeliveryOtp())
                .build();
    }

    private OrderResponseDto mapOrderToResponse(Order order) {
        List<OrderItemResponseDto> itemDtos = new ArrayList<>();
        if (order.getListOrderItems() != null) {
            for (OrderItem item : order.getListOrderItems()) {
                itemDtos.add(OrderItemResponseDto.builder()
                        .id(item.getId())
                        .menuItemId(item.getMenuItemId())
                        .priceAtTimeOfOrder(item.getPriceAtTimeOfOrder())
                        .quantity(item.getQuantity())
                        .build());
            }
        }

        return OrderResponseDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .restaurantId(order.getRestaurantId())
                .totalPrice(order.getTotalPrice())
                .deliveryAddress(order.getDeliveryAddress())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .orderStatus(order.getOrderStatus())
                .createdAt(order.getCreatedAt())
                .items(itemDtos)
                .build();
    }

    private String generateDeliveryOtp() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000);
        return String.valueOf(otp);
    }

    public OrderResponseDto updatePaymentStatusToSuccess(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Error: Order not found with ID: " + orderId));

        if(!order.getUserId().equals(userId)){
            throw new RuntimeException("Security Alert: Invalid User trying to update payment");
        }
        order.setPaymentStatus(PaymentStatus.COMPLETED);
        order.setOrderStatus(OrderStatus.CONFIRMED);
        Order updatedOrder = orderRepository.save(order);

        return mapOrderToResponse(updatedOrder);
    }

    public OrderResponseDto verifyDeliveryAndCompleteOrder(Long orderId, String submittedOtp, Long deliveryPartnerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Error: Order not found"));

        if (order.getOrderStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            throw new RuntimeException("Error: Order is not ready for delivery yet.");
        }
        if (!order.getDeliveryOtp().equals(submittedOtp)) {
            throw new RuntimeException("Access Denied: Invalid OTP. Do not hand over the food!");
        }

        order.setOrderStatus(OrderStatus.DELIVERED);
        Order completedOrder = orderRepository.save(order);
        return mapOrderToResponse(completedOrder);
    }
}