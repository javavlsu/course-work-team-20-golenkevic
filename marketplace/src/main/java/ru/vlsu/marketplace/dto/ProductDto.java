package ru.vlsu.marketplace.dto;

import lombok.Data;
import ru.vlsu.marketplace.entities.Product;

import java.math.BigDecimal;

@Data
public class ProductDto {

    private String title;
    private String description;
    private BigDecimal price;
    private Product.Condition condition;
    private Integer categoryId;
    private Integer brandId;
    private Product.Gender gender;
    private Product.Season season;
    private String color;
    private String material;
    private String size;
}
