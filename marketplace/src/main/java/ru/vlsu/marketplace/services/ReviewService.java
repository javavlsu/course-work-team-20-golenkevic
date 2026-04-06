package ru.vlsu.marketplace.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.vlsu.marketplace.entities.Review;
import ru.vlsu.marketplace.repositories.ReviewRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public Review save(Review review) {
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(Instant.now());
        }
        return reviewRepository.save(review);
    }

    public void delete(Review review) {
        reviewRepository.delete(review);
    }

    public Optional<Review> findById(Integer id) {
        return reviewRepository.findById(id);
    }

    public List<Review> getByProduct(Integer productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    public List<Review> getByAuthor(Integer authorId) {
        return reviewRepository.findByAuthorId(authorId);
    }

    public boolean reviewExists(Integer authorId, Integer productId) {
        return reviewRepository.existsByAuthorIdAndProductId(authorId, productId);
    }

    public Double getAverageRating(Integer productId) {
        return reviewRepository.getAverageRatingByProductId(productId);
    }
}
