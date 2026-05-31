package com.sportshop.service;

import com.sportshop.models.Product;
import com.sportshop.repository.ProductRepository;

import java.util.List;

public class ProductService {

    private final ProductRepository productRepository =
            new ProductRepository();

    public List<Product> getAllProducts() {

        return productRepository.searchWithFilters(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                1000,
                0
        );
    }

    public int getProductsCount() {
        return productRepository.countAll();
    }

    public Product getProduct(int id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByIds(List<Integer> ids) {
        return productRepository.findProductsByIds(ids);
    }

    public List<Product> searchWithFilters(String query,
                                           String brand,
                                           Double minPrice,
                                           Double maxPrice,
                                           Integer categoryId,
                                           Double minRating,
                                           String sort,
                                           int limit,
                                           int offset) {

        return productRepository.searchWithFilters(
                query,
                brand,
                minPrice,
                maxPrice,
                categoryId,
                minRating,
                sort,
                limit,
                offset
        );
    }

    public int countWithFilters(String query,
                                String brand,
                                Double minPrice,
                                Double maxPrice,
                                Integer categoryId,
                                Double minRating) {

        return productRepository.countWithFilters(
                query,
                brand,
                minPrice,
                maxPrice,
                categoryId,
                minRating
        );
    }

    public List<Product> getRelatedProducts(Product product,
                                            int limit) {

        return productRepository.findRelatedProducts(
                product,
                limit
        );
    }

    public List<Product> getPopularProducts(int limit) {
        return productRepository.findPopularProducts(limit);
    }

    public List<Product> getRecommendedProducts(int userId,
                                                int limit) {

        return productRepository.findRecommendedProducts(
                userId,
                limit
        );
    }

    public List<Product> getRecentlyViewedProducts(int userId,
                                                   int limit) {

        return productRepository.findRecentlyViewedProducts(
                userId,
                limit
        );
    }

    public int create(Product product) {
        return productRepository.create(product);
    }

    public void update(Product product) {
        productRepository.update(product);
    }

    public void delete(int id) {
        productRepository.delete(id);
    }
}