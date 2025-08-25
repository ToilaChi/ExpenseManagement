package org.example.expensemanagement.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Users")
public class Users {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(nullable = false, unique = true)
  private UUID id;

  @Column(nullable = false)
  private String fullName;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column
  private LocalDateTime createdAt;

  @Column
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Category> categories = new ArrayList<>();
}
