package com.ashwani.portfolio_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Service
public class GroqService implements AIProvider {

    private final RestClient restClient;
    private final String apiKey;

    public GroqService(
            @Value("${groq.api.key}") String apiKey
    ) {
        this.apiKey = apiKey;
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(20));

        this.restClient = RestClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public String getName() {
        return "Groq";
    }

    @Override
    public String generateAnswer(
            String question,
            String context,
            String conversationHistory
    ) {
        String prompt = """
                You are Ashwani's personal AI assistant on his portfolio website.

                Answer the visitor's question using the provided portfolio context.

                Speak naturally in first person as Ashwani.
                Do not invent information that is not supported by the context.
                If the information is not available, say:
                "I don't have that information right now."

                PORTFOLIO CONTEXT:
                %s

                CONVERSATION HISTORY:
                %s

                VISITOR QUESTION:
                %s
                """.formatted(
                context,
                conversationHistory,
                question
        );

        Map<String, Object> requestBody = Map.of(
                "model", "openai/gpt-oss-20b",
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "temperature", 0.3
        );

        Map<?, ?> response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("choices") == null) {
            throw new RuntimeException("Groq returned an empty response");
        }

        List<?> choices = (List<?>) response.get("choices");

        if (choices.isEmpty()) {
            throw new RuntimeException("Groq returned no choices");
        }

        Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
        Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");

        if (message == null || message.get("content") == null) {
            throw new RuntimeException("Groq returned no message content");
        }

        return message.get("content").toString();
    }
}