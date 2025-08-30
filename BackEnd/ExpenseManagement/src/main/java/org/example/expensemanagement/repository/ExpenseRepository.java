package org.example.expensemanagement.repository;

import org.example.expensemanagement.models.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
  List<Expense> findByCategoryUserIdOrderByCreatedAtDesc(String userId);
  List<Expense> findByCategoryIdOrderByCreatedAtDesc(Long categoryId);
  Optional<Expense> findByIdAndCategoryUserId(Long expenseId, String userId);

  Page<Expense> findByCategoryUserIdAndCreatedAtBetween(String userId, LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable);
}
