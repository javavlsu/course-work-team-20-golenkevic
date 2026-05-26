package ru.vlsu.marketplace.repositories;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.vlsu.marketplace.entities.Brand;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Integer> {

    @Override
    @Cacheable("brands")
    List<Brand> findAll();

    Optional<Brand> findByName(String name);

    @Override
    @CacheEvict(value = "brands", allEntries = true)
    <S extends Brand> S save(S entity);
}
