package org.example.expensemanagement.controllers;

import org.example.expensemanagement.dto.auth.*;
import org.example.expensemanagement.dto.user.RegisterRequest;
import org.example.expensemanagement.dto.user.RegisterResponse;
import org.example.expensemanagement.models.RefreshToken;
import org.example.expensemanagement.models.Users;
import org.example.expensemanagement.security.JwtUtil;
import org.example.expensemanagement.service.RefreshTokenService;
import org.example.expensemanagement.service.UserService;
import org.example.expensemanagement.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
  @Autowired
  private UserService userService;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private RefreshTokenService refreshTokenService;

  @PostMapping("/register")
  public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
    try {
      RegisterResponse response = userService.register(registerRequest);
      if (response.getData() != null) {
        return ResponseEntity.ok(response);
      } else {
        return ResponseEntity.badRequest().body(response);
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new RegisterResponse("Lỗi server: " + e.getMessage(), null));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
    try {
      LoginResponse response = userService.login(loginRequest);
      if (response.getData() != null) {
        return ResponseEntity.ok(response);
      } else {
        return ResponseEntity.badRequest().body(response);
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new LoginResponse(null, "Lỗi server: " + e.getMessage()));
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<LogoutResponse> logout(@RequestBody LogoutRequest logoutRequest) {
    try {
      LogoutResponse response = userService.logout(logoutRequest);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
              .body(new LogoutResponse("Lỗi server: " + e.getMessage()));
    }
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<ApiResponse<RefreshTokenResponse>> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
    try {
      // Kiem tra token xem ton tai va con han khong
      RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenRequest.getRefreshToken());
      refreshTokenService.verifyExpiration(refreshToken);

      // Tao access token moi
      Users user = refreshToken.getUser();
      String newAccessToken = jwtUtil.generateAccessToken(user.getPhone(), user.getId());

      return ResponseEntity.ok(new ApiResponse<>("", new RefreshTokenResponse(refreshToken.getToken(), newAccessToken)));
    }
    catch (Exception e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }
}
