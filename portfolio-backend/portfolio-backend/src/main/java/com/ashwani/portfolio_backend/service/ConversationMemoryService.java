package com.ashwani.portfolio_backend.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConversationMemoryService {

    private final Map<String, List<ChatMessage>> conversations =
            new HashMap<>();

    public void addMessage(
            String conversationId,
            String role,
            String message) {

        conversations
                .computeIfAbsent(
                        conversationId,
                        id -> new ArrayList<>()
                )
                .add(new ChatMessage(role, message));
    }

    public List<ChatMessage> getMessages(
            String conversationId) {

        return conversations.getOrDefault(
                conversationId,
                new ArrayList<>()
        );
    }

    public static class ChatMessage {

        private final String role;
        private final String message;

        public ChatMessage(
                String role,
                String message) {

            this.role = role;
            this.message = message;
        }

        public String getRole() {
            return role;
        }

        public String getMessage() {
            return message;
        }
    }
}