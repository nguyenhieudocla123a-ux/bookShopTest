package com.example.myBookShop.Mapper;

import com.example.myBookShop.DTO.Respone.UserRoleResponse;
import com.example.myBookShop.Entity.userRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserRoleMapper {

    @Mapping(source = "user.username", target = "userName")
    @Mapping(source = "role.name",target ="roleName")
    UserRoleResponse toResponse(userRole userRole);
}
