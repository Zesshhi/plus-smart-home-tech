package ru.yandex.practicum.commerce.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.dto.*;
import ru.yandex.practicum.commerce.store.service.ShoppingStoreService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class ShoppingStoreController {

    private final ShoppingStoreService shoppingStoreService;

    @GetMapping
    public Page<ProductDto> getProducts(@RequestParam ProductCategory category,
                                        Pageable pageable) {
        return shoppingStoreService.getProducts(category, pageable);
    }

    @GetMapping("/{productId}")
    public ProductDto getProduct(@PathVariable UUID productId) {
        return shoppingStoreService.getProduct(productId);
    }

    @PutMapping
    public ProductDto createProduct(@RequestBody ProductDto productDto) {
        return shoppingStoreService.createProduct(productDto);
    }

    @PostMapping
    public ProductDto updateProduct(@RequestBody ProductDto productDto) {
        return shoppingStoreService.updateProduct(productDto);
    }

    @PostMapping("/removeProductFromStore")
    public boolean removeProductFromStore(@RequestBody UUID productId) {
        return shoppingStoreService.removeProduct(productId);
    }

    @PostMapping("/quantityState")
    public boolean setQuantityState(@RequestBody SetProductQuantityStateRequest request) {
        return shoppingStoreService.setQuantityState(request);
    }
}
