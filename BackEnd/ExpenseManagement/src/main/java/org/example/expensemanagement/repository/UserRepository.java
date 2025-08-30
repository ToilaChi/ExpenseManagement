package org.example.expensemanagement.repository;

import io.micrometer.common.lang.NonNullApi;
import org.example.expensemanagement.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

@EnableJpaRepositories
public interface UserRepository extends JpaRepository<Users, String> {
  Users findByFullName(String fullName);

  Users findByPhone(String phone);

  Optional<Users> findById(String id);

  boolean existsByEmail(String email);

  boolean existsByPhone(String phone);
}
