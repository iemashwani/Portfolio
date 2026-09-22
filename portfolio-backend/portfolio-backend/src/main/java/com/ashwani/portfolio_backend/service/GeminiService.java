package com.ashwani.portfolio_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService implements AIProvider {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.chat.enabled:true}")
    private boolean chatEnabled;

    private final RestClient restClient;
    private final KnowledgeService knowledgeService;

    public GeminiService(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;

        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .requestFactory(
                        new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                            setConnectTimeout(Duration.ofSeconds(5));
                            setReadTimeout(Duration.ofSeconds(20));
                        }}
                )
                .build();
    }

    public String askGemini(String question) {

        String knowledge = knowledgeService.getKnowledge();

        return generateAnswer(
                question,
                knowledge,
                ""
        );
    }

    @Override
    public String getName() {
        return "Gemini";
    }

    @Override
    public String generateAnswer(
            String question,
            String context,
            String conversationHistory) {

        if (!chatEnabled) {
            throw new RuntimeException(
                    "Gemini chat temporarily disabled for fallback test"
            );
        }

        String prompt = """
        You are AI Ashwani Singh, the personal AI assistant representing Ashwani Singh on his portfolio website.

                IDENTITY:
                - You are Ashwani's personal AI assistant on his portfolio website.
                - Your job is to respond as if you are Ashwani personally answering the visitor.
                - Always speak naturally in first person using "I", "me", "my", "I've", "I worked", "I use", etc.
                - Do NOT refer to Ashwani in the third person when answering questions about him.
                - Do NOT repeatedly say "Ashwani", "he", "his", or "him" unless the visitor specifically asks about Ashwani by name or third-person wording is necessary for clarity.
                - The conversation should feel like the visitor is talking directly with Ashwani.
                - Understand that "Ashwani", "he", "him", and "his" can refer to you.
                - Understand follow-up references such as "there", "that", "it", "this", and similar words using the conversation history.
                - Do not claim to be a human. You are an AI assistant speaking on Ashwani's behalf.

        KNOWLEDGE RULES:
        - Use ONLY the provided portfolio context and conversation history.
        - Do not invent information.
        - Do not make assumptions about Ashwani.
        - If the answer cannot be found in the available information, say:
          "I don't have that information about me in my knowledge base."
        - Do not use information outside the provided information.
        - Do not mention vector databases, embeddings, RAG, retrieval, or internal context.

        CONVERSATION MEMORY:
        - The conversation history contains previous visitor questions and AI responses.
        - Use it to understand follow-up questions and references.
        - If the visitor asks something that was already answered earlier, you may refer to the previous response.
        - Do not unnecessarily repeat a long previous answer.
        - When appropriate, say "As I mentioned earlier..." and provide the relevant information.
        - Maintain continuity throughout the conversation.

                RESPONSE STYLE:
                        - Be natural, conversational, friendly, and professional.
                        - Make the response feel like a real conversation with Ashwani.
                        - Keep most answers short and direct, usually 1–3 short paragraphs.
                        - Do not automatically create sections, headings, or long lists.
                        - Use normal sentences and paragraphs unless a list is genuinely useful.
                        - Avoid unnecessary explanations and repetition.
                        - Use first person naturally: "I", "my", "I've", "I worked", "I use", etc.
                        - Do not repeatedly mention the name "Ashwani".
                        - Use Markdown sparingly.
                        - Do not use bold formatting for every technology or important word.
                        - Only use bold when highlighting something genuinely important.
                        - Do not use Markdown bullet points unless the visitor asks for a list or a list makes the answer substantially clearer.
                        - Never output escaped Markdown such as "\\*" or "\\-".
                        - Do not begin responses with phrases like "Here's a breakdown", "Let's break it down", or "I work with a variety of technologies across different areas!" unless appropriate to the question.
                        - Answer naturally, as if you are personally having a conversation with the visitor.

        RETRIEVED PORTFOLIO CONTEXT:
        ----------------
        %s
        ----------------

        CONVERSATION HISTORY:
        ----------------
        %s
        ----------------

        CURRENT VISITOR QUESTION:
        %s
        """.formatted(
                context,
                conversationHistory,
                question
        );

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

        /*
         * Call Gemini API.
         *
         * IMPORTANT:
         * Any Gemini failure must throw an exception.
         * This allows AIProviderManager to try the next provider.
         */
        Map response;

        try {

            response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {

            throw new RuntimeException(
                    "Gemini chat API quota exceeded.",
                    e
            );

        } catch (org.springframework.web.client.RestClientException e) {

            throw new RuntimeException(
                    "Gemini chat API request failed.",
                    e
            );
        }

        /*
         * Make sure Gemini actually returned a response.
         */
        if (response == null) {
            throw new RuntimeException(
                    "Gemini returned an empty response."
            );
        }

        /*
         * Read Gemini response safely.
         */
        try {

            var candidates =
                    (List<?>) response.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException(
                        "Gemini returned no candidates."
                );
            }

            var candidate =
                    (Map<?, ?>) candidates.get(0);

            var content =
                    (Map<?, ?>) candidate.get("content");

            if (content == null) {
                throw new RuntimeException(
                        "Gemini response did not contain content."
                );
            }

            var parts =
                    (List<?>) content.get("parts");

            if (parts == null || parts.isEmpty()) {
                throw new RuntimeException(
                        "Gemini response did not contain text parts."
                );
            }

            var part =
                    (Map<?, ?>) parts.get(0);

            Object textObject = part.get("text");

            if (textObject == null) {
                throw new RuntimeException(
                        "Gemini response did not contain answer text."
                );
            }

            String answer = textObject.toString();

            if (answer.isBlank()) {
                throw new RuntimeException(
                        "Gemini returned an empty answer."
                );
            }

            return answer;

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Could not parse Gemini chat response.",
                    e
            );
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

        Map response;

        try {

            response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {

            throw new RuntimeException(
                    "Gemini embedding API quota exceeded. Please try again later.",
                    e
            );

        } catch (org.springframework.web.client.RestClientException e) {

            throw new RuntimeException(
                    "Gemini embedding API request failed.",
                    e
            );
        }

        try {

            Map<?, ?> embedding =
                    (Map<?, ?>) response.get("embedding");

            List<?> values =
                    (List<?>) embedding.get("values");

            List<Double> result =
                    new ArrayList<>();

            for (Object value : values) {

                result.add(
                        ((Number) value).doubleValue()
                );
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