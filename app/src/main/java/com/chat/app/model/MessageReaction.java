package com.chat.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode; // NEW import
import lombok.Getter;           // NEW import
import lombok.NoArgsConstructor;
import lombok.Setter;           // NEW import
import lombok.ToString;         // NEW import

@Entity
// REPLACE @Data with these more specific annotations
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MessageReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reaction;
    private String username;

    // This is the field causing the loop. Exclude it.
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id")
    @JsonIgnore
    private ChatMessageEntity chatMessage;

    public MessageReaction(String reaction, String username, ChatMessageEntity chatMessage) {
        this.reaction = reaction;
        this.username = username;
        this.chatMessage = chatMessage;
    }
}