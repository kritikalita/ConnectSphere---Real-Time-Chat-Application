package com.chat.app.config;

import com.chat.app.model.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    @Autowired private SimpMessageSendingOperations messageTemplate;
    @Autowired private ChatRoom chatRoom; // Inject the ChatRoom

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        if (username != null) {
            // Remove user from the ChatRoom
            chatRoom.removeUser(username);

            // Create a LEAVE message and include the updated user list
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(ChatMessage.MessageType.LEAVE);
            chatMessage.setSender(username);
            chatMessage.setUsers(chatRoom.getUsers()); // Add the updated list

            messageTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}


