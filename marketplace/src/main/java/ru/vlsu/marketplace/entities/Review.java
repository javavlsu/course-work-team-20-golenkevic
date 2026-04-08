package ru.vlsu.marketplace.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(columnNames = {"fk_user", "fk_product"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "fk_product", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "fk_user", nullable = false)
    private User author;

    @Column(nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Byte rating;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(name = "created_at")
    private Instant createdAt;
}
