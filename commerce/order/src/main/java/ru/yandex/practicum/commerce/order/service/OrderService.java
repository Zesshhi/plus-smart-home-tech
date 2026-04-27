package ru.yandex.practicum.commerce.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.client.DeliveryClient;
import ru.yandex.practicum.commerce.client.PaymentClient;
import ru.yandex.practicum.commerce.client.WarehouseClient;
import ru.yandex.practicum.commerce.dto.*;
import ru.yandex.practicum.commerce.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.commerce.order.model.Order;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WarehouseClient warehouseClient;
    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;

    @Transactional
    public OrderDto createNewOrder(CreateNewOrderRequest request) {
        ShoppingCartDto cart = request.getShoppingCart();
        AddressDto address = request.getDeliveryAddress();

        BookedProductsDto booked;
        try {
            booked = warehouseClient.checkProductAvailabilityForCart(cart);
        } catch (Exception e) {
            log.error("Ошибка проверки наличия товаров на складе", e);
            throw new NoSpecifiedProductInWarehouseException("Нет заказываемого товара на складе");
        }

        Order order = Order.builder()
                .shoppingCartId(cart.getShoppingCartId())
                .products(cart.getProducts())
                .state(OrderState.NEW)
                .deliveryWeight(booked.getDeliveryWeight())
                .deliveryVolume(booked.getDeliveryVolume())
                .fragile(booked.isFragile())
                .deliveryCountry(address.getCountry())
                .deliveryCity(address.getCity())
                .deliveryStreet(address.getStreet())
                .deliveryHouse(address.getHouse())
                .deliveryFlat(address.getFlat())
                .build();

        order = orderRepository.save(order);

        AddressDto warehouseAddress = warehouseClient.getWarehouseAddress();

        DeliveryDto deliveryDto = DeliveryDto.builder()
                .orderId(order.getOrderId())
                .fromAddress(warehouseAddress)
                .toAddress(address)
                .deliveryState(DeliveryState.CREATED)
                .build();
        DeliveryDto createdDelivery = deliveryClient.planDelivery(deliveryDto);
        order.setDeliveryId(createdDelivery.getDeliveryId());
        order = orderRepository.save(order);

        return toDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username) {
        return orderRepository.findByUsername(username).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public OrderDto payment(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.ON_PAYMENT);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.PAYMENT_FAILED);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto delivery(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.DELIVERED);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.DELIVERY_FAILED);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto complete(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.COMPLETED);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        Order order = findOrder(orderId);
        OrderDto orderDto = toDto(order);

        BigDecimal totalCost = paymentClient.getTotalCost(orderDto);
        order.setTotalPrice(totalCost);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        Order order = findOrder(orderId);
        OrderDto orderDto = toDto(order);

        BigDecimal deliveryCost = deliveryClient.deliveryCost(orderDto);
        order.setDeliveryPrice(deliveryCost);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto assembly(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.ASSEMBLED);

        AssemblyProductsForOrderRequest assemblyRequest = AssemblyProductsForOrderRequest.builder()
                .orderId(orderId)
                .products(order.getProducts())
                .build();
        warehouseClient.assemblyProductsForOrder(assemblyRequest);

        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        Order order = findOrder(orderId);
        order.setState(OrderState.ASSEMBLY_FAILED);
        return toDto(orderRepository.save(order));
    }

    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        Order order = findOrder(request.getOrderId());
        order.setState(OrderState.PRODUCT_RETURNED);

        warehouseClient.acceptReturn(request.getProducts());

        return toDto(orderRepository.save(order));
    }

    private Order findOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoOrderFoundException("Заказ не найден: " + orderId));
    }

    private OrderDto toDto(Order order) {
        return OrderDto.builder()
                .orderId(order.getOrderId())
                .shoppingCartId(order.getShoppingCartId())
                .products(order.getProducts())
                .paymentId(order.getPaymentId())
                .deliveryId(order.getDeliveryId())
                .state(order.getState())
                .deliveryWeight(order.getDeliveryWeight())
                .deliveryVolume(order.getDeliveryVolume())
                .fragile(order.isFragile())
                .totalPrice(order.getTotalPrice())
                .deliveryPrice(order.getDeliveryPrice())
                .productPrice(order.getProductPrice())
                .build();
    }
}
