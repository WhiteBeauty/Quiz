package com.Karyakina.Ustenko.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.Karyakina.Ustenko.model.User;

import java.util.List;
import java.util.Optional;

public interface UserDao extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("""
            SELECT u FROM User u
            WHERE u.id <> :selfId
              AND (LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')))
            ORDER BY u.username ASC
            """)
    List<User> searchByNicknameOrEmail(@Param("selfId") Long selfId, @Param("q") String q, Pageable pageable);
}
