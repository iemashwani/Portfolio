package com.ashwani.portfolio_backend.service;

import com.ashwani.portfolio_backend.model.KnowledgeChunk;
import com.ashwani.portfolio_backend.model.RagResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {

    private final VectorStoreService vectorStoreService;
    private final AIProviderManager aiProviderManager;
    private final ConversationMemoryService conversationMemoryService;

    private static final double SIMILARITY_THRESHOLD = 0.60;

    public RagService(
            VectorStoreService vectorStoreService,
            AIProviderManager aiProviderManager,
            ConversationMemoryService conversationMemoryService) {

        this.vectorStoreService = vectorStoreService;
        this.aiProviderManager = aiProviderManager;
        this.conversationMemoryService = conversationMemoryService;
    }

    public RagResponse ask(
            String conversationId,
            String question) {

        List<ConversationMemoryService.ChatMessage> history =
                conversationMemoryService.getMessages(conversationId);

        conversationMemoryService.addMessage(
                conversationId,
                "user",
                question
        );

        List<VectorStoreService.ScoredChunk> results =
                vectorStoreService.search(question, 3);

        if (results.isEmpty()) {

            String answer =
                    "I don't have that information right now.";

            conversationMemoryService.addMessage(
                    conversationId,
                    "assistant",
                    answer
            );

            return new RagResponse(answer, false);
        }

        double bestScore = results.get(0).score();

        System.out.println(
                "Best RAG similarity score: " + bestScore
        );

        if (bestScore < SIMILARITY_THRESHOLD) {

            String answer =
                    "I don't have that information right now.";

            conversationMemoryService.addMessage(
                    conversationId,
                    "assistant",
                    answer
            );

            return new RagResponse(answer, false);
        }

        StringBuilder context = new StringBuilder();

        for (VectorStoreService.ScoredChunk result : results) {

            context.append(result.chunk().getText())
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

        String answer = aiProviderManager.generateAnswer(
                question,
                context.toString(),
                conversationContext.toString()
        );

        conversationMemoryService.addMessage(
                conversationId,
                "assistant",
                answer
        );

        return new RagResponse(answer, true);
    }
}