package org.example.expensemanagement.controllers;

import org.example.expensemanagement.models.Users;
import org.example.expensemanagement.repository.UserRepository;
import org.example.expensemanagement.security.JwtUtil;
import org.example.expensemanagement.service.BudgetService;
import org.example.expensemanagement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/budget")
public class BudgetController {
  @Autowired
  private BudgetService budgetService;

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

  @PostMapping("/reset-my-budgets")
  public ResponseEntity<ApiResponse<String>> resetMyBudgets(
          @RequestHeader("Authorization") String authorizationHeader) {

    try {
      String accessToken = getTokenFromHeader(authorizationHeader);
      Users user = getCurrentUser(accessToken);

      if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>("User không tồn tại", null));
      }

      budgetService.resetBudgetsForUser(user.getId());

      return ResponseEntity.ok(new ApiResponse<>("Reset ngân sách của bạn thành công", "SUCCESS"));

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse<>("Lỗi: " + e.getMessage(), null));
    }
  }
}
