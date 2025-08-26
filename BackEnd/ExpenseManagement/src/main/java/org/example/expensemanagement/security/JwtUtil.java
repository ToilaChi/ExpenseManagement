package org.example.expensemanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
  private final SecretKey key;

  // Token expiration time (30 minutes)
  private static final long EXPIRATION_TIME = 30 * 60 * 1000;
  private static  final long REFRESH_TOKEN_EXPIRATION = 604800000;

  public JwtUtil(@Value("${jwt.secret}") String secret) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(String username, String id) {
    return Jwts.builder()
            .subject("Expense Management")
            .claim("username", username)
            .claim("id", id)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .issuer("EXPENSE MANAGEMENT")
            .signWith(key, Jwts.SIG.HS256)
            .compact();
  }

  public String generateRefreshToken(String username, String id) {
    return Jwts.builder()
            .subject("Expense Management")
            .claim("username", username)
            .claim("id", id)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
            .issuer("EXPENSE MANAGEMENT")
            .signWith(key, Jwts.SIG.HS256)
            .compact();
  }

  public String validateTokenAndRetrieveSubject(String token) throws JwtException {
    Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();

    // Kiểm tra issuer và subject để đảm bảo tính nhất quán
    if (!"EXPENSE MANAGEMENT".equals(claims.getIssuer()) ||
            !"Expense Management".equals(claims.getSubject())) {
      System.out.println("Invalid token");
      throw new JwtException("Token không hợp lệ");
    }

    return claims.get("username", String.class);
  }
}
