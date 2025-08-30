package org.example.expensemanagement.controllers;

import org.example.expensemanagement.dto.user.UserInfoRequest;
import org.example.expensemanagement.dto.user.UserInfoResponse;
import org.example.expensemanagement.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/account/me")
  public ResponseEntity<UserInfoResponse> getAccountInfo(@RequestHeader("Authorization") String authHeader) {
    String token = null;
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      token = authHeader.substring(7);
    }

    UserInfoRequest  request = new UserInfoRequest(token);
    UserInfoResponse response = userService.getInfo(request);
    return ResponseEntity.ok(response);
  }
}
