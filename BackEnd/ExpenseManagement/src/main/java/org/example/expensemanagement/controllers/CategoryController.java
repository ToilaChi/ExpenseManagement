package org.example.expensemanagement.controllers;

import org.example.expensemanagement.dto.category.CategoryListResponse;
import org.example.expensemanagement.dto.category.GetCategoriesRequest;
import org.example.expensemanagement.models.Category;
import org.example.expensemanagement.models.Users;
import org.example.expensemanagement.repository.UserRepository;
import org.example.expensemanagement.security.JwtUtil;
import org.example.expensemanagement.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

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
  @PostMapping("/list")
  public ResponseEntity<CategoryListResponse> getCategories(@RequestBody GetCategoriesRequest request) {
    try {
      Users user  = getCurrentUser(request.getAccessToken());
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
}
