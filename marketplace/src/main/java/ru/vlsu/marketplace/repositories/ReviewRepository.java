package ru.vlsu.marketplace.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vlsu.marketplace.entities.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByProductIdOrderByCreatedAtDesc(Integer productId);

    List<Review> findByAuthorId(Integer authorId);

    Optional<Review> findByAuthorIdAndProductId(Integer authorId, Integer productId);

    boolean existsByAuthorIdAndProductId(Integer authorId, Integer productId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Integer productId);

    long countByProductId(Integer productId);
}
