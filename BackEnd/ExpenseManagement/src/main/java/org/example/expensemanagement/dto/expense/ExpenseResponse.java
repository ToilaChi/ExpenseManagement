package org.example.expensemanagement.dto.expense;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.expensemanagement.models.ExpenseType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseResponse {
  private ExpenseInfo data;
  private String message;

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
    private LocalDateTime updatedAt;
    private BigDecimal categoryCurrentBudget;
  }
}
