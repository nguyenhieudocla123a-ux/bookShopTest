package com.example.myBookShop.Controllers;

import com.example.myBookShop.DTO.Request.LoginRequest;
import com.example.myBookShop.DTO.Request.LogoutRequest;
import com.example.myBookShop.DTO.Request.RegisterRequest;
import com.example.myBookShop.DTO.Respone.ApiRespone;
import com.example.myBookShop.DTO.Respone.jwtResponse;
import com.example.myBookShop.Entity.RefreshToken;
import com.example.myBookShop.Entity.Role;
import com.example.myBookShop.Entity.User;
import com.example.myBookShop.Entity.userRole;
import com.example.myBookShop.Exception.ConflictDataException;
import com.example.myBookShop.Exception.ResourceNotFoundException;
import com.example.myBookShop.Repositories.RoleRepository;
import com.example.myBookShop.Repositories.UserRepository;
import com.example.myBookShop.Repositories.UserRoleRepository;
import com.example.myBookShop.Security.JwtService;
import com.example.myBookShop.Service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@CrossOrigin(origins ="*")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRolesRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RedisTemplate<String,Object> redisTemplate;

    @PostMapping("/login")
    public ResponseEntity<?> Login(@Valid @RequestBody LoginRequest loginRequest){
        User user=userRepository.findByUserName(loginRequest.getUserName()).orElseThrow(()->new UsernameNotFoundException("UserName not found"));
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(user.getUsername(),user.getPassword(),user.getAuthorities());

        //Nếu ok lưu vào context
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //Trả token về
        //Tạo jwt Response
        String refreshToken=refreshTokenService.create(user.getId()).getToken();

        redisTemplate.opsForValue().set("refreshToken:"+user.getId(),refreshToken,10, TimeUnit.DAYS);
        String accessToken=refreshTokenService.createNewAccessToken(refreshToken);
        jwtResponse jwtRes=new jwtResponse(accessToken,user.getUsername(),user.getEmail(),user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        //Trả respone
        ApiRespone<Map<String,String>> res=new ApiRespone<>(
                true, Map.of("accessToken",accessToken,"refreshToken",refreshToken),"Login Successful", LocalDateTime.now()
        );
        return ResponseEntity.ok(res);
    }

    @PostMapping("/register")
    public ResponseEntity<?> Register(@Valid @RequestBody RegisterRequest registerRequest){
        String userName=registerRequest.getUserName();
        String passWord=registerRequest.getPassWord();
        String email=registerRequest.getEmail();

        //Xem trong db có ông nào có tên như vậy ko
        Optional<User> user=userRepository.findByUserName(userName);
        if(user.isPresent()) throw  new ConflictDataException("Username is existed!");
        //Bỏ qua email
        // lưu xuống DB
        User userRegister= new User();
        userRegister.setUserName(userName);
        userRegister.setEmail(email);
        userRegister.setPassWord(passwordEncoder.encode(passWord));
        userRegister.setEnable(true);
        userRegister.setCreatedAt(LocalDateTime.now());
        //set auth
        // default role : User
        Role role = roleRepository.findByName("USER").orElseThrow(()-> new ResourceNotFoundException("Role not found"));
        userRepository.save(userRegister);
        userRolesRepository.save(new userRole(userRegister,role));
        return ResponseEntity.ok("Sign up Successful!");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request){
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
        // Revoked token
        refreshTokenService.deleteAllRefreshToken(refreshToken.getUsersToken().getId());
        ApiRespone<Void> res=new ApiRespone<>(
                true,null ,"Logout Successful", LocalDateTime.now()
        );
        return ResponseEntity.ok(res);
    }
}
