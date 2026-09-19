package com.example.myBookShop.Mapper;

import com.example.myBookShop.DTO.Request.CreateAuthorRequest;
import com.example.myBookShop.DTO.Respone.AuthorResponse;
import com.example.myBookShop.Entity.Author;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthorMapper {

    AuthorResponse toResponse(Author author);

    Author fromCreateRequestToEntity(CreateAuthorRequest createAuthorRequest);

}
