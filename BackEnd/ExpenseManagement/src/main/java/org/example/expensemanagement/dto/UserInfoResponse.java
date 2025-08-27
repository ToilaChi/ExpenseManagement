package org.example.expensemanagement.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
public class UserInfoResponse {
  private DataInfo data;
  private String message;

  public UserInfoResponse(DataInfo data, String message) {
    this.data = data;
    this.message = message;
  }

  @Data
  public static class DataInfo {
    private UserInfoResponse.AccountInfo account;

    public DataInfo(UserInfoResponse.AccountInfo account) {
      this.account = account;
    }
  }

  @Getter
  public static class AccountInfo {
    private final String id;
    private final String fullName;
    private final String phone;
    private final String email;

    public AccountInfo(String id, String fullName, String phone, String email) {
      this.id = id;
      this.fullName = fullName;
      this.phone = phone;
      this.email = email;
    }
  }
}
