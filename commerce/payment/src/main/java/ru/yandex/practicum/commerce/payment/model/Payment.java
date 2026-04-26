package ru.yandex.practicum.commerce.payment.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.commerce.dto.PaymentState;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;

    private UUID orderId;

    @Builder.Default
    private BigDecimal productTotal = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal deliveryTotal = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal feeTotal = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalPayment = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentState state = PaymentState.PENDING;
}
