package org.example.expensemanagement.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.expensemanagement.models.ExpenseType;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse {
  private CategoryInfo data;
  private String message;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class CategoryInfo {
    private Long id;
    private String name;
    private String colorHex;
    private ExpenseType expenseType;
    private BigDecimal allocatedBudget;
    private BigDecimal currentBudget;
  }
}
