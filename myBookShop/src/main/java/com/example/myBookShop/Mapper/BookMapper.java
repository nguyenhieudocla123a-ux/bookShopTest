package com.example.myBookShop.Mapper;

import com.example.myBookShop.DTO.Respone.BookResponse;
import com.example.myBookShop.Entity.Book;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookMapper {
    BookResponse toResponse(Book book);


}
