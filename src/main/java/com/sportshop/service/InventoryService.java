package com.sportshop.service;

import com.sportshop.models.Inventory;
import com.sportshop.repository.InventoryRepository;

public class InventoryService {

    private final InventoryRepository repo =
            new InventoryRepository();

    public Inventory getInventory(int productId) {
        return repo.findByProductId(productId);
    }

    public void saveOrUpdate(int productId, int quantity) {

        if (quantity < 0) {
            quantity = 0;
        }

        Inventory inventory =
                repo.findByProductId(productId);

        if (inventory == null) {
            repo.create(productId, quantity);
        } else {
            repo.updateQuantity(productId, quantity);
        }
    }

    public void addHistory(int productId,
                           int changeAmount,
                           String changeType) {

        repo.addHistory(
                productId,
                changeAmount,
                changeType
        );
    }
}