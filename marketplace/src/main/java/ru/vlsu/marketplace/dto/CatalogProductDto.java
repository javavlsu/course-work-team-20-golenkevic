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
    private Integer categoryId;
    private String categoryName;
    private Integer brandId;
    private String brandName;
    private Product.Gender gender;
    private Product.Season season;
    private String color;
    private String material;
    private String size;
    private String sellerUsername;
    private Double averageRating;
    private Integer reviewsCount;
    private boolean hasImage;
    private String themeColor;
}
