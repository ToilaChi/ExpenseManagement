package org.example.expensemanagement.dto.expense;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseFilterRequest {
  private String filterType = "month";
  private String date;
  private int page = 0;
  private int size = 20;
}
