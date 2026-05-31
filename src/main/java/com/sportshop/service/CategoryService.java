package com.sportshop.service;

import com.sportshop.models.Category;
import com.sportshop.repository.CategoryRepository;

import java.util.List;

public class CategoryService {

    private final CategoryRepository repository =
            new CategoryRepository();

    public List<Category> getAllCategories() {
        return repository.findAll();
    }

    public Category getCategory(int id) {
        return repository.findById(id);
    }

    public int save(Category category) {
        normalize(category);

        if (category.getId() > 0 &&
                category.getParentId() != null &&
                category.getId() == category.getParentId()) {

            return 0;
        }

        return repository.save(category);
    }

    public boolean delete(int id) {
        if (id <= 0) {
            return false;
        }

        if (repository.hasChildCategories(id)) {
            return false;
        }

        if (repository.hasProducts(id)) {
            return false;
        }

        return repository.delete(id);
    }

    private void normalize(Category category) {
        if (category.getName() != null) {
            category.setName(category.getName().trim());
        }

        if (category.getName() == null || category.getName().isBlank()) {
            category.setName("Новая категория");
        }
    }
}
