package com.sportshop.service;

import com.sportshop.models.Product;
import com.sportshop.repository.RecommendationRepository;

import java.util.List;

public class RecommendationService {

    private final RecommendationRepository repository =
            new RecommendationRepository();

    private final ProductService productService =
            new ProductService();

    public void recordProductView(Integer userId,
                                  int productId) {

        if (userId == null || productId <= 0) {
            return;
        }

        repository.recordView(userId, productId);
    }

    public List<Product> getRecommendations(int userId,
                                            int limit) {

        return productService.getRecommendedProducts(
                userId,
                limit
        );
    }

    public List<Product> getRecentlyViewed(int userId,
                                           int limit) {

        return productService.getRecentlyViewedProducts(
                userId,
                limit
        );
    }

    public List<Product> getPopularProducts(int limit) {
        return productService.getPopularProducts(limit);
    }
}