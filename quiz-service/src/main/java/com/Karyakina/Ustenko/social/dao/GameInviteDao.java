package com.Karyakina.Ustenko.social.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import com.Karyakina.Ustenko.social.model.GameInvite;

import java.util.Optional;

public interface GameInviteDao extends JpaRepository<GameInvite, Long> {

    Optional<GameInvite> findByTokenHash(String tokenHash);
}
