package ru.vlsu.marketplace.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vlsu.marketplace.entities.Brand;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    Optional<Brand> findByName(String name);
}
