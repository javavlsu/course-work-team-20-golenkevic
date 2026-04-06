package ru.vlsu.marketplace.dto;

import lombok.Data;

@Data
public class ReviewDto {

    private Byte rating;
    private String text;
}
