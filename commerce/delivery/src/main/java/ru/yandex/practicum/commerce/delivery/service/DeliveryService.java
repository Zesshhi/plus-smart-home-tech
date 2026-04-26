package ru.yandex.practicum.commerce.delivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.client.OrderClient;
import ru.yandex.practicum.commerce.client.WarehouseClient;
import ru.yandex.practicum.commerce.delivery.model.Delivery;
import ru.yandex.practicum.commerce.delivery.repository.DeliveryRepository;
import ru.yandex.practicum.commerce.dto.*;
import ru.yandex.practicum.commerce.exception.NoDeliveryFoundException;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryService {

    private static final double BASE_COST = 5.0;

    private final DeliveryRepository deliveryRepository;
    private final OrderClient orderClient;
    private final WarehouseClient warehouseClient;

    @Transactional
    public DeliveryDto planDelivery(DeliveryDto deliveryDto) {
        Delivery delivery = Delivery.builder()
                .orderId(deliveryDto.getOrderId())
                .deliveryState(DeliveryState.CREATED)
                .fromCountry(deliveryDto.getFromAddress().getCountry())
                .fromCity(deliveryDto.getFromAddress().getCity())
                .fromStreet(deliveryDto.getFromAddress().getStreet())
                .fromHouse(deliveryDto.getFromAddress().getHouse())
                .fromFlat(deliveryDto.getFromAddress().getFlat())
                .toCountry(deliveryDto.getToAddress().getCountry())
                .toCity(deliveryDto.getToAddress().getCity())
                .toStreet(deliveryDto.getToAddress().getStreet())
                .toHouse(deliveryDto.getToAddress().getHouse())
                .toFlat(deliveryDto.getToAddress().getFlat())
                .build();

        delivery = deliveryRepository.save(delivery);
        return toDto(delivery);
    }

    @Transactional
    public void deliverySuccessful(UUID orderId) {
        Delivery delivery = findByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);

        orderClient.delivery(orderId);
    }

    @Transactional
    public void deliveryPicked(UUID orderId) {
        Delivery delivery = findByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);
        deliveryRepository.save(delivery);

        orderClient.assembly(orderId);

        ShippedToDeliveryRequest shippedRequest = ShippedToDeliveryRequest.builder()
                .orderId(orderId)
                .deliveryId(delivery.getDeliveryId())
                .build();
        warehouseClient.shippedToDelivery(shippedRequest);
    }

    @Transactional
    public void deliveryFailed(UUID orderId) {
        Delivery delivery = findByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.FAILED);
        deliveryRepository.save(delivery);

        orderClient.deliveryFailed(orderId);
    }

    @Transactional(readOnly = true)
    public BigDecimal deliveryCost(OrderDto orderDto) {
        Delivery delivery = findByOrderId(orderDto.getOrderId());

        String fromStreet = delivery.getFromStreet();
        String toStreet = delivery.getToStreet();

        double cost = BASE_COST;

        if (fromStreet != null && fromStreet.contains("ADDRESS_2")) {
            cost *= 2;
        }

        cost += BASE_COST;

        if (orderDto.isFragile()) {
            cost += cost * 0.2;
        }

        cost += orderDto.getDeliveryWeight() * 0.3;

        cost += orderDto.getDeliveryVolume() * 0.2;

        if (toStreet == null || !toStreet.equals(fromStreet)) {
            cost += cost * 0.2;
        }

        return BigDecimal.valueOf(cost);
    }

    private Delivery findByOrderId(UUID orderId) {
        return deliveryRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NoDeliveryFoundException("Доставка не найдена для заказа: " + orderId));
    }

    private DeliveryDto toDto(Delivery delivery) {
        return DeliveryDto.builder()
                .deliveryId(delivery.getDeliveryId())
                .orderId(delivery.getOrderId())
                .deliveryState(delivery.getDeliveryState())
                .fromAddress(AddressDto.builder()
                        .country(delivery.getFromCountry())
                        .city(delivery.getFromCity())
                        .street(delivery.getFromStreet())
                        .house(delivery.getFromHouse())
                        .flat(delivery.getFromFlat())
                        .build())
                .toAddress(AddressDto.builder()
                        .country(delivery.getToCountry())
                        .city(delivery.getToCity())
                        .street(delivery.getToStreet())
                        .house(delivery.getToHouse())
                        .flat(delivery.getToFlat())
                        .build())
                .build();
    }
}
