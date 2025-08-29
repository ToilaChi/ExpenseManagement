package org.example.expensemanagement.controllers;

import org.example.expensemanagement.dto.category.*;
import org.example.expensemanagement.models.Category;
import org.example.expensemanagement.models.Users;
import org.example.expensemanagement.repository.UserRepository;
import org.example.expensemanagement.security.JwtUtil;
import org.example.expensemanagement.service.CategoryService;
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
  public ResponseEntity<CategoryResponse> createCategory(
          @RequestHeader("Authorization") String authorizationHeader,
          @RequestBody CreateCategoryRequest request
  ) {
    try {
      String accessToken = getTokenFromHeader(authorizationHeader);
      Users user  = getCurrentUser(accessToken);
      if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new CategoryResponse(null, "User không tồn tại"));
      }

      Category category = categoryService.createCategory(user.getId(), request.getCategoryName(), request.getAllocatedBudget());

      CategoryResponse.CategoryInfo categoryInfo = new CategoryResponse.CategoryInfo(
              category.getId(),
              category.getName(),
              category.getColorHex(),
              category.getExpenseType(),
              category.getAllocatedBudget(),
              category.getCurrentBudget()
      );

      return ResponseEntity.ok(new CategoryResponse(categoryInfo,
              "Tạo category thành công"));
    }
    catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new CategoryResponse(null, "Lỗi: " + e.getMessage()));
    }
  }

  @PatchMapping("/update/{id}")
  public ResponseEntity<CategoryResponse> updateCategory(
          @RequestHeader("Authorization") String authorizationHeader,
          @PathVariable("id") Long categoryId,
          @RequestBody UpdateCategoryRequest request) {
    try {
      String accessToken = getTokenFromHeader(authorizationHeader);
      Users user  = getCurrentUser(accessToken);
      if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new CategoryResponse(null, "User không tồn tại"));
      }

      Category category = categoryService.updateCategory(
              categoryId,
              user.getId(),
              request.getCategoryName(),
              request.getBudget()
      );

      CategoryResponse.CategoryInfo categoryInfo = new CategoryResponse.CategoryInfo(
              category.getId(),
              category.getName(),
              category.getColorHex(),
              category.getExpenseType(),
              category.getAllocatedBudget(),
              category.getCurrentBudget()
      );

      return ResponseEntity.ok(new CategoryResponse(categoryInfo,
              "Cập nhật category thành công"));
    }
    catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new CategoryResponse(null, "Lỗi: " + e.getMessage()));
    }
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<CategoryResponse> deleteCategory(
          @RequestHeader("Authorization") String authorizationHeader,
          @PathVariable("id") Long categoryId) {
    try {
      String accessToken = getTokenFromHeader(authorizationHeader);
      Users user  = getCurrentUser(accessToken);
      if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new CategoryResponse(null, "User không tồn tại"));
      }

      categoryService.deleteCategory(categoryId, user.getId());

      return ResponseEntity.ok(new CategoryResponse(null,
              "Xóa category thành công"));
    }
    catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
              .body(new CategoryResponse(null, "Lỗi: " + e.getMessage()));
    }
  }

  //Helper method
  private String getTokenFromHeader(String header) {
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
  }
}
