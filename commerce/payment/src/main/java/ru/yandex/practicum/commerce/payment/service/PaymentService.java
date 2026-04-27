package ru.yandex.practicum.commerce.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.client.OrderClient;
import ru.yandex.practicum.commerce.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.dto.OrderDto;
import ru.yandex.practicum.commerce.dto.PaymentDto;
import ru.yandex.practicum.commerce.dto.PaymentState;
import ru.yandex.practicum.commerce.dto.ProductDto;
import ru.yandex.practicum.commerce.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.exception.NotEnoughInfoInOrderToCalculateException;
import ru.yandex.practicum.commerce.payment.model.Payment;
import ru.yandex.practicum.commerce.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final ShoppingStoreClient shoppingStoreClient;

    @Transactional(readOnly = true)
    public BigDecimal productCost(OrderDto orderDto) {
        validateOrderForCalculation(orderDto);
        return calculateProductCost(orderDto.getProducts());
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCost(OrderDto orderDto) {
        validateOrderForCalculation(orderDto);

        BigDecimal productCost = calculateProductCost(orderDto.getProducts());
        BigDecimal fee = productCost.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal productWithFee = productCost.add(fee);
        BigDecimal deliveryPrice = orderDto.getDeliveryPrice() != null ? orderDto.getDeliveryPrice() : BigDecimal.ZERO;

        return productWithFee.add(deliveryPrice);
    }

    @Transactional
    public PaymentDto payment(OrderDto orderDto) {
        validateOrderForCalculation(orderDto);

        BigDecimal productCost = calculateProductCost(orderDto.getProducts());
        BigDecimal fee = productCost.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal productWithFee = productCost.add(fee);
        BigDecimal deliveryPrice = orderDto.getDeliveryPrice() != null ? orderDto.getDeliveryPrice() : BigDecimal.ZERO;
        BigDecimal totalPayment = productWithFee.add(deliveryPrice);

        Payment payment = Payment.builder()
                .orderId(orderDto.getOrderId())
                .productTotal(productCost)
                .deliveryTotal(deliveryPrice)
                .feeTotal(fee)
                .totalPayment(totalPayment)
                .state(PaymentState.PENDING)
                .build();

        payment = paymentRepository.save(payment);
        return toDto(payment);
    }

    @Transactional
    public void paymentSuccess(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Платёж не найден: " + paymentId));

        payment.setState(PaymentState.SUCCESS);
        paymentRepository.save(payment);

        orderClient.payment(payment.getOrderId());
    }

    @Transactional
    public void paymentFailed(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoOrderFoundException("Платёж не найден: " + paymentId));

        payment.setState(PaymentState.FAILED);
        paymentRepository.save(payment);

        orderClient.paymentFailed(payment.getOrderId());
    }

    private BigDecimal calculateProductCost(Map<UUID, Long> products) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            ProductDto product = shoppingStoreClient.getProduct(entry.getKey());
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(entry.getValue()));
            total = total.add(itemTotal);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateOrderForCalculation(OrderDto orderDto) {
        if (orderDto.getProducts() == null || orderDto.getProducts().isEmpty()) {
            throw new NotEnoughInfoInOrderToCalculateException("Заказ не содержит товаров для расчёта");
        }
    }

    private PaymentDto toDto(Payment payment) {
        return PaymentDto.builder()
                .paymentId(payment.getPaymentId())
                .totalPayment(payment.getTotalPayment())
                .deliveryTotal(payment.getDeliveryTotal())
                .feeTotal(payment.getFeeTotal())
                .build();
    }
}
