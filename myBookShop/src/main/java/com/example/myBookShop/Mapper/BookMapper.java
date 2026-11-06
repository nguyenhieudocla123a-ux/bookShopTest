package com.example.myBookShop.Mapper;

import com.example.myBookShop.DTO.Respone.BookResponse;
import com.example.myBookShop.Entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookMapper {
    @Mapping(source = "author.name", target = "authorName")
    BookResponse toResponse(Book book);
}
