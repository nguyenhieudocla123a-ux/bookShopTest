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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthorService {
      private final AuthorRepository authorRepository;
      private final AuthorMapper authorMapper;
      private final BookMapper bookMapper;
      private final RedisTemplate<String,Object> redisTemplate;
      private final ObjectMapper objectMapper;

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

          //First step for finding book in redis
           Author author=authorRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Not found author"));
           String key = "author:"+author.getId()+":book";
           List<Object> listBook = redisTemplate.opsForList().range(key,0, -1);
           if(!listBook.isEmpty()){
               return author.getTheirBooks().stream().map(bookMapper::toResponse).toList();
           }
            redisTemplate.opsForList().rightPushAll(key,author.getTheirBooks().stream().map(bookMapper::toResponse).toList());
            return redisTemplate.opsForList().range(key,0,-1).stream().map(t->objectMapper.convertValue(t,BookResponse.class)).toList();
      }

      //Delete author but server is going to reject if the book of this author still exist

      public void delete(int id ){
          Author author=authorRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Not found author"));
          if(author.getTheirBooks().isEmpty()) authorRepository.delete(author);
          else throw new ConflictDataException("all book of this author still exist");
      }

}
