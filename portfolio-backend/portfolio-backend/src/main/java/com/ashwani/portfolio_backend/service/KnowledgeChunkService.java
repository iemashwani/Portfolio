package com.ashwani.portfolio_backend.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KnowledgeChunkService {

    private final KnowledgeService knowledgeService;

    public KnowledgeChunkService(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    public List<String> createChunks() {

        String knowledge = knowledgeService.getKnowledge();

        String[] sections = knowledge.split(
                "(?m)(?=^[A-Z][A-Z0-9 &—–-]+\\r?\\n-+\\r?\\n)"
        );

        List<String> chunks = new ArrayList<>();

        for (String section : sections) {

            String cleaned = section.trim();

            if (!cleaned.isEmpty()
                    && !cleaned.matches("=+\\s*ASHWANI SINGH.*")
                    && !cleaned.matches("=+\\s*END OF KNOWLEDGE BASE.*")) {

                chunks.add(cleaned);
            }
        }

        return chunks;
    }
}