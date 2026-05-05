package com.Karyakina.Ustenko.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Karyakina.Ustenko.model.User;

import java.util.Optional;

public interface UserDao extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
