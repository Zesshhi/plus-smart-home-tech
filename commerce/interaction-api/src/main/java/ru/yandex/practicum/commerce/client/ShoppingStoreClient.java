package ru.yandex.practicum.commerce.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.dto.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
public interface ShoppingStoreClient {

    @GetMapping
    List<ProductDto> getProducts(@RequestParam ProductCategory category,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size);

    @GetMapping("/{productId}")
    ProductDto getProduct(@PathVariable UUID productId);

    @PostMapping
    ProductDto createProduct(@RequestBody ProductDto productDto);

    @PutMapping
    ProductDto updateProduct(@RequestBody ProductDto productDto);

    @DeleteMapping("/{productId}")
    boolean removeProductFromStore(@PathVariable UUID productId);

    @PostMapping("/quantityState")
    boolean setQuantityState(@RequestBody SetProductQuantityStateRequest request);
}
