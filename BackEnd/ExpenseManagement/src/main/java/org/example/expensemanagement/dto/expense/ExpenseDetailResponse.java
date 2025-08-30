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
public class ExpenseDetailResponse {
  private List<ExpenseDetail> data;
  private String message;
  private PaginationInfo pagination;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ExpenseDetail {
    private LocalDateTime date;
    private String categoryName;
    private BigDecimal amount;
    private String description;
    private String colorHex;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class PaginationInfo {
    private int currentPage;
    private int totalPages;
    private long totalItems;
  }
}
