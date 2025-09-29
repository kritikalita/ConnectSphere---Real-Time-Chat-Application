package com.chat.app.repository;

import com.chat.app.model.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    // --- NEW: Efficiently finds public messages ---
    List<ChatMessageEntity> findByRecipientIsNullOrderByTimestampAsc();

    // --- Your excellent query for private conversations ---
    @Query("SELECT m FROM ChatMessageEntity m WHERE (m.sender = :user1 AND m.recipient = :user2) OR (m.sender = :user2 AND m.recipient = :user1) ORDER BY m.timestamp ASC")
    List<ChatMessageEntity> findConversationHistory(@Param("user1") String user1, @Param("user2") String user2);

    // --- NEW: Efficiently finds all of a user's conversation partners ---
    @Query("SELECT m.recipient FROM ChatMessageEntity m WHERE m.sender = :username AND m.recipient IS NOT NULL")
    Set<String> findRecipients(@Param("username") String username);

    @Query("SELECT m.sender FROM ChatMessageEntity m WHERE m.recipient = :username")
    Set<String> findSenders(@Param("username") String username);
}
