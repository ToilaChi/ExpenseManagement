package org.example.expensemanagement.controllers;

import org.example.expensemanagement.dto.category.*;
import org.example.expensemanagement.models.Category;
import org.example.expensemanagement.models.Users;
import org.example.expensemanagement.repository.UserRepository;
import org.example.expensemanagement.security.JwtUtil;
import org.example.expensemanagement.service.CategoryService;
import org.example.expensemanagement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
  @Autowired
  private CategoryService categoryService;

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

  //Get list
  @GetMapping("/list")
  public ResponseEntity<CategoryListResponse> getCategories(@RequestHeader("Authorization") String authorizationHeader
  ) {
    try {
      String accessToken = getTokenFromHeader(authorizationHeader);
      Users user  = getCurrentUser(accessToken);
      if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new CategoryListResponse(null, "User không tồn tại"));
      }
      List<Category> categories = categoryService.getUserCategories(user.getId());

      List<CategoryListResponse.CategoryInfo> categoryInfos = categories
              .stream()
              .map(cat -> new CategoryListResponse.CategoryInfo(
                      cat.getId(),
                      cat.getName(),
                      cat.getColorHex(),
                      cat.getExpenseType(),
                      cat.getAllocatedBudget(),
                      cat.getCurrentBudget()
              ))
              .toList();

      return ResponseEntity.ok(new CategoryListResponse(categoryInfos,
              "Lấy danh sách categories thành công"));
    }
    catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new CategoryListResponse(null, "Lỗi: " + e.getMessage()));
    }
  }

  @PostMapping("/create")
  public ResponseEntity<ApiResponse<CategoryResponse.CategoryInfo>> createCategory(
          @RequestHeader("Authorization") String authorizationHeader,
          @RequestBody CreateCategoryRequest request
  ) {
    try {
      String accessToken = getTokenFromHeader(authorizationHeader);
      Users user  = getCurrentUser(accessToken);
      if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>("User không tồn tại", null));
      }

      ApiResponse<CategoryResponse.CategoryInfo> serviceResponse = categoryService.createCategory(
              user.getId(),
              request.getCategoryName(),
              request.getAllocatedBudget()
      );

      return ResponseEntity.ok(serviceResponse);
    }
    catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse<>("Lỗi: " + e.getMessage(), null));
    }
  }

  @PatchMapping("/update/{id}")
  public ResponseEntity<ApiResponse<CategoryResponse.CategoryInfo>> updateCategory(
          @RequestHeader("Authorization") String authorizationHeader,
          @PathVariable("id") Long categoryId,
          @RequestBody UpdateCategoryRequest request) {
    try {
      String accessToken = getTokenFromHeader(authorizationHeader);
      Users user  = getCurrentUser(accessToken);
      if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>("User không tồn tại", null));
      }

      ApiResponse<CategoryResponse.CategoryInfo> serviceResponse = categoryService.updateCategory(
              categoryId,
              user.getId(),
              request.getCategoryName(),
              request.getBudget()
      );

      return ResponseEntity.ok(serviceResponse);
    }
    catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse<>("Lỗi: " + e.getMessage(), null));
    }
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteCategory(
          @RequestHeader("Authorization") String authorizationHeader,
          @PathVariable("id") Long categoryId) {
    try {
      String accessToken = getTokenFromHeader(authorizationHeader);
      Users user  = getCurrentUser(accessToken);
      if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>("User không tồn tại", null));
      }

      ApiResponse<Void> serviceResponse = categoryService.deleteCategory(categoryId, user.getId());

      return ResponseEntity.ok(serviceResponse);
    }
    catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse<>("Lỗi: " + e.getMessage(), null));
    }
  }
}
