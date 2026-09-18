package com.ashwani.portfolio_backend.service;

import com.ashwani.portfolio_backend.model.KnowledgeChunk;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {

    private final VectorStoreService vectorStoreService;
    private final GeminiService geminiService;

    public RagService(
            VectorStoreService vectorStoreService,
            GeminiService geminiService) {

        this.vectorStoreService = vectorStoreService;
        this.geminiService = geminiService;
    }

    public String ask(String question) {

        List<KnowledgeChunk> results =
                vectorStoreService.search(question, 3);

        if (results.isEmpty()) {
            return "I don't have that information about Ashwani in my knowledge base.";
        }

        StringBuilder context = new StringBuilder();

        for (KnowledgeChunk chunk : results) {
            context.append(chunk.getText())
                    .append("\n\n");
        }

        return geminiService.generateAnswer(
                question,
                context.toString()
        );
    }
}