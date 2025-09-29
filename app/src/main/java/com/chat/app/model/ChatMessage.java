package com.chat.app.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
public class ChatMessage {

    private Long id;
    private String content; // Will hold filename for FILE type
    private String sender;
    private MessageType type;
    private Set<String> users;
    private String recipient;
    private String imageUrl;
    private Map<String, Set<String>> reactions;
    private String timestamp;
    private boolean edited;

    private Long repliedToMessageId;
    private String repliedToMessageSender;
    private String repliedToMessageContent;

    private String linkUrl;
    private String linkTitle;
    private String linkDescription;
    private String linkImageUrl;
    private String linkSiteName;


    private String fileUrl;
    private Long fileSize;
    private String senderAvatarUrl;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        PRIVATE,
        TYPING,
        GIF,
        EDIT,
        DELETE,
        REACTION,
        FILE,
        MENTION
    }
}

