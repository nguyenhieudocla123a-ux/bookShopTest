package com.example.myBookShop.Mapper;

import com.example.myBookShop.DTO.Respone.BorrowRecordResponse;
import com.example.myBookShop.Entity.borrowRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BorrowMapper {
    @Mapping(source = "user.username" , target = "userName")
    @Mapping(source = "book.title",target = "bookName")
    BorrowRecordResponse toResponse(borrowRecord borrow);

}
