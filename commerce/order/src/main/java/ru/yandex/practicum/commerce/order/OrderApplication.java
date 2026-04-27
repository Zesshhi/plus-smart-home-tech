package ru.yandex.practicum.commerce.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.yandex.practicum.commerce.client.DeliveryClient;
import ru.yandex.practicum.commerce.client.PaymentClient;
import ru.yandex.practicum.commerce.client.ShoppingCartClient;
import ru.yandex.practicum.commerce.client.WarehouseClient;

@SpringBootApplication
@EnableFeignClients(clients = {WarehouseClient.class, ShoppingCartClient.class, DeliveryClient.class, PaymentClient.class})
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
