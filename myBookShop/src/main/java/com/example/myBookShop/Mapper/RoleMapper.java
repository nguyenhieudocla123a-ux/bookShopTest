package com.example.myBookShop.Mapper;

import com.example.myBookShop.DTO.Respone.RoleRespone;
import com.example.myBookShop.Entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleRespone toResponse(Role role);
}
