package ru.vlsu.marketplace.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vlsu.marketplace.entities.ExperimentData;

public interface ExperimentDataRepository extends JpaRepository<ExperimentData, Integer> {
}
