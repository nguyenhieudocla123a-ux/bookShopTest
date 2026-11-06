package com.example.myBookShop.Service;

import com.example.myBookShop.DTO.Respone.UserRoleResponse;
import com.example.myBookShop.Mapper.UserRoleMapper;
import com.example.myBookShop.Repositories.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final UserRoleRepository userRoleRepository;
    private final UserRoleMapper userRoleMapper;

    @Transactional(readOnly = true)
    public List<UserRoleResponse> getAll() {
        return userRoleRepository.findAll().stream().map(userRoleMapper::toResponse).toList();
    }
}
