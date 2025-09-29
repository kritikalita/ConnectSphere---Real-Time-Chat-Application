package com.chat.app.repository;

import com.chat.app.model.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {
    // Finds a reaction by message ID, username, and the reaction emoji
    Optional<MessageReaction> findByChatMessageIdAndUsernameAndReaction(Long messageId, String username, String reaction);
}
