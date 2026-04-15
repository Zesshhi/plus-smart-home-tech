package ru.yandex.practicum.commerce.warehouse.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "warehouse_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseProduct {

    @Id
    private UUID productId;

    private boolean fragile;

    private double width;
    private double height;
    private double depth;
    private double weight;

    @Builder.Default
    private long quantity = 0;
}
