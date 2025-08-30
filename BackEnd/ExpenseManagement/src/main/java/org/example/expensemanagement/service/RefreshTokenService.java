package org.example.expensemanagement.service;

import jakarta.transaction.Transactional;
import org.example.expensemanagement.models.RefreshToken;
import org.example.expensemanagement.models.Users;
import org.example.expensemanagement.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {
  @Value("${jwt.refresh.duration}")
  private Long refreshTokenDuration;

  @Autowired
  private RefreshTokenRepository refreshTokenRepository;

  @Transactional
  public RefreshToken createRefreshToken(Users user) {
    //Xóa các refresh cũ của user
    refreshTokenRepository.deleteByUser(user);

    RefreshToken refreshToken = new RefreshToken();
    refreshToken.setUser(user);

    String refreshTokenString = UUID.randomUUID().toString();
    refreshToken.setToken(refreshTokenString);

    refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenDuration));
    return refreshTokenRepository.save(refreshToken);
  }

  public RefreshToken verifyExpiration(RefreshToken token) {
    if(token.getExpiresAt().compareTo(Instant.now()) < 0) {
      refreshTokenRepository.delete(token);
      throw new RuntimeException("Refresh token was expired. Please login again.");
    }
    return token;
  }

  @Transactional
  public void deleteByUser(Users user) {
    refreshTokenRepository.deleteByUser(user);
  }

  public RefreshToken findByToken(String token) {
    return refreshTokenRepository.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Refresh token not found"));
  }

  public void deleteByToken(String token) {
    refreshTokenRepository.deleteByToken(token);
  }
}
