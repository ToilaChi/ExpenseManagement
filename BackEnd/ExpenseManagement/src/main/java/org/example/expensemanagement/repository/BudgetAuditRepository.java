package org.example.expensemanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.example.expensemanagement.models.BudgetAudit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BudgetAuditRepository extends JpaRepository<BudgetAudit, Long> {

  List<BudgetAudit> findByUserIdOrderByTimestampDesc(String userId);

  List<BudgetAudit> findByActionOrderByTimestampDesc(String action);

  @Query("SELECT ba FROM BudgetAudit ba WHERE ba.timestamp BETWEEN :startDate AND :endDate ORDER BY ba.timestamp DESC")
  List<BudgetAudit> findByTimestampBetween(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);
}
