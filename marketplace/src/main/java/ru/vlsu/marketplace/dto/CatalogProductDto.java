package ru.vlsu.marketplace.dto;

import lombok.Builder;
import lombok.Data;
import ru.vlsu.marketplace.entities.Product;

import java.math.BigDecimal;

@Data
@Builder
public class CatalogProductDto {

    private Integer id;
    private String title;
    private String description;
    private BigDecimal price;
    private Product.Condition condition;
    private String categoryName;
    private String sellerUsername;
    private Double averageRating;
    private Integer reviewsCount;
    private boolean hasImage;
    private String themeColor;
}
