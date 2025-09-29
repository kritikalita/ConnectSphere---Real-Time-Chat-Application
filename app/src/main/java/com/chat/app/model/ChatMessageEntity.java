package com.chat.app.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sender;
    @Column(length = 1000)
    private String content;
    private LocalDateTime timestamp;
    @Enumerated(EnumType.STRING)
    private ChatMessage.MessageType type;

    // --- ADDED ---
    private String recipient; // For private messages

    private String imageUrl;
    private boolean edited = false;

    private Long repliedToMessageId;
    private String repliedToMessageSender;
    @Column(length = 200)
    private String repliedToMessageContent;

    private String linkUrl;
    private String linkTitle;
    @Column(length = 1000)
    private String linkDescription;
    private String linkImageUrl;
    private String linkSiteName;

    private String fileUrl;
    private Long fileSize;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<MessageReaction> reactions = new HashSet<>();
}