package org.example.expensemanagement.repository;

import org.example.expensemanagement.models.RefreshToken;
import org.example.expensemanagement.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  Optional<RefreshToken> findByToken(String token);
  void deleteByUser(Users user);
  void deleteByToken(String token);
}