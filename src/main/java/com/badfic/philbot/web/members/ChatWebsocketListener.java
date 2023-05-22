package com.badfic.philbot.web.members;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Controller
@Slf4j
public class ChatWebsocketListener {
    private static final Object2IntMap<String> ONLINE_MEMBERS = Object2IntMaps.synchronize(new Object2IntArrayMap<>());

    private final SimpMessageSendingOperations messagingTemplate;

    public ChatWebsocketListener(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String userAvatar = (String) headerAccessor.getSessionAttributes().get("userAvatar");
        if (username != null) {
            log.info("[User={}] Disconnected", username);
            ONLINE_MEMBERS.computeInt(username, (key, oldValue) -> {
                if (oldValue != null && oldValue > 1) {
                    return oldValue - 1;
                }

                ChatMessage chatMessage = new ChatMessage(ChatMessage.MessageType.LEAVE, null, username, userAvatar);

                messagingTemplate.convertAndSend("/topic/public", chatMessage);
                return null;
            });

            messagingTemplate.convertAndSend("/topic/online", ONLINE_MEMBERS.keySet());
        }
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatMessage.sender());
        headerAccessor.getSessionAttributes().put("userAvatar", chatMessage.avatar());
        ONLINE_MEMBERS.computeInt(chatMessage.sender(), (key, oldValue) -> {
            if (oldValue == null) {
                messagingTemplate.convertAndSend("/topic/public", chatMessage);
                return 1;
            }

            return oldValue + 1;
        });

        messagingTemplate.convertAndSend("/topic/online", ONLINE_MEMBERS.keySet());
    }

    public record ChatMessage(MessageType type, String content, String sender, String avatar) {
        public enum MessageType {
            CHAT, JOIN, LEAVE
        }
    }

}