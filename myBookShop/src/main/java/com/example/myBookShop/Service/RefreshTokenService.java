package com.example.myBookShop.Service;

import com.example.myBookShop.Entity.RefreshToken;
import com.example.myBookShop.Entity.User;
import com.example.myBookShop.Exception.ResourceNotFoundException;
import com.example.myBookShop.Repositories.RfTokenRepository;
import com.example.myBookShop.Repositories.UserRepository;
import com.example.myBookShop.Security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final Long expirationToken;
    private final RfTokenRepository rfTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public RefreshTokenService(RfTokenRepository rfTokenRepository, UserRepository userRepository, JwtService jwtService,@Value("${jwt.expiration-refreshToken}") Long expirationToken) {
        this.rfTokenRepository = rfTokenRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.expirationToken = expirationToken;
    }

    //Create Refresh Token
    @Transactional
    public RefreshToken create(int userId){
        User user=userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found!"));

        //Delete all user's refreshToken
        rfTokenRepository.deleteAllByUsersToken_Id(userId);
        //create object token
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setExpiryAt(refreshToken.getCreatedAt().plusMillis(expirationToken));
        refreshToken.setRevoked(false);
        refreshToken.setUsersToken(user);
        rfTokenRepository.save(refreshToken);

        return refreshToken;
    }

    //Check expiration

    public boolean verifyExpirationToken(RefreshToken token){
        return token.getExpiryAt().compareTo(Instant.now()) > 0;
    }
    // find by token

    public RefreshToken findByToken(String token){
        return rfTokenRepository.findByToken(token).orElseThrow(()-> new ResourceNotFoundException("Refresh Token not found"));
    }
    // create new accessToken

    public String createNewAccessToken(String token){
        //Check Token's values
        RefreshToken refreshToken=rfTokenRepository.findByToken(token).
                orElseThrow(()-> new ResourceNotFoundException("Refresh Token not found"));
        //Expiration
        if(refreshToken.isRevoked()) throw  new RuntimeException("Refresh Token revoked");
        if(refreshToken.getExpiryAt().compareTo(Instant.now()) < 0) throw  new RuntimeException("Refresh Token outdate!");
        //Check user
        User user = userRepository.findById(refreshToken.getUsersToken().getId()).orElseThrow(()-> new ResourceNotFoundException("User Not Found!"));

        // generate access Token
        return jwtService.generateToken(user,null);
}
    //Delete all refreshToken by userId
    @Transactional
    public void deleteAllRefreshToken(int id){
        User user= userRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("User Not Found !"));
        rfTokenRepository.deleteAllByUsersToken_Id(user.getId());
    }

}
