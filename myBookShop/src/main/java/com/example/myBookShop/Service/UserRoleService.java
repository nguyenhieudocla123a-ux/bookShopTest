package com.example.myBookShop.Service;

import com.example.myBookShop.DTO.Respone.UserRoleResponse;
import com.example.myBookShop.Mapper.UserRoleMapper;
import com.example.myBookShop.Repositories.UserRoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor

public class UserRoleService {
    private final UserRoleRepository userRoleRepository;
    private final UserRoleMapper userRoleMapper;
    //get all
    public List<UserRoleResponse> getAll(){
         return userRoleRepository.findAll().stream().map(userRoleMapper::toResponse).toList();
    }
}
