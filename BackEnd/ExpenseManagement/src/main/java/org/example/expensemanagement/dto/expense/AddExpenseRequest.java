package org.example.expensemanagement.dto.expense;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddExpenseRequest {
  private Long categoryId;
  private BigDecimal amount;
  private String description;
}
