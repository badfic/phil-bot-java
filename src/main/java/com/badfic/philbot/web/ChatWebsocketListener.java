package com.badfic.philbot.web;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ChatWebsocketListener {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ConcurrentMap<String, Integer> ONLINE_MEMBERS = new ConcurrentHashMap<>();

    @Resource
    private SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String userAvatar = (String) headerAccessor.getSessionAttributes().get("userAvatar");
        if (username != null) {
            logger.info("User Disconnected : " + username);
            ONLINE_MEMBERS.compute(username, (key, oldValue) -> {
                if (oldValue != null && oldValue > 1) {
                    return oldValue - 1;
                }

                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setType(ChatMessage.MessageType.LEAVE);
                chatMessage.setSender(username);
                chatMessage.setAvatar(userAvatar);

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
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        headerAccessor.getSessionAttributes().put("userAvatar", chatMessage.getAvatar());
        ONLINE_MEMBERS.compute(chatMessage.getSender(), (key, oldValue) -> {
            if (oldValue == null) {
                messagingTemplate.convertAndSend("/topic/public", chatMessage);
                return 1;
            }

            return oldValue + 1;
        });

        messagingTemplate.convertAndSend("/topic/online", ONLINE_MEMBERS.keySet());
    }

}