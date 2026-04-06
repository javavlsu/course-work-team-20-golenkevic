package ru.vlsu.marketplace.dto;

import lombok.Data;
import ru.vlsu.marketplace.entities.User;

@Data
public class ChangeRoleRequest {

    private Integer userId;
    private User.Role newRole;
}
