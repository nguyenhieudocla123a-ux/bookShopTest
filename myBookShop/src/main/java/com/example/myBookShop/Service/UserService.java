package com.example.myBookShop.Service;

import com.example.myBookShop.DTO.Request.CreateUserRequest;
import com.example.myBookShop.DTO.Respone.UserSimpleResponse;
import com.example.myBookShop.Entity.Role;
import com.example.myBookShop.Entity.User;
import com.example.myBookShop.Entity.userRole;
import com.example.myBookShop.Exception.ConflictDataException;
import com.example.myBookShop.Exception.ResourceNotFoundException;
import com.example.myBookShop.Mapper.UserMapper;
import com.example.myBookShop.Repositories.RoleRepository;
import com.example.myBookShop.Repositories.UserRepository;
import com.example.myBookShop.Repositories.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private static final String DEFAULT_ROLE = "USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found!"));
    }

    @Transactional
    public UserSimpleResponse create(CreateUserRequest request) {
        User user = createWithDefaultRole(
                request.getUserName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassWord()),
                request.getPhone());
        return userMapper.toSimpleResponse(user);
    }

    /** Lưu user và role trong cùng một transaction: lỗi ở bước nào thì không bước nào được lưu. */
    @Transactional
    public User createWithDefaultRole(String userName, String email, String encodedPassword, String phone) {
        if (userRepository.findByUserName(userName).isPresent()) {
            throw new ConflictDataException("Username is already existed!");
        }
        Role role = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User user = new User();
        user.setUserName(userName);
        user.setEmail(email);
        user.setPassWord(encodedPassword);
        user.setPhone(phone);
        user.setEnable(true);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        userRoleRepository.save(new userRole(user, role));
        return user;
    }

    @Transactional(readOnly = true)
    public Page<UserSimpleResponse> getAllSimpleUser(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toSimpleResponse);
    }
}
