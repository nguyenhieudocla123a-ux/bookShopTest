package com.example.myBookShop.Service;

import com.example.myBookShop.DTO.Request.CreateAuthorRequest;
import com.example.myBookShop.DTO.Respone.AuthorResponse;
import com.example.myBookShop.DTO.Respone.BookResponse;
import com.example.myBookShop.Entity.Author;
import com.example.myBookShop.Exception.ConflictDataException;
import com.example.myBookShop.Exception.ResourceNotFoundException;
import com.example.myBookShop.Mapper.AuthorMapper;
import com.example.myBookShop.Mapper.BookMapper;
import com.example.myBookShop.Repositories.AuthorRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthorService {
      private final AuthorRepository authorRepository;
      private final AuthorMapper authorMapper;
      private final BookMapper bookMapper;

      //Find All
      public List<AuthorResponse> getAll(){
          return authorRepository.findAll().stream().map(authorMapper::toResponse).toList();
      }

      //Create
      public AuthorResponse create(CreateAuthorRequest createAuthorRequest){
          Author createdAuthor = authorMapper.fromCreateRequestToEntity(createAuthorRequest);
          authorRepository.save(createdAuthor);
          return authorMapper.toResponse(createdAuthor);
      }

      //Find all books of author

      public List<BookResponse> findBook(int id){
           Author author=authorRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Not found author"));
           return author.getTheirBooks().stream().map(bookMapper::toResponse).toList();
      }

      //Delete author but server is going to reject if the book of this author still exist

      public void delete(int id ){
          Author author=authorRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Not found author"));
          if(author.getTheirBooks().isEmpty()) authorRepository.delete(author);
          else throw new ConflictDataException("all book of this author still exist");
      }

}
