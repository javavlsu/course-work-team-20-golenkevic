package ru.vlsu.marketplace.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "brands")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String description;

    @Lob
    @Column(name = "logo", columnDefinition = "LONGBLOB")
    private byte[] logo;

    @OneToMany(mappedBy = "brand")
    private List<Product> products;
}
