package org.example.expensemanagement.dto.expense;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseListResponse {
  private List<ExpenseInfo> data;
  private String message;
  private PaginationInfo pagination;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ExpenseInfo {
    private Long id;
    private String categoryName;
    private String colorHex;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class PaginationInfo {
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
  }
}