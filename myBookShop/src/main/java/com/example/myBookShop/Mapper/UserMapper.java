package com.example.myBookShop.Mapper;

import com.example.myBookShop.DTO.Respone.UserSimpleResponse;
import com.example.myBookShop.Entity.User;
import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserSimpleResponse toSimpleResponse(User user);
}
