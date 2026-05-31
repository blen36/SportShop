package com.sportshop.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Product {

    private int id;
    private String name;
    private String description;
    private BigDecimal price;
    private String brand;
    private Integer categoryId;

    // характеристики товара из JSONB-поля products.attributes
    private String attributes;

    // главное изображение товара из product_images
    private String imageUrl;

    // остаток на складе из inventory
    private int stock;

    // рейтинг из reviews
    private double averageRating;
    private int reviewsCount;

    public Product() {}

    public Product(int id,
                   String name,
                   String description,
                   BigDecimal price,
                   String brand,
                   Integer categoryId) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.brand = brand;
        this.categoryId = categoryId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }


    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }


    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }


    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }


    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }


    public int getReviewsCount() {
        return reviewsCount;
    }

    public void setReviewsCount(int reviewsCount) {
        this.reviewsCount = reviewsCount;
    }

    public List<AttributeItem> getAttributeItems() {
        return parseAttributes(false);
    }

    public List<AttributeItem> getShortAttributeItems() {
        List<AttributeItem> all = parseAttributes(true);
        List<AttributeItem> result = new ArrayList<>();

        addPreferredAttribute(all, result, "Материал");
        addPreferredAttribute(all, result, "Уровень");
        addPreferredAttribute(all, result, "Цвет");
        addPreferredAttribute(all, result, "Размер");
        addPreferredAttribute(all, result, "Вес");

        if (result.size() < 3) {
            for (AttributeItem item : all) {
                if (result.size() >= 3) {
                    break;
                }

                if (!containsKey(result, item.getKey())) {
                    result.add(item);
                }
            }
        }

        if (result.size() > 3) {
            return result.subList(0, 3);
        }

        return result;
    }

    private void addPreferredAttribute(List<AttributeItem> source,
                                       List<AttributeItem> target,
                                       String key) {

        if (target.size() >= 3) {
            return;
        }

        for (AttributeItem item : source) {
            if (key.equalsIgnoreCase(item.getKey()) &&
                    !containsKey(target, item.getKey())) {

                target.add(item);
                return;
            }
        }
    }

    private boolean containsKey(List<AttributeItem> items,
                                String key) {

        for (AttributeItem item : items) {
            if (item.getKey().equalsIgnoreCase(key)) {
                return true;
            }
        }

        return false;
    }

    private List<AttributeItem> parseAttributes(boolean skipCategoryFields) {

        List<AttributeItem> items = new ArrayList<>();

        if (attributes == null || attributes.isBlank()) {
            return items;
        }

        String value = attributes.trim();

        if (value.equals("{}")) {
            return items;
        }

        if (value.startsWith("{") && value.endsWith("}")) {
            value = value.substring(1, value.length() - 1);
        }

        for (String pair : splitJsonPairs(value)) {

            int separatorIndex = findSeparator(pair);

            if (separatorIndex <= 0) {
                continue;
            }

            String key = cleanJsonValue(
                    pair.substring(0, separatorIndex)
            );

            String val = cleanJsonValue(
                    pair.substring(separatorIndex + 1)
            );

            if (key.isBlank() || val.isBlank()) {
                continue;
            }

            if (skipCategoryFields &&
                    ("Категория".equalsIgnoreCase(key) ||
                            "Подкатегория".equalsIgnoreCase(key))) {

                continue;
            }

            items.add(new AttributeItem(key, val));
        }

        return items;
    }

    private List<String> splitJsonPairs(String value) {

        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean escaped = false;

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);

            if (escaped) {
                current.append(ch);
                escaped = false;
                continue;
            }

            if (ch == '\\') {
                current.append(ch);
                escaped = true;
                continue;
            }

            if (ch == '"') {
                inQuotes = !inQuotes;
                current.append(ch);
                continue;
            }

            if (ch == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(ch);
        }

        if (!current.isEmpty()) {
            result.add(current.toString());
        }

        return result;
    }

    private int findSeparator(String value) {

        boolean inQuotes = false;
        boolean escaped = false;

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (ch == '\\') {
                escaped = true;
                continue;
            }

            if (ch == '"') {
                inQuotes = !inQuotes;
                continue;
            }

            if (ch == ':' && !inQuotes) {
                return i;
            }
        }

        return -1;
    }

    private String cleanJsonValue(String value) {

        if (value == null) {
            return "";
        }

        String result = value.trim();

        if (result.startsWith("\"") && result.endsWith("\"") &&
                result.length() >= 2) {

            result = result.substring(1, result.length() - 1);
        }

        return result
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .trim();
    }

    public static class AttributeItem {

        private final String key;
        private final String value;

        public AttributeItem(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}
