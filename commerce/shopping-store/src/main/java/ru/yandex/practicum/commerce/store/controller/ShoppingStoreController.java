package ru.yandex.practicum.commerce.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.dto.*;
import ru.yandex.practicum.commerce.store.service.ShoppingStoreService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class ShoppingStoreController implements ShoppingStoreClient {

    private final ShoppingStoreService shoppingStoreService;

    @Override
    @GetMapping
    public List<ProductDto> getProducts(@RequestParam ProductCategory category,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return shoppingStoreService.getProducts(category, page, size);
    }

    @Override
    @GetMapping("/{productId}")
    public ProductDto getProduct(@PathVariable UUID productId) {
        return shoppingStoreService.getProduct(productId);
    }

    @Override
    @PostMapping
    public ProductDto createProduct(@RequestBody ProductDto productDto) {
        return shoppingStoreService.createProduct(productDto);
    }

    @Override
    @PutMapping
    public ProductDto updateProduct(@RequestBody ProductDto productDto) {
        return shoppingStoreService.updateProduct(productDto);
    }

    @Override
    @PostMapping("/removeProductFromStore")
    public boolean removeProductFromStore(@RequestBody UUID productId) {
        return shoppingStoreService.removeProduct(productId);
    }

    @Override
    @PostMapping("/quantityState")
    public boolean setQuantityState(@RequestBody SetProductQuantityStateRequest request) {
        return shoppingStoreService.setQuantityState(request);
    }
}
