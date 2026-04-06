package ru.vlsu.marketplace.dto;

import lombok.Data;

@Data
public class OrderDto {

    private String deliveryAddress;
    private String contactName;
    private String contactPhone;
}
