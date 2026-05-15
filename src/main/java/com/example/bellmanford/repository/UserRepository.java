package com.example.bellmanford.repository;

import com.example.bellmanford.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, java.util.UUID> {
    Optional<User> findByUsername(String username);
    List<User> findByRoleAndActiveTrue(User.UserRole role);
}
