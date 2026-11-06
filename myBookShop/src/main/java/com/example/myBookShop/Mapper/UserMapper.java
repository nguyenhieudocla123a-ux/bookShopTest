package com.example.myBookShop.Mapper;

import com.example.myBookShop.DTO.Respone.UserSimpleResponse;
import com.example.myBookShop.Entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // User chỉ có getUsername() (chữ n thường). Lombok không sinh thêm getUserName() vì nó so tên
    // getter không phân biệt hoa thường, nên phải khai báo rõ, nếu không userName sẽ luôn là null.
    @Mapping(source = "username", target = "userName")
    UserSimpleResponse toSimpleResponse(User user);
}
