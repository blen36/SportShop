package com.sportshop.service;

import com.sportshop.models.Review;
import com.sportshop.repository.ReviewRepository;

import java.util.List;
import java.util.Set;

public class ReviewService {

    private final ReviewRepository repository =
            new ReviewRepository();

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "APPROVED",
            "REJECTED",
            "PENDING"
    );

    public List<Review> getProductReviews(int productId) {
        return repository.findApprovedByProductId(productId);
    }

    public List<Review> getAllReviews() {
        return repository.findAll();
    }

    public boolean addReview(int userId,
                             int productId,
                             int rating,
                             String comment) {

        if (rating < 1 || rating > 5) {
            return false;
        }

        if (comment == null) {
            comment = "";
        }

        return repository.addReview(
                userId,
                productId,
                rating,
                comment.trim()
        );
    }

    public boolean canUserReview(int userId,
                                 int productId) {

        return repository.canUserReview(
                userId,
                productId
        );
    }

    public void updateStatus(int reviewId,
                             String status) {

        if (status == null ||
                !ALLOWED_STATUSES.contains(status)) {

            return;
        }

        repository.updateStatus(
                reviewId,
                status
        );
    }
}