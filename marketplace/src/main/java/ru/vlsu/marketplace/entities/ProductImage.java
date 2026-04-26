package ru.vlsu.marketplace.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "fk_product", nullable = false)
    private Product product;

    @Lob
    @Column(name = "image_data", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] imageData;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
}
