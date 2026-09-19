package com.example.myBookShop.DTO.Respone;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRoleResponse {
    private String userName;
    private String roleName;
}
