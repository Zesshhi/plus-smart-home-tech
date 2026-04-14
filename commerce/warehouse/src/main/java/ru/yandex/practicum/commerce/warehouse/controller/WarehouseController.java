package ru.yandex.practicum.commerce.warehouse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.client.WarehouseClient;
import ru.yandex.practicum.commerce.dto.*;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

@RestController
@RequestMapping("/api/v1/warehouse")
@RequiredArgsConstructor
public class WarehouseController implements WarehouseClient {

    private final WarehouseService warehouseService;

    @Override
    @PutMapping
    public void newProductInWarehouse(@RequestBody NewProductInWarehouseRequest request) {
        warehouseService.newProduct(request);
    }

    @Override
    @PostMapping("/add")
    public void addProductToWarehouse(@RequestBody AddProductToWarehouseRequest request) {
        warehouseService.addProduct(request);
    }

    @Override
    @PostMapping("/check")
    public BookedProductsDto checkProductAvailabilityForCart(@RequestBody ShoppingCartDto shoppingCartDto) {
        return warehouseService.checkAvailability(shoppingCartDto);
    }

    @Override
    @GetMapping("/address")
    public AddressDto getWarehouseAddress() {
        return warehouseService.getAddress();
    }
}
