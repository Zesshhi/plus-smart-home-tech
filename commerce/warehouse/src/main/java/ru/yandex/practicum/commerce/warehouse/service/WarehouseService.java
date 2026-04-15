package ru.yandex.practicum.commerce.warehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.dto.*;
import ru.yandex.practicum.commerce.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.commerce.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {

    private static final String[] ADDRESSES = new String[]{"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    private final WarehouseProductRepository warehouseProductRepository;
    private final ShoppingStoreClient shoppingStoreClient;

    @Transactional
    public void newProduct(NewProductInWarehouseRequest request) {
        if (warehouseProductRepository.existsById(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException(
                    "Product already exists in warehouse: " + request.getProductId());
        }

        WarehouseProduct product = WarehouseProduct.builder()
                .productId(request.getProductId())
                .fragile(request.isFragile())
                .width(request.getDimension().getWidth())
                .height(request.getDimension().getHeight())
                .depth(request.getDimension().getDepth())
                .weight(request.getWeight())
                .build();

        warehouseProductRepository.save(product);
    }

    @Transactional
    public void addProduct(AddProductToWarehouseRequest request) {
        WarehouseProduct product = findProduct(request.getProductId());
        product.setQuantity(product.getQuantity() + request.getQuantity());
        warehouseProductRepository.save(product);

        updateQuantityStateInStore(product);
    }

    @Transactional(readOnly = true)
    public BookedProductsDto checkAvailability(ShoppingCartDto cart) {
        double totalWeight = 0;
        double totalVolume = 0;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : cart.getProducts().entrySet()) {
            UUID productId = entry.getKey();
            long requestedQuantity = entry.getValue();

            WarehouseProduct product = findProduct(productId);

            if (product.getQuantity() < requestedQuantity) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Not enough stock for product: " + productId);
            }

            totalWeight += product.getWeight() * requestedQuantity;
            totalVolume += product.getWidth() * product.getHeight() * product.getDepth() * requestedQuantity;
            if (product.isFragile()) {
                fragile = true;
            }
        }

        return BookedProductsDto.builder()
                .deliveryWeight(totalWeight)
                .deliveryVolume(totalVolume)
                .fragile(fragile)
                .build();
    }

    public AddressDto getAddress() {
        return AddressDto.builder()
                .country(CURRENT_ADDRESS)
                .city(CURRENT_ADDRESS)
                .street(CURRENT_ADDRESS)
                .house(CURRENT_ADDRESS)
                .flat(CURRENT_ADDRESS)
                .build();
    }

    private WarehouseProduct findProduct(UUID productId) {
        return warehouseProductRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found in warehouse: " + productId));
    }

    private void updateQuantityStateInStore(WarehouseProduct product) {
        QuantityState state;
        long qty = product.getQuantity();

        if (qty == 0) {
            state = QuantityState.ENDED;
        } else if (qty < 10) {
            state = QuantityState.FEW;
        } else if (qty <= 100) {
            state = QuantityState.ENOUGH;
        } else {
            state = QuantityState.MANY;
        }

        try {
            shoppingStoreClient.setQuantityState(product.getProductId(), state);
        } catch (Exception e) {
            log.error("Failed to update quantity state in store for product {}: {}",
                    product.getProductId(), e.getMessage());
        }
    }
}
