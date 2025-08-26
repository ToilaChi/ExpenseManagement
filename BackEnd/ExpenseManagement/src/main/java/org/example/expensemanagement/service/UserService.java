package org.example.expensemanagement.service;

import jakarta.transaction.Transactional;
import org.example.expensemanagement.dto.LoginRequest;
import org.example.expensemanagement.dto.LoginResponse;
import org.example.expensemanagement.dto.LogoutRequest;
import org.example.expensemanagement.dto.LogoutResponse;
import org.example.expensemanagement.models.RefreshToken;
import org.example.expensemanagement.models.Users;
import org.example.expensemanagement.repository.UserRepository;
import org.example.expensemanagement.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserService {
  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private TokenBlackListService tokenBlackListService;

  @Autowired
  private RefreshTokenService refreshTokenService;
  @Autowired
  private UserRepository userRepository;

  public LoginResponse login(LoginRequest loginRequest) {
    try {
      Users phone = userRepository.findByPhone(loginRequest.getPhone());
      if (phone == null) {
        return new LoginResponse(null, "Số điện thoại sai. Vui lòng nhập lại!!!");
      }

      if(!isPasswordValid(phone, loginRequest.getPassword())) {
        return new LoginResponse(null, "Mật khẩu không đúng. Vui lòng nhập lại!!!");
      }

      if(isPasswordValid(phone, loginRequest.getPassword())) {
        //Encode va save it
        if(!phone.getPassword().startsWith("$2a$") &&
                !phone.getPassword().startsWith("$2b$") &&
                !phone.getPassword().startsWith("$2y$")) {
          System.out.println("Updating password with encryption");
          updatePasswordWithEncryption(phone, loginRequest.getPassword());
        }

        // Create access token
        String accessToken = jwtUtil.generateAccessToken(phone.getFullName(), phone.getId());

        // Create refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(phone);
        String refreshTokenString = refreshToken.getToken();

        if (accessToken == null) {
          System.err.println("Generated token is null!");
          throw new RuntimeException("Failed to generate token");
        }

        // Get info user
        LoginResponse.AccountInfo accountInfo = new LoginResponse.AccountInfo(
                phone.getId(),
                phone.getFullName(),
                phone.getEmail(),
                phone.getPhone()
        );

        LoginResponse.DataInfo dataInfo = new LoginResponse.DataInfo(accessToken, refreshTokenString, accountInfo);

        return new LoginResponse(dataInfo, "Đăng nhập thành công!!!");
      } else {
        System.out.println("Lỗi mật khẩu với người dùng: " + phone.getFullName());
        throw new RuntimeException("Lỗi mật khẩu hoặc người dùng");
      }
    }
    catch (Exception e) {
      System.err.println("Đăng nhập lỗi: " + e.getMessage());
      e.printStackTrace();
      throw new RuntimeException("Đăng nhập thất bại: " + e.getMessage(), e);
    }
  }

  @Transactional
  public LogoutResponse logout(LogoutRequest logoutRequest) {
    try {
      if(logoutRequest.getRefreshToken() != null) {
        RefreshToken refreshToken = refreshTokenService.findByToken(logoutRequest.getRefreshToken());

        String accessToken = logoutRequest.getAccessToken();
        if (accessToken != null && !accessToken.isEmpty()) {
          tokenBlackListService.addBlacklistToken(accessToken);
        }
        refreshTokenService.deleteByToken(refreshToken.getToken());
        return new LogoutResponse("Logout thành công!!!");
      }
      return new LogoutResponse("Refresh token không tìm thấy");
    }
    catch (Exception e) {
      return new LogoutResponse("Logout thất bại: " + e.getMessage());
    }
  }

  private boolean isPasswordValid(Users user, String rawPassword) {
    if (user.getPassword().startsWith("$2a$") ||
            user.getPassword().startsWith("$2b$") ||
            user.getPassword().startsWith("$2y$")) {
      // Nếu password đã được mã hóa, so sánh bằng passwordEncoder
      return passwordEncoder.matches(rawPassword, user.getPassword());
    } else {
      // Nếu password chưa mã hóa, so sánh trực tiếp
      return user.getPassword().equals(rawPassword);
    }
  }

  private void updatePasswordWithEncryption(Users user, String rawPassword) {
    user.setPassword(passwordEncoder.encode(rawPassword));
    userRepository.save(user);
  }
}
