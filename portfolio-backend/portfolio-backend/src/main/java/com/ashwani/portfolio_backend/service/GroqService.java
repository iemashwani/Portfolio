package com.ashwani.portfolio_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

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

        /*
         * Call Groq API.
         *
         * IMPORTANT:
         * Any Groq API/network failure must throw an exception.
         * This allows AIProviderManager to try OpenRouter.
         */
        Map<?, ?> response;

        try {

            response = restClient.post()
                    .uri("/chat/completions")
                    .header(
                            "Authorization",
                            "Bearer " + apiKey
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {

            throw new RuntimeException(
                    "Groq API quota exceeded.",
                    e
            );

        } catch (org.springframework.web.client.RestClientException e) {

            throw new RuntimeException(
                    "Groq API request failed.",
                    e
            );
        }

        /*
         * Make sure Groq actually returned a response.
         */
        if (response == null) {
            throw new RuntimeException(
                    "Groq returned an empty response."
            );
        }

        /*
         * Read Groq response safely.
         */
        try {

            Object choicesObject = response.get("choices");

            if (!(choicesObject instanceof List<?> choices)) {
                throw new RuntimeException(
                        "Groq response did not contain choices."
                );
            }

            if (choices.isEmpty()) {
                throw new RuntimeException(
                        "Groq returned no choices."
                );
            }

            Object firstChoiceObject = choices.get(0);

            if (!(firstChoiceObject instanceof Map<?, ?> firstChoice)) {
                throw new RuntimeException(
                        "Groq returned an invalid choice."
                );
            }

            Object messageObject = firstChoice.get("message");

            if (!(messageObject instanceof Map<?, ?> message)) {
                throw new RuntimeException(
                        "Groq returned no message."
                );
            }

            Object contentObject = message.get("content");

            if (contentObject == null) {
                throw new RuntimeException(
                        "Groq returned no message content."
                );
            }

            String answer = contentObject.toString();

            if (answer.isBlank()) {
                throw new RuntimeException(
                        "Groq returned an empty answer."
                );
            }

            return answer;

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not parse Groq response.",
                    e
            );
        }
    }
}