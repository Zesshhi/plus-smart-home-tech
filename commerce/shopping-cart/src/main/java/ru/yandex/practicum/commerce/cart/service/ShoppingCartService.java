package ru.yandex.practicum.commerce.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.cart.model.ShoppingCart;
import ru.yandex.practicum.commerce.cart.repository.ShoppingCartRepository;
import ru.yandex.practicum.commerce.client.WarehouseClient;
import ru.yandex.practicum.commerce.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.exception.NoProductsInShoppingCartException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingCartService {

    private final ShoppingCartRepository cartRepository;
    private final WarehouseClient warehouseClient;

    @Transactional(readOnly = true)
    public ShoppingCartDto getShoppingCart(String username) {

        ShoppingCart cart = getOrCreateCart(username);
        return toDto(cart);
    }

    @Transactional
    public ShoppingCartDto addProducts(String username, Map<UUID, Long> products) {

        ShoppingCart cart = getOrCreateCart(username);

        cart.getProducts().putAll(products);
        cartRepository.save(cart);

        ShoppingCartDto dto = toDto(cart);
        warehouseClient.checkProductAvailabilityForCart(dto);

        return dto;
    }

    @Transactional
    public void deactivateCart(String username) {

        ShoppingCart cart = getOrCreateCart(username);
        cart.setActive(false);
        cartRepository.save(cart);
    }

    @Transactional
    public ShoppingCartDto removeProducts(String username, List<UUID> productIds) {

        ShoppingCart cart = getOrCreateCart(username);

        boolean anyFound = productIds.stream().anyMatch(id -> cart.getProducts().containsKey(id));
        if (!anyFound) {
            throw new NoProductsInShoppingCartException("No specified products found in cart");
        }

        productIds.forEach(cart.getProducts()::remove);
        cartRepository.save(cart);
        return toDto(cart);
    }

    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {

        ShoppingCart cart = getOrCreateCart(username);

        if (!cart.getProducts().containsKey(request.getProductId())) {
            throw new NoProductsInShoppingCartException("Product not found in cart: " + request.getProductId());
        }

        cart.getProducts().put(request.getProductId(), request.getNewQuantity());
        cartRepository.save(cart);
        return toDto(cart);
    }

    private ShoppingCart getOrCreateCart(String username) {
        return cartRepository.findByUsernameAndActiveTrue(username)
                .orElseGet(() -> cartRepository.save(ShoppingCart.builder()
                        .username(username)
                        .build()));
    }
    private ShoppingCartDto toDto(ShoppingCart cart) {
        return ShoppingCartDto.builder()
                .shoppingCartId(cart.getShoppingCartId())
                .products(cart.getProducts())
                .build();
    }
}
