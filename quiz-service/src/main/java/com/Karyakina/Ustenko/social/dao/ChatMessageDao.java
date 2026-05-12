package com.Karyakina.Ustenko.social.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.Karyakina.Ustenko.social.model.ChatMessage;

import java.time.LocalDateTime;

public interface ChatMessageDao extends JpaRepository<ChatMessage, Long> {

    @Query("""
            SELECT m FROM ChatMessage m
            WHERE ((m.senderId = :a AND m.recipientId = :b) OR (m.senderId = :b AND m.recipientId = :a))
              AND (:before IS NULL OR m.createdAt < :before)
            ORDER BY m.createdAt DESC
            """)
    Page<ChatMessage> findConversation(
            @Param("a") Long userA,
            @Param("b") Long userB,
            @Param("before") LocalDateTime before,
            Pageable pageable
    );

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
}
