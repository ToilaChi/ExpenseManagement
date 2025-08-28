package org.example.expensemanagement.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.expensemanagement.models.Category;
import org.example.expensemanagement.models.ExpenseType;
import org.example.expensemanagement.models.Users;
import org.example.expensemanagement.repository.CategoryRepository;
import org.example.expensemanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryService {
  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private UserRepository userRepository;

  // Color pool
  private static final List<String> COLOR_POOL = Arrays.asList(
          "#4CAF50", // Xanh lá cho INCOME (default)
          "#FF6B6B", "#4ECDC4", "#45B7D1", // 3 màu cho default EXPENSE categories
          "#96CEB4", "#FECA57", "#FF9FF3", "#54A0FF", "#5F27CD", "#00D2D3",
          "#FF9F43", "#10AC84", "#EE5A24", "#0984E3", "#A29BFE", "#FD79A8",
          "#E17055", "#81ECEC", "#74B9FF", "#6C5CE7", "#FDCB6E", "#E84393",
          "#00B894", "#00CEFF", "#E17055", "#00D63F", "#FF7675", "#6C5CE7",
          "#A29BFE", "#FD79A8", "#FDCB6E", "#00B894"
  );

  @Data
  @AllArgsConstructor
  public static class BudgetSummary {
    private BigDecimal totalIncome;
    private BigDecimal totalAllocated;
    private BigDecimal remainingIncome;
  }

  public BudgetSummary getBudgetSummary(String userId) {
    BigDecimal totalIncome = getIncomeAmount(userId);
    BigDecimal totalAllocated = categoryRepository.findByUserIdAndExpenseType(userId, ExpenseType.EXPENSE)
            .stream()
            .map(Category::getAllocatedBudget)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal remaining = totalIncome.subtract(totalAllocated);

    return new BudgetSummary(totalIncome, totalAllocated, remaining);
  }

  //Default Category
  @Transactional
  public void createDefaultCategories(Users user) {
    //INCOME category
    Category incomeCategory = new Category();
    incomeCategory.setName("Số dư tài khoản");
    incomeCategory.setColorHex(COLOR_POOL.get(0));
    incomeCategory.setExpenseType(ExpenseType.INCOME);
    incomeCategory.setUser(user);
    incomeCategory.setAllocatedBudget(BigDecimal.ZERO);
    incomeCategory.setCurrentBudget(BigDecimal.ZERO);
    categoryRepository.save(incomeCategory);

    //EXPENSE category
    String[][] defaultExpense = {
            {"Ăn uống", COLOR_POOL.get(1)},
            {"Cafe & Đi chơi", COLOR_POOL.get(2)},
            {"Xăng xe", COLOR_POOL.get(3)}
    };

    for(String[] expenseData : defaultExpense) {
      Category expenseCategory = new Category();
      expenseCategory.setName(expenseData[0]);
      expenseCategory.setColorHex(expenseData[1]);
      expenseCategory.setExpenseType(ExpenseType.EXPENSE);
      expenseCategory.setUser(user);
      expenseCategory.setAllocatedBudget(BigDecimal.ZERO);
      expenseCategory.setCurrentBudget(BigDecimal.ZERO);
      categoryRepository.save(expenseCategory);
    }
  }

  //Create new category
  @Transactional
  public Category createCategory(String userId, String categoryName) {
    Users user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

    Category category = new Category();
    category.setName(categoryName);
    category.setExpenseType(ExpenseType.EXPENSE);
    category.setUser(user);
    category.setAllocatedBudget(BigDecimal.ZERO);
    category.setCurrentBudget(BigDecimal.ZERO);
    category.setColorHex(getNextAvailableColor(userId));

    return categoryRepository.save(category);
  }

  // Lay mau tiep theo
  private String getNextAvailableColor(String userId) {
    // Cac mau da dung
    Set<String> usedColors = categoryRepository.findByUserId(userId)
            .stream()
            .map(Category::getColorHex)
            .collect(Collectors.toSet());

    // Tim mau chua dung
    for(String color : COLOR_POOL) {
      if(!usedColors.contains(color)) {
        return color;
      }
    }

    return generateRandomColor();
  }

  private String generateRandomColor() {
    // Generate random hex color
    int r = (int) (Math.random() * 256);
    int g = (int) (Math.random() * 256);
    int b = (int) (Math.random() * 256);
    return String.format("#%02X%02X%02X", r, g, b);
  }

  // Get list categories cua user
  public List<Category> getUserCategories(String userId) {
    return categoryRepository.findByUserIdOrderByExpenseTypeDescCreatedAtAsc(userId);
  }

  // Cap nhat  category
  @Transactional
  public Category updateCategory(Long categoryId, String userId, String newName, BigDecimal newBudget) {
    Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(() -> new RuntimeException("Category không tồn tại"));

    // Đổi tên nếu truyền mới
    if (newName != null && !newName.trim().isEmpty()) {
      category.setName(newName);
    }

    // Đổi budget nếu truyền mới
    if (newBudget != null) {
      if (category.getExpenseType() != ExpenseType.INCOME) {
        BigDecimal incomeAmount = getIncomeAmount(userId);
        BigDecimal totalAllocated = getTotalAllocatedBudgetExcept(userId, categoryId);
        if (totalAllocated.add(newBudget).compareTo(incomeAmount) > 0) {
          throw new RuntimeException("Tổng ngân sách vượt quá thu nhập!!!");
        }
      }
      category.setAllocatedBudget(newBudget);
      category.setCurrentBudget(newBudget);
    }

    return categoryRepository.save(category);
  }

  // delete category
  @Transactional
  public void deleteCategory(Long categoryId, String userId) {
    Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElseThrow(() -> new RuntimeException("Category không tồn tại"));

    if (!category.getExpenses().isEmpty()) {
      throw new RuntimeException("Không thể xóa category có giao dịch!");
    }

    categoryRepository.delete(category);
  }

  // Helper methods
  private BigDecimal getIncomeAmount(String userId) {
    return categoryRepository.findByUserIdAndExpenseType(userId, ExpenseType.INCOME)
            .stream()
            .map(Category::getCurrentBudget)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private BigDecimal getTotalAllocatedBudgetExcept(String userId, Long excludeCategoryId) {
    return categoryRepository.findByUserIdAndExpenseTypeAndIdNot(userId, ExpenseType.EXPENSE, excludeCategoryId)
            .stream()
            .map(Category::getAllocatedBudget)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
