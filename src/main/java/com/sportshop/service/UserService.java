package com.sportshop.service;

import com.sportshop.models.Product;
import com.sportshop.models.User;
import com.sportshop.repository.UserRepository;

import java.util.List;
import java.util.Set;

public class UserService {

    private final UserRepository repo = new UserRepository();

    private static final Set<String> ALLOWED_ROLES = Set.of(
            "CLIENT",
            "ADMIN"
    );

    public User login(String email, String password) {

        if (email == null || password == null) {
            return null;
        }

        User user = repo.findByEmail(email);

        if (user == null) {
            return null;
        }

        if (user.isBlocked()) {
            return null;
        }

        if (password.equals(user.getPasswordHash())) {
            return user;
        }

        return null;
    }

    public boolean register(String email, String password) {

        if (email == null || password == null
                || email.isBlank() || password.isBlank()) {

            return false;
        }

        if (repo.findByEmail(email) != null) {
            return false;
        }

        User user = new User();

        user.setEmail(email);
        user.setPasswordHash(password);
        user.setBlocked(false);

        if (email.equalsIgnoreCase("bankoegor@gmail.com")) {
            user.setRole("ADMIN");
        } else {
            user.setRole("CLIENT");
        }

        repo.save(user);

        return true;
    }

    public User getUser(int id) {
        return repo.findById(id);
    }

    public List<User> getAllUsers() {
        return repo.findAll();
    }

    public void updateRole(int userId,
                           String role) {

        if (role == null ||
                !ALLOWED_ROLES.contains(role)) {

            return;
        }

        repo.updateRole(userId, role);
    }

    public void updateBlocked(int userId,
                              boolean blocked) {

        repo.updateBlocked(userId, blocked);
    }

    public void addFavorite(int userId, int productId) {
        repo.addFavorite(userId, productId);
    }

    public void removeFavorite(int userId, int productId) {
        repo.removeFavorite(userId, productId);
    }

    public List<Product> getFavorites(int userId) {
        return repo.getFavorites(userId);
    }

    public int getUsersCount() {
        return repo.getUsersCount();
    }
}