package com.ashwani.portfolio_backend.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class KnowledgeService {

    public String getKnowledge() {
        try {
            ClassPathResource resource =
                    new ClassPathResource("portfolio-knowledge.txt");

            return new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            throw new RuntimeException("Could not load portfolio knowledge", e);
        }
    }
}