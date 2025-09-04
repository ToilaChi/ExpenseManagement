package org.example.expensemanagement.service;

import jakarta.transaction.Transactional;
import org.example.expensemanagement.dto.expense.ExpenseDetailResponse;
import org.example.expensemanagement.dto.expense.ExpenseResponse;
import org.example.expensemanagement.dto.expense.ExpenseSummaryResponse;
import org.example.expensemanagement.models.Category;
import org.example.expensemanagement.models.Expense;
import org.example.expensemanagement.models.ExpenseType;
import org.example.expensemanagement.models.Users;
import org.example.expensemanagement.repository.CategoryRepository;
import org.example.expensemanagement.repository.ExpenseRepository;
import org.example.expensemanagement.repository.UserRepository;
import org.example.expensemanagement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExpenseService {
  @Autowired
  private ExpenseRepository expenseRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private UserRepository userRepository;

  // Them chi tieu
  public ApiResponse<ExpenseResponse.ExpenseInfo> addExpense(String userId, Long categoryId, BigDecimal amount, String description) {
    // Kiem tra user
    Users user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User không tồn tại"));

    // Kiem tra cate co thuoc ve user do khong
    Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
            .orElse(null);

    if (category == null) {
      return new ApiResponse<>("Category không tồn tại hoặc không thuộc về bạn");
    }

    if (category.getExpenseType() != ExpenseType.EXPENSE) {
      return new ApiResponse<>("Không thể thêm chi tiêu vào category INCOME");
    }

    //Validate amount > 0
    if (amount == null ||  amount.compareTo(BigDecimal.ZERO) <= 0) {
      return new ApiResponse<>("Số tiền phải lớn hơn 0");
    }

    // warning neu vuot qua so budget (nhung van cho phep)
    BigDecimal currentBudget = category.getCurrentBudget();
    boolean isOverBudget = currentBudget.compareTo(amount) < 0;

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

    ExpenseResponse.ExpenseInfo expenseInfo = new ExpenseResponse.ExpenseInfo(
            savedExpense.getId(),
            savedExpense.getCategory().getName(),
            savedExpense.getCategory().getColorHex(),
            savedExpense.getAmount(),
            savedExpense.getDescription(),
            savedExpense.getCreatedAt(),
            savedExpense.getUpdatedAt(),
            savedExpense.getCategory().getCurrentBudget()
    );

    String message = isOverBudget ?
            "Thêm chi tiêu thành công (Cảnh báo: Vượt ngân sách!)" :
            "Thêm chi tiêu thành công";

    return new ApiResponse<>(message, expenseInfo);
  }

  // Update expense
  @Transactional
  public ApiResponse<ExpenseResponse.ExpenseInfo> updateExpense(String userId, Long expenseId, String newCategoryName, BigDecimal newAmount, String newDescription) {
    // Tim expense va validate ownership
    Expense expense = expenseRepository.findByIdAndCategoryUserId(expenseId, userId)
            .orElse(null);

    if (expense == null) {
      return new ApiResponse<>("Expense không tồn tại hoặc không thuộc về bạn");
    }

    Category oldCategory = expense.getCategory();
    BigDecimal oldAmount = expense.getAmount();

    //Validate new category neu update
    Category newCategory = oldCategory;
    if (newCategoryName != null && !newCategoryName.trim().isEmpty()
            && !newCategoryName.equals(oldCategory.getName())) {
      newCategory = categoryRepository.findByNameAndUserId(newCategoryName, userId)
              .orElse(null);

      if (newCategory == null) {
        return new ApiResponse<>("Category '" + newCategoryName + "' không tồn tại");
      }

      if (newCategory.getExpenseType() != ExpenseType.EXPENSE) {
        return new ApiResponse<>("Không thể chuyển expense sang category INCOME");
      }
    }

    if (newAmount != null && newAmount.compareTo(BigDecimal.ZERO) <= 0) {
      return new ApiResponse<>("Số tiền phải lớn hơn 0");
    }

    //Validate new amount
    BigDecimal finalAmount = (newAmount != null && newAmount.compareTo(BigDecimal.ZERO) > 0) ? newAmount : oldAmount;

    //Update budget cua categories
    // Hoan lai tien cho old category
    oldCategory.setCurrentBudget(oldCategory.getCurrentBudget().add(oldAmount));

    //Tru tien tu new category
    newCategory.setCurrentBudget(newCategory.getCurrentBudget().subtract(finalAmount));

    //Cap nhat expense
    expense.setAmount(finalAmount);
    expense.setCategory(newCategory);
    if(newDescription != null) {
      expense.setDescription(newDescription);
    }
    expense.setUpdatedAt(LocalDateTime.now());

    categoryRepository.save(oldCategory);
    if(!oldCategory.equals(newCategory)) {
      categoryRepository.save(newCategory);
    }

    Expense savedExpense = expenseRepository.save(expense);

    ExpenseResponse.ExpenseInfo expenseInfo = new ExpenseResponse.ExpenseInfo(
            savedExpense.getId(),
            savedExpense.getCategory().getName(),
            savedExpense.getCategory().getColorHex(),
            savedExpense.getAmount(),
            savedExpense.getDescription(),
            savedExpense.getCreatedAt(),
            savedExpense.getUpdatedAt(),
            savedExpense.getCategory().getCurrentBudget()
    );

    return new ApiResponse<>("Cập nhật chi tiêu thành công", expenseInfo);
  }

  //Delete expense
  @Transactional
  public ApiResponse<Void> deleteExpense(String userId, Long expenseId) {
    try {
      Expense expense = expenseRepository.findByIdAndCategoryUserId(expenseId, userId)
              .orElse(null);

      if (expense == null) {
        return new ApiResponse<>("Expense không tồn tại hoặc không thuộc về bạn");
      }

      Category category = expense.getCategory();
      BigDecimal amount = expense.getAmount();

      // Hoàn lại tiền cho category
      category.setCurrentBudget(category.getCurrentBudget().add(amount));
      categoryRepository.save(category);

      expenseRepository.delete(expense);

      return new ApiResponse<>("Xóa chi tiêu thành công", null);

    } catch (Exception e) {
      return new ApiResponse<>("Lỗi: " + e.getMessage());
    }
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

  // Summary cho pie chart
  public List<ExpenseSummaryResponse.CategorySummary> getExpenseSummary(
          String userId, String filterType, String date) {
    Page<Expense> expenses = getFilteredExpenses(userId, filterType, date, 0, Integer.MAX_VALUE);

    // Group category va tinh tong
    Map<String, BigDecimal> categoryTotals = expenses.getContent()
            .stream()
            .collect(Collectors.groupingBy(
                    expense -> expense.getCategory().getName(),
                    Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
            ));

    BigDecimal grandTotal = categoryTotals.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return categoryTotals.entrySet().stream()
            .map(entry -> {
              String categoryName = entry.getKey();
              BigDecimal totalSpent = entry.getValue();

              Category category = categoryRepository.findByNameAndUserId(categoryName, userId)
                      .orElse(null);
              String colorHex = category != null ? category.getColorHex() : "#CCCCCC";

              // Tinh percentage
              BigDecimal percentage = grandTotal.compareTo(BigDecimal.ZERO) > 0
                      ? totalSpent.multiply(BigDecimal.valueOf(100))
                        .divide(grandTotal, 1, RoundingMode.HALF_UP)
                      : BigDecimal.ZERO;

              // Count transactions
              int transactionCount = (int) expenses.getContent().stream()
                      .filter(exp -> exp.getCategory().getName().equals(categoryName))
                      .count();

              return new ExpenseSummaryResponse.CategorySummary(
                      categoryName, colorHex, totalSpent, percentage, transactionCount
              );
            })
            .toList();
  }

  //Summary stats
  public ExpenseSummaryResponse.SummaryStats getSummaryStats(String userId, String filterType, String date) {
    Page<Expense> expenses = getFilteredExpenses(userId, filterType, date, 0, Integer.MAX_VALUE);

    BigDecimal grandTotal = expenses.getContent().stream()
            .map(Expense::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new ExpenseSummaryResponse.SummaryStats(grandTotal, filterType, date);
  }

  // Detail table cho power BI
  public Page<ExpenseDetailResponse.ExpenseDetail> getExpenseDetail(
          String userId, String filterType, String date, int page, int size) {
    Page<Expense> expensePage = getFilteredExpenses(userId, filterType, date, page, size);

    List<ExpenseDetailResponse.ExpenseDetail> details = expensePage.getContent().stream()
            .map(expense -> new ExpenseDetailResponse.ExpenseDetail(
                    expense.getCreatedAt(),
                    expense.getCategory().getName(),
                    expense.getAmount(),
                    expense.getDescription(),
                    expense.getCategory().getColorHex()
            ))
            .toList();

    return new PageImpl<>(details, expensePage.getPageable(),
            expensePage.getTotalElements());
  }
}
