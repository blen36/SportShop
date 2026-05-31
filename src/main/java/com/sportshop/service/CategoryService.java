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
}