package org.example.expensemanagement.controllers;

import org.example.expensemanagement.dto.expense.*;
import org.example.expensemanagement.models.Expense;
import org.example.expensemanagement.models.Users;
import org.example.expensemanagement.repository.ExpenseRepository;
import org.example.expensemanagement.repository.UserRepository;
import org.example.expensemanagement.security.JwtUtil;
import org.example.expensemanagement.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {
  @Autowired
  private ExpenseService expenseService;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private UserRepository userRepository;


  //Helper method
  private String getTokenFromHeader(String header) {
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
  }
  private Users getCurrentUser(String accessToken) {
    String phone = jwtUtil.validateTokenAndRetrieveSubject(accessToken);
    return userRepository.findByPhone(phone);
  }

  @PostMapping("/add")
  public ResponseEntity<ExpenseResponse> addExpense(
          @RequestHeader("Authorization") String authorizationHeader,
          @RequestBody AddExpenseRequest request
  ) {
    try {
      String accessToken = getTokenFromHeader(authorizationHeader);
      Users user = getCurrentUser(accessToken);

      if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ExpenseResponse(null, "User không tồn tại"));
      }

      Expense expense = expenseService.addExpense(
              user.getId(),
              request.getCategoryId(),
              request.getAmount(),
              request.getDescription()
      );

      ExpenseResponse.ExpenseInfo expenseInfo = new ExpenseResponse.ExpenseInfo(
              expense.getId(),
              expense.getCategory().getName(),
              expense.getCategory().getColorHex(),
              expense.getAmount(),
              expense.getDescription(),
              expense.getCreatedAt(),
              expense.getUpdatedAt(),
              expense.getCategory().getCurrentBudget()
      );

      return ResponseEntity.ok(new ExpenseResponse(expenseInfo,
              "Thêm chi tiêu thành công"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ExpenseResponse(null, "Lỗi: " + e.getMessage()));
    }
  }

  @GetMapping("list")
  public ResponseEntity<ExpenseListResponse> getExpenses(
          @RequestHeader("Authorization") String authorizationHeader,
          @RequestParam(defaultValue = "month") String filterType,
          @RequestParam(defaultValue = "2025-01") String date,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "20") int size) {
    try {
      String accessToken = getTokenFromHeader(authorizationHeader);
      Users user = getCurrentUser(accessToken);

      if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ExpenseListResponse(null, "User không tồn tại", null));
      }

      Page<Expense> expensePage = expenseService.getFilteredExpenses(
              user.getId(), filterType, date, page, size
      );

      List<ExpenseListResponse.ExpenseInfo> expenseInfos = expensePage.getContent()
              .stream()
              .map(expense -> new ExpenseListResponse.ExpenseInfo(
                      expense.getId(),
                      expense.getCategory().getName(),
                      expense.getCategory().getColorHex(),
                      expense.getAmount(),
                      expense.getDescription(),
                      expense.getCreatedAt()
              ))
              .toList();

      ExpenseListResponse.PaginationInfo paginationInfo = new ExpenseListResponse.PaginationInfo(
              expensePage.getNumber(),
              expensePage.getTotalPages(),
              expensePage.getTotalElements(),
              expensePage.getSize()
      );

      return ResponseEntity.ok(new ExpenseListResponse(expenseInfos,
              "Lấy danh sách chi tiêu thành công", paginationInfo));
    }
    catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ExpenseListResponse(null, "Lỗi: " + e.getMessage(), null));
    }
  }

  @PutMapping("/update/{expenseId}")
  public ResponseEntity<ExpenseResponse> updateExpense(
          @RequestHeader("Authorization") String authorizationHeader,
          @PathVariable Long expenseId,
          @RequestBody UpdateExpenseRequest request) {
    try {
      String accessToken = getTokenFromHeader(authorizationHeader);
      Users user = getCurrentUser(accessToken);

      if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ExpenseResponse(null, "User không tồn tại"));
      }

      Expense expense = expenseService.updateExpense(
              user.getId(),
              expenseId,
              request.getNewCategoryName(),
              request.getNewAmount(),
              request.getNewDescription()
      );

      ExpenseResponse.ExpenseInfo expenseInfo = new ExpenseResponse.ExpenseInfo(
              expense.getId(),
              expense.getCategory().getName(),
              expense.getCategory().getColorHex(),
              expense.getAmount(),
              expense.getDescription(),
              expense.getCreatedAt(),
              expense.getUpdatedAt(),
              expense.getCategory().getCurrentBudget()
      );

      return ResponseEntity.ok(new ExpenseResponse(expenseInfo,
              "Cập nhật chi tiêu thành công"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ExpenseResponse(null, "Lỗi: " + e.getMessage()));
    }
  }

  @DeleteMapping("/delete/{expenseId}")
  public ResponseEntity<ExpenseResponse> deleteExpense(
          @RequestHeader("Authorization") String authorizationHeader,
          @PathVariable Long expenseId) {
    try {
      String accessToken = getTokenFromHeader(authorizationHeader);
      Users user = getCurrentUser(accessToken);

      if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ExpenseResponse(null, "User không tồn tại"));
      }

      // Delete expense
      expenseService.deleteExpense(user.getId(), expenseId);

      return ResponseEntity.ok(new ExpenseResponse(null, "Xóa chi tiêu thành công"));

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ExpenseResponse(null, "Lỗi: " + e.getMessage()));
    }
  }
}
