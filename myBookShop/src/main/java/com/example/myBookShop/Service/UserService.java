package com.example.myBookShop.Service;

import com.example.myBookShop.DTO.Request.CreateUserRequest;
import com.example.myBookShop.DTO.Respone.PageResponse;
import com.example.myBookShop.DTO.Respone.UserSimpleResponse;
import com.example.myBookShop.Entity.User;
import com.example.myBookShop.Exception.ConflictDataException;
import com.example.myBookShop.Mapper.UserMapper;
import com.example.myBookShop.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Data
public class UserService implements UserDetailsService {
    private UserRepository userRepository;
    private UserMapper userMapper;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUserName(username).orElseThrow(()->new UsernameNotFoundException("Username not found!"));
    }

    @Transactional
    public UserSimpleResponse create(CreateUserRequest createUserRequest){
          Optional<User> user=userRepository.findByUserName(createUserRequest.getUserName());
          if(user.isPresent()) throw new ConflictDataException("Username is already existed!");
          userRepository.save(user.get());
          return userMapper.toSimpleResponse(user.get());
    }

    public Page<UserSimpleResponse> getAllSimpleUser(Pageable pageable){
          return userRepository.findAll(pageable).map(userMapper::toSimpleResponse);
    }

    //TODO Delete, Update


}
