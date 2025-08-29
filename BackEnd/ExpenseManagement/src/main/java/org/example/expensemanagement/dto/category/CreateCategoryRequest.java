package org.example.expensemanagement.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCategoryRequest {
  private String categoryName;
  private BigDecimal allocatedBudget;
}
