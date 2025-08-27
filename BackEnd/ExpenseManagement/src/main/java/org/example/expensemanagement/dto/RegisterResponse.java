package org.example.expensemanagement.dto;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RegisterResponse {
  private String message;
  private DataInfo data;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class DataInfo {
    private String accessToken;
    private String refreshToken;
    private AccountInfo account;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class AccountInfo {
    private String id;
    private String username;
    private String phone;
    private String email;
  }
}
