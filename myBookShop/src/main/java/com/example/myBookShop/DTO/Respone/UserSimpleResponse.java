package com.example.myBookShop.DTO.Respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSimpleResponse {
    int Id;
    String userName;
    String Email;
    String createAt;
}
