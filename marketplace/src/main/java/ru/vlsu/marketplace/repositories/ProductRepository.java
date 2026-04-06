package ru.vlsu.marketplace.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vlsu.marketplace.entities.Product;
import ru.vlsu.marketplace.entities.User;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findBySeller(User seller);

    List<Product> findBySellerIdAndStatus(Integer sellerId, Product.Status status);

    Page<Product> findByStatus(Product.Status status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' " +
           "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:condition IS NULL OR p.condition = :condition) " +
           "AND (:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%',:search,'%')))")
    Page<Product> findWithFilters(@Param("categoryId") Integer categoryId,
                                  @Param("minPrice") BigDecimal minPrice,
                                  @Param("maxPrice") BigDecimal maxPrice,
                                  @Param("condition") Product.Condition condition,
                                  @Param("search") String search,
                                  Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' ORDER BY p.createdAt DESC")
    List<Product> findNewest(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = 'APPROVED' ORDER BY SIZE(p.orderItems) DESC")
    List<Product> findPopular(Pageable pageable);

    List<Product> findByStatusOrderByCreatedAtDesc(Product.Status status);

    long countByStatus(Product.Status status);
}
