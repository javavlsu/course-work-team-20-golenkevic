package ru.vlsu.marketplace.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "experiment_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "value", length = 100, nullable = false)
    private String value;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
