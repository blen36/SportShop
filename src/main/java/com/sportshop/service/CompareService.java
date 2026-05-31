package com.sportshop.service;

import com.sportshop.models.Product;
import com.sportshop.repository.ComparisonRepository;

import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompareService {

    private static final String SESSION_KEY = "compareIds";

    private final ComparisonRepository repository =
            new ComparisonRepository();

    private final ProductService productService =
            new ProductService();

    public List<Product> getProducts(Integer userId,
                                     HttpSession session) {

        List<Integer> ids = getIds(userId, session);

        return productService.getProductsByIds(ids);
    }

    public List<Integer> getIds(Integer userId,
                                HttpSession session) {

        if (userId != null) {
            return repository.findProductIds(userId);
        }

        return getSessionIds(session);
    }

    public boolean add(Integer userId,
                       HttpSession session,
                       int productId) {

        if (productId <= 0) {
            return false;
        }

        if (userId != null) {
            return repository.addProduct(userId, productId);
        }

        List<Integer> ids = getSessionIds(session);

        if (ids.contains(productId)) {
            return true;
        }

        if (ids.size() >= 5) {
            return false;
        }

        ids.add(0, productId);
        session.setAttribute(SESSION_KEY, ids);

        return true;
    }

    public void remove(Integer userId,
                       HttpSession session,
                       int productId) {

        if (userId != null) {

            repository.removeProduct(
                    userId,
                    productId
            );

            return;
        }

        List<Integer> ids = getSessionIds(session);
        ids.remove(Integer.valueOf(productId));

        session.setAttribute(SESSION_KEY, ids);
    }

    public void clear(Integer userId,
                      HttpSession session) {

        if (userId != null) {
            repository.clear(userId);
            return;
        }

        session.removeAttribute(SESSION_KEY);
    }

    public boolean hasDifferentBrands(List<Product> products) {

        if (products == null || products.size() < 2) {
            return false;
        }

        String first = normalize(products.get(0).getBrand());

        for (Product product : products) {

            if (!Objects.equals(
                    first,
                    normalize(product.getBrand())
            )) {

                return true;
            }
        }

        return false;
    }

    public boolean hasDifferentPrices(List<Product> products) {

        if (products == null || products.size() < 2) {
            return false;
        }

        BigDecimal first = products.get(0).getPrice();

        for (Product product : products) {

            if (first == null && product.getPrice() != null) {
                return true;
            }

            if (first != null &&
                    product.getPrice() != null &&
                    first.compareTo(product.getPrice()) != 0) {

                return true;
            }
        }

        return false;
    }

    public boolean hasDifferentStock(List<Product> products) {

        if (products == null || products.size() < 2) {
            return false;
        }

        int first = products.get(0).getStock();

        for (Product product : products) {

            if (first != product.getStock()) {
                return true;
            }
        }

        return false;
    }

    public boolean hasDifferentAttributes(List<Product> products) {

        if (products == null || products.size() < 2) {
            return false;
        }

        String first = normalize(products.get(0).getAttributes());

        for (Product product : products) {

            if (!Objects.equals(
                    first,
                    normalize(product.getAttributes())
            )) {

                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    private List<Integer> getSessionIds(HttpSession session) {

        Object value = session.getAttribute(SESSION_KEY);

        if (value instanceof List<?>) {

            try {
                return (List<Integer>) value;
            } catch (Exception ignored) {
            }
        }

        List<Integer> ids = new ArrayList<>();
        session.setAttribute(SESSION_KEY, ids);

        return ids;
    }

    private String normalize(String value) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }
}