package ru.yandex.practicum.commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProductToWarehouseRequest {
    private UUID productId;
    private long quantity;
}
