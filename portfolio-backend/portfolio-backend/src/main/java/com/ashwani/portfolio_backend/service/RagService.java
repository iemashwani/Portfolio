package com.ashwani.portfolio_backend.service;

import com.ashwani.portfolio_backend.model.KnowledgeChunk;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {

    private final VectorStoreService vectorStoreService;
    private final GeminiService geminiService;
    private final ConversationMemoryService conversationMemoryService;

    public RagService(
            VectorStoreService vectorStoreService,
            GeminiService geminiService,
            ConversationMemoryService conversationMemoryService) {

        this.vectorStoreService = vectorStoreService;
        this.geminiService = geminiService;
        this.conversationMemoryService = conversationMemoryService;
    }

    public String ask(
            String conversationId,
            String question) {

        // Get previous conversation before adding the new question
        List<ConversationMemoryService.ChatMessage> history =
                conversationMemoryService.getMessages(conversationId);

        // Save visitor's question
        conversationMemoryService.addMessage(
                conversationId,
                "user",
                question
        );

        // RAG search
        List<KnowledgeChunk> results =
                vectorStoreService.search(question, 3);

        if (results.isEmpty()) {

            String answer =
                    "I don't have that information right now.";

            conversationMemoryService.addMessage(
                    conversationId,
                    "assistant",
                    answer
            );

            return answer;
        }

        StringBuilder context = new StringBuilder();

        for (KnowledgeChunk chunk : results) {
            context.append(chunk.getText())
                    .append("\n\n");
        }

        StringBuilder conversationContext =
                new StringBuilder();

        for (ConversationMemoryService.ChatMessage message : history) {

            conversationContext
                    .append(message.getRole())
                    .append(": ")
                    .append(message.getMessage())
                    .append("\n");
        }

        String answer =
                geminiService.generateAnswer(
                        question,
                        context.toString(),
                        conversationContext.toString()
                );

        // Save AI response
        conversationMemoryService.addMessage(
                conversationId,
                "assistant",
                answer
        );

        return answer;
    }
}