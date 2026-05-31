package com.sportshop.service;

import com.sportshop.models.Notification;
import com.sportshop.repository.NotificationRepository;

import java.util.List;

public class NotificationService {

    private final NotificationRepository repository =
            new NotificationRepository();

    public void notifyUser(int userId,
                           String message) {

        if (userId <= 0 ||
                message == null ||
                message.isBlank()) {

            return;
        }

        repository.create(
                userId,
                message.trim()
        );
    }

    public List<Notification> getUserNotifications(int userId) {
        return repository.findByUserId(userId);
    }

    public int getUnreadCount(int userId) {
        return repository.countUnread(userId);
    }

    public void markAllAsRead(int userId) {
        repository.markAllAsRead(userId);
    }
}