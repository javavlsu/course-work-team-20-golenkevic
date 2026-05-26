package ru.vlsu.marketplace.repositories;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.vlsu.marketplace.entities.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Override
    @Cacheable("categories")
    List<Category> findAll();

    Optional<Category> findByName(String name);

    boolean existsByName(String name);

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    <S extends Category> S save(S entity);
}
