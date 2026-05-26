package ru.vlsu.marketplace.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vlsu.marketplace.entities.ExperimentLog;

import java.util.List;

public interface ExperimentLogRepository extends JpaRepository<ExperimentLog, Integer> {
    List<ExperimentLog> findByExperimentOrderByIdAsc(String experiment);
}
