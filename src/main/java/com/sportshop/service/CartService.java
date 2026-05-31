package com.sportshop.service;

import com.sportshop.models.CartItem;
import com.sportshop.repository.CartRepository;

import java.math.BigDecimal;
import java.util.List;

public class CartService {

    private final CartRepository repo = new CartRepository();

    public List<CartItem> getCart(int userId) {
        return repo.getCart(userId);
    }

    public boolean add(int userId, int productId) {
        return repo.addToCart(userId, productId);
    }

    public void remove(int userId, int productId) {
        repo.remove(userId, productId);
    }

    public boolean update(int userId,
                          int productId,
                          int quantity) {

        return repo.updateQuantity(
                userId,
                productId,
                quantity
        );
    }

    public void clearCart(int userId) {
        repo.clearCart(userId);
    }

    public BigDecimal calculateTotal(List<CartItem> items) {

        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .map(CartItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}