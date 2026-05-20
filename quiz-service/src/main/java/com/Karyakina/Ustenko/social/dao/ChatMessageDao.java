package com.Karyakina.Ustenko.social.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.Karyakina.Ustenko.social.model.ChatMessage;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatMessageDao extends JpaRepository<ChatMessage, Long> {

    @Modifying
    @Query("""
            UPDATE ChatMessage m SET m.deliveredAt = :now
            WHERE m.recipientId = :me AND m.senderId = :peer AND m.deliveredAt IS NULL
            """)
    int markDeliveredToMeFromPeer(@Param("me") Long me, @Param("peer") Long peer, @Param("now") LocalDateTime now);

    @Modifying
    @Query("""
            UPDATE ChatMessage m SET m.readAt = :now
            WHERE m.recipientId = :me AND m.senderId = :peer AND m.id <= :upToId AND m.readAt IS NULL
            """)
    int markReadUpTo(@Param("me") Long me, @Param("peer") Long peer, @Param("upToId") Long upToId, @Param("now") LocalDateTime now);

    @Query("""
            SELECT m.senderId, COUNT(m) FROM ChatMessage m
            WHERE m.recipientId = :userId AND m.readAt IS NULL
            GROUP BY m.senderId
            """)
    List<Object[]> countUnreadBySender(@Param("userId") Long userId);

    @Query("""
            SELECT COUNT(m) FROM ChatMessage m
            WHERE m.recipientId = :userId AND m.readAt IS NULL
            """)
    long countUnreadMessages(@Param("userId") Long userId);
}
