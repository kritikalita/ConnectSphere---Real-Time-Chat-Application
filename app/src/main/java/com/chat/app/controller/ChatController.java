package com.chat.app.controller;

import com.chat.app.config.ChatRoom;
import com.chat.app.service.GiphyService;
import com.chat.app.service.LinkPreviewService;
import com.chat.app.model.*;
import com.chat.app.repository.ChatMessageRepository;
import com.chat.app.repository.MessageReactionRepository;
import com.chat.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    @Autowired private ChatRoom chatRoom;
    @Autowired private SimpMessageSendingOperations messagingTemplate;
    @Autowired private ChatMessageRepository messageRepository;
    @Autowired private GiphyService giphyService;
    @Autowired private MessageReactionRepository reactionRepository;
    @Autowired private LinkPreviewService linkPreviewService;
    @Autowired private UserRepository userRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");

    @GetMapping("/messages/{recipient}")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> getPrivateChatHistory(@PathVariable String recipient, Principal principal) {
        List<ChatMessageEntity> historyEntities = messageRepository.findConversationHistory(principal.getName(), recipient);
        List<ChatMessage> historyMessages = historyEntities.stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(historyMessages);
    }

    @GetMapping("/messages/public")
    @ResponseBody
    public ResponseEntity<List<ChatMessage>> getPublicChatHistory() {
        List<ChatMessageEntity> historyEntities = messageRepository.findAll()
                .stream()
                .filter(m -> m.getRecipient() == null)
                .sorted(Comparator.comparing(ChatMessageEntity::getTimestamp))
                .collect(Collectors.toList());
        List<ChatMessage> historyMessages = historyEntities.stream()
                .map(this::convertEntityToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(historyMessages);
    }

    @GetMapping("/conversations")
    @ResponseBody
    public ResponseEntity<Set<String>> getConversations(Principal principal) {
        List<ChatMessageEntity> messages = messageRepository.findAll();
        Set<String> conversations = new HashSet<>();
        for (ChatMessageEntity message : messages) {
            if (principal.getName().equals(message.getSender()) && message.getRecipient() != null) {
                conversations.add(message.getRecipient());
            }
            if (principal.getName().equals(message.getRecipient())) {
                conversations.add(message.getSender());
            }
        }
        return ResponseEntity.ok(conversations);
    }

    // --- MODIFIED: This is now the single endpoint for all messages ---
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage, Principal principal) {
        chatMessage.setSender(principal.getName());

        boolean isPrivate = chatMessage.getRecipient() != null && !chatMessage.getRecipient().isEmpty();

        // Mentions are only for public chat
        if (!isPrivate) {
            handleMentions(chatMessage);
        }

        // Handle special message types
        if (chatMessage.getContent() != null && chatMessage.getContent().startsWith("/gif")) {
            handleGifMessage(chatMessage);
        } else if (chatMessage.getType() == ChatMessage.MessageType.FILE) {
            saveAndBroadcast(chatMessage);
        } else {
            // Set standard message type and save
            chatMessage.setType(isPrivate ? ChatMessage.MessageType.PRIVATE : ChatMessage.MessageType.CHAT);
            saveAndBroadcast(chatMessage);
        }
    }

    private void handleMentions(ChatMessage chatMessage) {
        String content = chatMessage.getContent();
        if (content == null) return;
        Matcher matcher = MENTION_PATTERN.matcher(content);
        Set<String> mentionedUsers = new HashSet<>();
        StringBuffer contentWithMarkup = new StringBuffer();
        while (matcher.find()) {
            String username = matcher.group(1);
            if (!username.equals(chatMessage.getSender()) && userRepository.findByUsername(username).isPresent()) {
                mentionedUsers.add(username);
                matcher.appendReplacement(contentWithMarkup, "<span class=\"mention\">@" + username + "</span>");
            }
        }
        matcher.appendTail(contentWithMarkup);
        chatMessage.setContent(contentWithMarkup.toString());
        ChatMessage mentionNotification = new ChatMessage();
        mentionNotification.setType(ChatMessage.MessageType.MENTION);
        mentionNotification.setSender(chatMessage.getSender());
        mentionNotification.setContent("mentioned you in the chat!");
        mentionedUsers.forEach(mentionedUser ->
                messagingTemplate.convertAndSendToUser(mentionedUser, "/queue/private", mentionNotification)
        );
    }

    // --- MODIFIED: Ensure recipient is preserved for private GIFs ---
    private void handleGifMessage(ChatMessage chatMessage) {
        String query = chatMessage.getContent().substring(4).trim();
        String gifUrl = giphyService.getRandomGifUrl(query);

        if (gifUrl != null && !gifUrl.isEmpty()) {
            ChatMessage gifMessage = new ChatMessage();
            gifMessage.setType(ChatMessage.MessageType.GIF);
            gifMessage.setSender(chatMessage.getSender());
            gifMessage.setImageUrl(gifUrl);
            gifMessage.setContent(chatMessage.getContent());
            gifMessage.setRecipient(chatMessage.getRecipient()); // Preserve recipient
            if (chatMessage.getRepliedToMessageId() != null) {
                gifMessage.setRepliedToMessageId(chatMessage.getRepliedToMessageId());
            }
            saveAndBroadcast(gifMessage);
        }
    }

    private void saveAndBroadcast(ChatMessage chatMessage) {
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setSender(chatMessage.getSender());
        entity.setContent(chatMessage.getContent());
        entity.setImageUrl(chatMessage.getImageUrl());
        entity.setType(chatMessage.getType());
        entity.setTimestamp(LocalDateTime.now());
        entity.setEdited(chatMessage.isEdited());
        entity.setRecipient(chatMessage.getRecipient());

        if (chatMessage.getType() == ChatMessage.MessageType.FILE) {
            entity.setFileUrl(chatMessage.getFileUrl());
            entity.setFileSize(chatMessage.getFileSize());
        }

        // --- MODIFIED LINE ---
        // Allow link previews for both CHAT and PRIVATE message types.
        if (chatMessage.getType() == ChatMessage.MessageType.CHAT || chatMessage.getType() == ChatMessage.MessageType.PRIVATE) {
            String urlInMessage = linkPreviewService.findFirstUrl(chatMessage.getContent().replaceAll("<.*?>", ""));
            if (urlInMessage != null) {
                LinkPreview preview = linkPreviewService.generatePreview(urlInMessage);
                if (preview != null) {
                    entity.setLinkUrl(preview.getUrl());
                    entity.setLinkTitle(preview.getTitle());
                    entity.setLinkDescription(preview.getDescription());
                    entity.setLinkImageUrl(preview.getImageUrl());
                    entity.setLinkSiteName(preview.getSiteName());
                }
            }
        }

        if (chatMessage.getRepliedToMessageId() != null) {
            messageRepository.findById(chatMessage.getRepliedToMessageId()).ifPresent(originalMessage -> {
                entity.setRepliedToMessageId(originalMessage.getId());
                entity.setRepliedToMessageSender(originalMessage.getSender());
                String originalContent = originalMessage.getContent();
                entity.setRepliedToMessageContent(
                        originalContent.length() > 70 ? originalContent.substring(0, 70) + "..." : originalContent
                );
            });
        }

        ChatMessageEntity savedEntity = messageRepository.save(entity);
        ChatMessage broadcastMessage = convertEntityToDto(savedEntity);

        if (savedEntity.getRecipient() != null) {
            messagingTemplate.convertAndSendToUser(broadcastMessage.getRecipient(), "/queue/private", broadcastMessage);
            messagingTemplate.convertAndSendToUser(broadcastMessage.getSender(), "/queue/private", broadcastMessage);
        } else {
            messagingTemplate.convertAndSend("/topic/public", broadcastMessage);
        }
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor, Principal principal) {
        String username = principal.getName();
        headerAccessor.getSessionAttributes().put("username", username);
        chatRoom.addUser(username);
        chatMessage.setSender(username);
        chatMessage.setUsers(chatRoom.getUsers());
        return chatMessage;
    }


    private ChatMessage convertEntityToDto(ChatMessageEntity entity) {
        ChatMessage msg = new ChatMessage();
        msg.setId(entity.getId());
        msg.setType(entity.getType());
        msg.setContent(entity.getContent());
        msg.setSender(entity.getSender());
        userRepository.findByUsername(entity.getSender()).ifPresent(user -> {
            msg.setSenderAvatarUrl(user.getAvatarUrl());
        });
        msg.setImageUrl(entity.getImageUrl());
        msg.setEdited(entity.isEdited());
        msg.setRecipient(entity.getRecipient());

        if (entity.getTimestamp() != null) {
            msg.setTimestamp(entity.getTimestamp().format(TIME_FORMATTER));
        }
        if (entity.getRepliedToMessageId() != null) {
            msg.setRepliedToMessageId(entity.getRepliedToMessageId());
            msg.setRepliedToMessageSender(entity.getRepliedToMessageSender());
            msg.setRepliedToMessageContent(entity.getRepliedToMessageContent());
        }
        if (entity.getLinkUrl() != null) {
            msg.setLinkUrl(entity.getLinkUrl());
            msg.setLinkTitle(entity.getLinkTitle());
            msg.setLinkDescription(entity.getLinkDescription());
            msg.setLinkImageUrl(entity.getLinkImageUrl());
            msg.setLinkSiteName(entity.getLinkSiteName());
        }
        if (entity.getType() == ChatMessage.MessageType.FILE) {
            msg.setFileUrl(entity.getFileUrl());
            msg.setFileSize(entity.getFileSize());
        }
        if (entity.getReactions() != null && !entity.getReactions().isEmpty()) {
            Map<String, Set<String>> reactionsMap = entity.getReactions().stream()
                    .collect(Collectors.groupingBy(MessageReaction::getReaction, Collectors.mapping(MessageReaction::getUsername, Collectors.toSet())));
            msg.setReactions(reactionsMap);
        }
        return msg;
    }



    @MessageMapping("/chat.editMessage")
    public void editMessage(@Payload ChatMessage chatMessage, Principal principal) {
        ChatMessageEntity entity = messageRepository.findById(chatMessage.getId()).orElseThrow(() -> new RuntimeException("Message not found"));
        if (!entity.getSender().equals(principal.getName())) { return; }
        handleMentions(chatMessage);
        entity.setContent(chatMessage.getContent());
        entity.setEdited(true);
        messageRepository.save(entity);
        chatMessage.setType(ChatMessage.MessageType.EDIT);
        chatMessage.setSender(principal.getName());
        if (entity.getRecipient() != null) {
            messagingTemplate.convertAndSendToUser(entity.getRecipient(), "/queue/private", chatMessage);
            messagingTemplate.convertAndSendToUser(entity.getSender(), "/queue/private", chatMessage);
        } else {
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }

    @MessageMapping("/chat.deleteMessage")
    public void deleteMessage(@Payload ChatMessage chatMessage, Principal principal) {
        ChatMessageEntity entity = messageRepository.findById(chatMessage.getId()).orElseThrow(() -> new RuntimeException("Message not found"));
        if (!entity.getSender().equals(principal.getName())) { return; }
        messageRepository.delete(entity);
        ChatMessage deletedMessage = new ChatMessage();
        deletedMessage.setId(chatMessage.getId());
        deletedMessage.setType(ChatMessage.MessageType.DELETE);
        if (entity.getRecipient() != null) {
            deletedMessage.setRecipient(entity.getRecipient());
            messagingTemplate.convertAndSendToUser(entity.getRecipient(), "/queue/private", deletedMessage);
            messagingTemplate.convertAndSendToUser(entity.getSender(), "/queue/private", deletedMessage);
        } else {
            messagingTemplate.convertAndSend("/topic/public", deletedMessage);
        }
    }

    @Transactional
    @MessageMapping("/chat.toggleReaction")
    public void toggleReaction(@Payload ChatMessage chatMessage, Principal principal) {
        String username = principal.getName();
        Long messageId = chatMessage.getId();
        String reaction = chatMessage.getContent();
        ChatMessageEntity messageEntity = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));
        Optional<MessageReaction> existingReaction = reactionRepository.findByChatMessageIdAndUsernameAndReaction(messageId, username, reaction);
        ChatMessage reactionUpdateMessage = new ChatMessage();
        reactionUpdateMessage.setType(ChatMessage.MessageType.REACTION);
        reactionUpdateMessage.setId(messageId);
        reactionUpdateMessage.setSender(username);
        reactionUpdateMessage.setContent(reaction);
        if (existingReaction.isPresent()) {
            messageEntity.getReactions().remove(existingReaction.get());
            reactionRepository.delete(existingReaction.get());
            reactionUpdateMessage.setRecipient("REMOVE");
        } else {
            MessageReaction newReaction = new MessageReaction(reaction, username, messageEntity);
            messageEntity.getReactions().add(newReaction);
            reactionUpdateMessage.setRecipient("ADD");
        }
        messageRepository.save(messageEntity);
        if (messageEntity.getRecipient() != null) {
            reactionUpdateMessage.setRecipient(messageEntity.getRecipient());
            messagingTemplate.convertAndSendToUser(messageEntity.getRecipient(), "/queue/private", reactionUpdateMessage);
            messagingTemplate.convertAndSendToUser(messageEntity.getSender(), "/queue/private", reactionUpdateMessage);
        } else {
            messagingTemplate.convertAndSend("/topic/public", reactionUpdateMessage);
        }
    }

    @SendTo("/topic/public")
    @MessageMapping("/chat.typing")
    public ChatMessage sendTypingStatus(@Payload ChatMessage chatMessage, Principal principal) {
        chatMessage.setSender(principal.getName());
        return chatMessage;
    }

    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }
}