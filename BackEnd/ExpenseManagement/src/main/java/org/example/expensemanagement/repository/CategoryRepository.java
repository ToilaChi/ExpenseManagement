package org.example.expensemanagement.repository;

import org.example.expensemanagement.models.Category;
import org.example.expensemanagement.models.ExpenseType;
import org.example.expensemanagement.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  Optional<Category> findByIdAndUserId(Long categoryId, String userId);
  Optional<Category> findByNameAndUserId(String categoryName, String userId);
  List<Category> findByUserId(String userId);

  String user(Users user);

  List<Category> findByUserIdAndExpenseType(String userId, ExpenseType expenseType);

  List<Category> findByUserIdAndExpenseTypeAndIdNot(String userId, ExpenseType expenseType, Long excludeId);

  List<Category> findByUserIdOrderByExpenseTypeDescCreatedAtAsc(String userId);

  boolean existsByNameAndUserId(String categoryName, String userId);

  boolean existsByNameAndUserIdAndIdNot(String name, String userId, Long excludeId);

  List<Category> findAllByExpenseType(ExpenseType expenseType);

  List<Category> findAllByUserIdAndExpenseType(String userId, ExpenseType expenseType);
}
