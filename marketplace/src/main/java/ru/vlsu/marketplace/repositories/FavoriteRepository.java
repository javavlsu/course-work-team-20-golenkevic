package ru.vlsu.marketplace.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vlsu.marketplace.entities.Favorite;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    List<Favorite> findByUserId(Integer userId);

    @Query("""
            SELECT f FROM Favorite f
            JOIN FETCH f.product p
            LEFT JOIN FETCH p.category
            LEFT JOIN FETCH p.brand
            JOIN FETCH p.seller
            WHERE f.user.id = :userId
            """)
    List<Favorite> findByUserIdWithProduct(@Param("userId") Integer userId);

    Optional<Favorite> findByUserIdAndProductId(Integer userId, Integer productId);

    boolean existsByUserIdAndProductId(Integer userId, Integer productId);
}
