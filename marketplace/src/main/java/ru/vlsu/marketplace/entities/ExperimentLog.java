package ru.vlsu.marketplace.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "experiment_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentLog {

    public enum Status { SUCCESS, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "experiment", length = 32, nullable = false)
    private String experiment;

    @Column(name = "method_name", length = 32, nullable = false)
    private String methodName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status;

    @Column(length = 500)
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
