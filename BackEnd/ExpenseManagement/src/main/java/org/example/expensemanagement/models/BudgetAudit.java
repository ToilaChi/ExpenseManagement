package org.example.expensemanagement.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "budget_audit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetAudit {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 50)
  private String action; // AUTO_RESET_MONTHLY, MANUAL_RESET, USER_RESET, etc.

  @Column(nullable = false, length = 50)
  private String userId;

  @Column(nullable = false, length = 500)
  private String details;

  @Column(nullable = false)
  private LocalDateTime timestamp;

  public BudgetAudit(String action, String userId, String details) {
    this.action = action;
    this.userId = userId;
    this.details = details;
    this.timestamp = LocalDateTime.now();
  }
}
