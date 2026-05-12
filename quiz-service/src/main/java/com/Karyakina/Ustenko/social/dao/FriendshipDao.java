package com.Karyakina.Ustenko.social.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.Karyakina.Ustenko.social.model.Friendship;

import java.util.List;
import java.util.Optional;

public interface FriendshipDao extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByUser1IdAndUser2Id(Long user1Id, Long user2Id);

    @Query("""
            SELECT f FROM Friendship f
            WHERE f.status = 'ACCEPTED' AND (f.user1Id = :uid OR f.user2Id = :uid)
            ORDER BY f.createdAt DESC
            """)
    List<Friendship> findAcceptedForUser(@Param("uid") Long userId);

    @Query("""
            SELECT f FROM Friendship f
            WHERE f.status = 'PENDING' AND (f.user1Id = :uid OR f.user2Id = :uid) AND f.initiatorId <> :uid
            ORDER BY f.createdAt DESC
            """)
    List<Friendship> findPendingIncoming(@Param("uid") Long userId);

    @Query("""
            SELECT f FROM Friendship f
            WHERE f.status = 'PENDING' AND f.initiatorId = :uid
            ORDER BY f.createdAt DESC
            """)
    List<Friendship> findPendingOutgoing(@Param("uid") Long userId);
}
