package org.example.expensemanagement.repository;

import org.example.expensemanagement.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.UUID;
@EnableJpaRepositories
public interface UserRepository extends JpaRepository<Users, String> {
  Users findByFullName(String fullName);

  Users findByPhone(String phone);
}
