package org.example.expensemanagement.service;

import jakarta.transaction.Transactional;
import org.example.expensemanagement.models.BudgetAudit;
import org.example.expensemanagement.models.Category;
import org.example.expensemanagement.models.ExpenseType;
import org.example.expensemanagement.repository.BudgetAuditRepository;
import org.example.expensemanagement.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BudgetService {
  private static final Logger log = LoggerFactory.getLogger(BudgetService.class);

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private BudgetAuditRepository budgetAuditRepository;

  // Reset budget vao 2h sang ngay 1 hang thang
  @Scheduled(cron = "${budget.reset.cron}")
  @Transactional
  public void autoResetMonthlyBudgets() {
    log.info("Start auto reset monthly budgets");

    try {
      List<Category> expenseCategories = categoryRepository.findAllByExpenseType(ExpenseType.EXPENSE);

      for(Category category : expenseCategories) {
        BigDecimal oldCurrentBudget = category.getCurrentBudget();

        //Reset currentBudget ve allocatedBudget
        category.setCurrentBudget(category.getAllocatedBudget());
        categoryRepository.save(category);

        //Record audit
        createAuditLog(
                "AUTO_RESET_MONTHLY",
                category.getUser().getId(),
                String.format("Auto reset budget for category '%s'. Old: %s, New: %s",
                        category.getName(), oldCurrentBudget, category.getAllocatedBudget())
        );

        log.debug("Reset budget for category '{}' (User: {})", category.getName(), category.getUser().getId());
      }
      log.info("Monthly budget reset completed for {} categories", expenseCategories.size());
    } catch (Exception e) {
      log.error("Error during monthly budget reset: ", e);
      throw e;
    }
  }

  @Transactional
  public void resetBudgetsForUser(String userId) {
    log.info("Resetting budgets for user: {}", userId);

    List<Category> userCategories = categoryRepository.findAllByUserIdAndExpenseType(userId, ExpenseType.EXPENSE);

    for (Category category : userCategories) {
      BigDecimal oldCurrentBudget = category.getCurrentBudget();

      category.setCurrentBudget(category.getAllocatedBudget());
      categoryRepository.save(category);

      createAuditLog(
              "USER_RESET",
              userId,
              String.format("User budget reset for category '%s'. Old: %s, New: %s",
                      category.getName(), oldCurrentBudget, category.getAllocatedBudget())
      );
    }

    log.info("Reset completed for {} categories of user: {}", userCategories.size(), userId);
  }

  private void createAuditLog(String action, String userId, String details) {
    BudgetAudit audit = new BudgetAudit();
    audit.setAction(action);
    audit.setUserId(userId);
    audit.setDetails(details);
    audit.setTimestamp(LocalDateTime.now());

    budgetAuditRepository.save(audit);
  }
}
