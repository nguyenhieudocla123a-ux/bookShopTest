package com.example.myBookShop.test;


import com.example.myBookShop.Entity.Role;
import com.example.myBookShop.Entity.User;
import com.example.myBookShop.Exception.ConflictDataException;
import com.example.myBookShop.Mapper.UserMapper;
import com.example.myBookShop.Repositories.BookRepository;
import com.example.myBookShop.Repositories.RoleRepository;
import com.example.myBookShop.Repositories.UserRepository;
import com.example.myBookShop.Repositories.UserRoleRepository;
import com.example.myBookShop.Service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @Mock
    BookRepository bookRepository;
    @Mock
    private  RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private  UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    UserService userService;
    private final String DEFAULT_ROLE ="USER";


    @Test
    public void testCreateUserWithDefaultRole() {
        //Arrange
        String userName="Hieudeptrai";
        String email="nguyenhieudocla@gmail.com";
        String encodedPassword="uoeowoeoeddprse";
        String phone="0123456789";

        Role role=new Role();
        role.setName(DEFAULT_ROLE);
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(anyString())).thenReturn(Optional.of(role));

        //act
        User user= userService.createWithDefaultRole(userName,email,encodedPassword,phone);
        //assert
        assertEquals(DEFAULT_ROLE,user.getUserRoleList().get(0).getRole().getName());
        //verify
        verify(userRoleRepository,atLeastOnce()).save(any()) ;
    }

    @Test
    public void testCreateUserWithDefaultRoleAndPassword_When_UserNameExists() {
        String userName="Hieudeptrai";
        String email="nguyenhieudocla@gmail.com";
        String encodedPassword="uoeowoeoeddprse";
        String phone="0123456789";

        User test_user=new User();
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(test_user));


        //assert
        assertThrows(ConflictDataException.class,()->userService.createWithDefaultRole(userName,email,encodedPassword,phone));
    }


    @Test
    public void testCreateUserWithDefaultRoleAndPassword_When_EmailExists() {


    }
}
