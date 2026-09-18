package com.ashwani.portfolio_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestClient restClient;
    private final KnowledgeService knowledgeService;

    public GeminiService(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;

        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public String askGemini(String question) {

        String knowledge = knowledgeService.getKnowledge();

        return generateAnswer(question, knowledge);
    }

    public String generateAnswer(
            String question,
            String context) {

        String prompt = """
        You are Ashwani Singh's AI Portfolio Assistant.

        Answer the visitor's question using ONLY the context
        provided below.

        IMPORTANT RULES:
        - Do not invent information.
        - Do not make assumptions about Ashwani.
        - Do not use information outside the provided context.
        - If the answer cannot be found in the context, say:
          "I don't have that information about Ashwani in my knowledge base."
        - Keep the answer clear, natural, and concise.
        - When relevant, mention specific technologies,
          projects, achievements, or experience.
        - Do not mention that you are using a vector database,
          embeddings, RAG, or internal context.

        RESPONSE FORMATTING:
        - Use Markdown bold formatting to highlight important information.
        - Bold technology names when relevant.
        - Bold project names when relevant.
        - Bold company names when relevant.
        - Bold important numbers, metrics, and achievements.
        - Bold important skills and key points when appropriate.
        - Do not bold the entire response.
        - Do not overuse bold formatting.
        - Use bullet points when helpful.

        RETRIEVED CONTEXT:
        ----------------
        %s
        ----------------

        VISITOR QUESTION:
        %s
        """.formatted(context, question);

        String url =
                "/v1beta/models/gemini-2.5-flash:generateContent?key="
                        + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of(
                                "parts", new Object[]{
                                        Map.of("text", prompt)
                                }
                        )
                }
        );

        Map response = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        try {
            var candidates = (List<?>) response.get("candidates");
            var candidate = (Map<?, ?>) candidates.get(0);
            var content = (Map<?, ?>) candidate.get("content");
            var parts = (List<?>) content.get("parts");
            var part = (Map<?, ?>) parts.get(0);

            return part.get("text").toString();

        } catch (Exception e) {
            return "Could not read Gemini response.";
        }
    }

    public List<Double> createEmbedding(String text) {

        String url =
                "/v1beta/models/gemini-embedding-001:embedContent?key="
                        + apiKey;

        Map<String, Object> requestBody = Map.of(
                "model", "models/gemini-embedding-001",
                "content", Map.of(
                        "parts", new Object[]{
                                Map.of("text", text)
                        }
                )
        );

        Map response = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        try {
            Map<?, ?> embedding =
                    (Map<?, ?>) response.get("embedding");

            List<?> values =
                    (List<?>) embedding.get("values");

            List<Double> result = new ArrayList<>();

            for (Object value : values) {
                result.add(((Number) value).doubleValue());
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not parse Gemini embedding response",
                    e
            );
        }
    }
}