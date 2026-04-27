package ru.yandex.practicum.commerce.delivery.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.commerce.dto.DeliveryState;

import java.util.UUID;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID deliveryId;

    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeliveryState deliveryState = DeliveryState.CREATED;

    private String fromCountry;
    private String fromCity;
    private String fromStreet;
    private String fromHouse;
    private String fromFlat;

    private String toCountry;
    private String toCity;
    private String toStreet;
    private String toHouse;
    private String toFlat;
}
