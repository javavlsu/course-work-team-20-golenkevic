package ru.vlsu.marketplace.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vlsu.marketplace.entities.ProductImage;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProductIdOrderBySortOrderAsc(Integer productId);
}
