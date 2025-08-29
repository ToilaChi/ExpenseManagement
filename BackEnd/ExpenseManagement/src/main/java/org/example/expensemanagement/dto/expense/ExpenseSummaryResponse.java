package org.example.expensemanagement.dto.expense;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseSummaryResponse {
  private List<CategorySummary> data;
  private String message;
  private SummaryStats stats;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CategorySummary {
    private String categoryName;
    private String colorHex;
    private BigDecimal totalSpent;
    private BigDecimal percentage;
    private int transactionCount;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class SummaryStats {
    private BigDecimal grandTotal;
    private String filterType;
    private String filterDate;
  }
}
