package org.example.expensemanagement.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetUpdateResponse {
  private String message;
  private BigDecimal newBudget;
  private BigDecimal totalAllocated;
  private BigDecimal remainingIncome;
}
