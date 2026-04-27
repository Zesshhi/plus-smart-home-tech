package ru.yandex.practicum.commerce.order.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.commerce.dto.OrderState;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    private UUID shoppingCartId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    @Builder.Default
    private Map<UUID, Long> products = new HashMap<>();

    private UUID paymentId;

    private UUID deliveryId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderState state = OrderState.NEW;

    private double deliveryWeight;

    private double deliveryVolume;

    private boolean fragile;

    @Builder.Default
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal deliveryPrice = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal productPrice = BigDecimal.ZERO;

    private String username;

    @Column(length = 500)
    private String deliveryCountry;

    private String deliveryCity;

    private String deliveryStreet;

    private String deliveryHouse;

    private String deliveryFlat;
}
