package org.example.expensemanagement.service;

import jakarta.transaction.Transactional;
import org.example.expensemanagement.models.Category;
import org.example.expensemanagement.models.Expense;
import org.example.expensemanagement.models.ExpenseType;
import org.example.expensemanagement.models.Users;
import org.example.expensemanagement.repository.CategoryRepository;
import org.example.expensemanagement.repository.ExpenseRepository;
import org.example.expensemanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExpenseService {
  @Autowired
  private ExpenseRepository expenseRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private UserRepository userRepository;

  // Them chi tieu
  public Expense addExpense(String userId, Long categoryId, BigDecimal amount, String description) {
    // Kiem tra user
    Users user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User không tồn tại"));

    // Kiem tra cate co thuoc ve user do khong
    Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(() -> new RuntimeException("Category không tồn tại hoặc không thuộc về bạn"));

    if(category.getExpenseType() != ExpenseType.EXPENSE) {
      throw new RuntimeException("Không thể thêm chi tiêu vào category INCOME");
    }

    //Validate amount > 0
    if (amount == null ||  amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new RuntimeException("Số tiền phải lớn hơn 0");
    }

    // warning neu vuot qua so budget (nhung van cho phep)
    BigDecimal currentBudget = category.getCurrentBudget();
    boolean isOverBudget = currentBudget.compareTo(amount) <= 0;

    // expense record
    Expense expense = new Expense();
    expense.setAmount(amount);
    expense.setCategory(category);
    expense.setExpenseType(ExpenseType.EXPENSE);
    expense.setDescription(description);
    expense.setCreatedAt(LocalDateTime.now());
    expense.setUpdatedAt(LocalDateTime.now());

    Expense savedExpense = expenseRepository.save(expense);

    //update currentBudget cua category
    BigDecimal newCurrentBudget = currentBudget.subtract(amount);
    category.setCurrentBudget(newCurrentBudget);
    categoryRepository.save(category);

    //Log warning
    if (isOverBudget) {
      System.out.println("WARNING: Chi tiêu vượt quá ngân sách! Category: " +
              category.getName() + ", Over by: " + amount.subtract(currentBudget));
    }

    return savedExpense;
  }

  // Update expense
  @Transactional
  public Expense updateExpense(String userId, Long expenseId, String newCategoryName, BigDecimal newAmount, String newDescription) {
    // Tim expense va validate ownership
    Expense expense = expenseRepository.findByIdAndCategoryUserId(expenseId, userId)
            .orElseThrow(() -> new RuntimeException("Expense không tồn tại hoặc không thuộc về bạn"));

    Category oldCategory = expense.getCategory();
    BigDecimal oldAmount = expense.getAmount();

    //Validate new category neu update
    Category newCategory = oldCategory;
    if(newCategoryName  != null && !newCategoryName.trim().isEmpty()
            && !newCategoryName.equals(oldCategory.getName())) {
      newCategory = categoryRepository.findByNameAndUserId(newCategoryName, userId)
              .orElseThrow(() -> new RuntimeException("Category '" + newCategoryName + "' không tồn tại"));

      if(newCategory.getExpenseType() != ExpenseType.EXPENSE) {
        throw new RuntimeException("Không thể chuyển expense sang category INCOME");
      }
    }

    //Validate new amount
    BigDecimal finalAmount = (newAmount != null && newAmount.compareTo(BigDecimal.ZERO) > 0) ? newAmount : oldAmount;

    //Update budget cua categories
    // Hoan lai tien cho old category
    assert oldCategory != null;
    oldCategory.setCurrentBudget(oldCategory.getCurrentBudget().add(finalAmount));

    //Tru tien tu new category
    newCategory.setCurrentBudget(newCategory.getCurrentBudget().subtract(finalAmount));

    //Cap nhat expense
    expense.setAmount(finalAmount);
    expense.setCategory(newCategory);
    if(newDescription != null) {
      expense.setDescription(newDescription);
    }

    categoryRepository.save(oldCategory);
    if(!oldCategory.equals(newCategory)) {
      categoryRepository.save(newCategory);
    }

    return expenseRepository.save(expense);
  }

  //Delete expense
  @Transactional
  public void deleteExpense(String userId, Long expenseId) {
    Expense expense = expenseRepository.findByIdAndCategoryUserId(expenseId, userId)
            .orElseThrow(() -> new RuntimeException("Expense không tồn tại hoặc không thuộc về bạn"));

    Category category = expense.getCategory();
    BigDecimal amount = expense.getAmount();

    //Hoan lai tien cho category
    category.setCurrentBudget(category.getCurrentBudget().add(amount));
    categoryRepository.save(category);

    expenseRepository.delete(expense);
  }

  //Filter
  public Page<Expense> getFilteredExpenses(
          String userId, String filterType, String date, int page, int size) {
    Pageable pageable = PageRequest.of(page, size,
            Sort.by("createdAt").descending());

    LocalDateTime startDateTime;
    LocalDateTime endDateTime;

    //Default value
    if(filterType == null) filterType = "month";
    if(date == null || date.isEmpty()) date = "2025-01";

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

    try {
      switch (filterType.toLowerCase()) {
        case "day":
          LocalDate dayParsed = LocalDate.parse(date, dateFormatter);
          startDateTime = dayParsed.atStartOfDay();
          endDateTime = dayParsed.atTime(23, 59, 59);
          break;

        case "week":
          LocalDate weekStart = LocalDate.parse(date, dateFormatter);
          startDateTime = weekStart.atStartOfDay();
          endDateTime = weekStart.plusDays(6).atTime(23, 59, 59);
          break;

        case "month":
          LocalDate monthStart = LocalDate.parse(date + "-01", dateFormatter);
          startDateTime = monthStart.atStartOfDay();
          endDateTime = monthStart.plusMonths(1).minusDays(1).atTime(23, 59, 59);
          break;

        default:
          throw new IllegalArgumentException("FilterType không hợp lệ: " + filterType);
      }

      return expenseRepository.findByCategoryUserIdAndCreatedAtBetween(userId, startDateTime, endDateTime, pageable);
    }
    catch (Exception e) {
      throw new RuntimeException("Lỗi parse date: " + e.getMessage());
    }
  }
}
