package ru.vlsu.marketplace.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "favorites", uniqueConstraints = @UniqueConstraint(columnNames = {"fk_user", "fk_product"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "fk_user", optional = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "fk_product", optional = false)
    private Product product;
}
