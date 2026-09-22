package com.ashwani.portfolio_backend.service;

import com.ashwani.portfolio_backend.model.KnowledgeChunk;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VectorStoreService implements CommandLineRunner {

    private final KnowledgeChunkService knowledgeChunkService;
    private final GeminiService geminiService;

    private final List<KnowledgeChunk> chunks = new ArrayList<>();

    public VectorStoreService(
            KnowledgeChunkService knowledgeChunkService,
            GeminiService geminiService) {

        this.knowledgeChunkService = knowledgeChunkService;
        this.geminiService = geminiService;
    }

    public void buildVectorStore() {

        chunks.clear();

        List<String> knowledgeChunks =
                knowledgeChunkService.createChunks();

        for (String text : knowledgeChunks) {

            List<Double> embedding =
                    geminiService.createEmbedding(text);

            chunks.add(
                    new KnowledgeChunk(text, embedding)
            );
        }

        System.out.println(
                "Vector store created with "
                        + chunks.size()
                        + " chunks."
        );

        if (!chunks.isEmpty()) {

            System.out.println(
                    "Embedding dimensions: "
                            + chunks.get(0)
                            .getEmbedding()
                            .size()
            );
        }
    }

    public List<KnowledgeChunk> getChunks() {
        return chunks;
    }

    public List<ScoredChunk> search(
            String question,
            int topK) {

        List<Double> questionEmbedding =
                geminiService.createEmbedding(question);

        Set<String> questionKeywords =
                extractKeywords(question);

        System.out.println("\n===== SEARCH DEBUG =====");
        System.out.println("Question: " + question);
        System.out.println("Keywords: " + questionKeywords);

        List<ScoredChunk> results = chunks.stream()
                .map(chunk -> {

                    double semanticScore =
                            cosineSimilarity(
                                    questionEmbedding,
                                    chunk.getEmbedding()
                            );

                    double keywordScore =
                            calculateKeywordScore(
                                    questionKeywords,
                                    chunk.getText()
                            );

                    /*
                     * Semantic similarity remains the primary signal.
                     *
                     * 75% semantic
                     * 25% keyword
                     */
                    double finalScore;

                    if (questionKeywords.isEmpty()) {

                        finalScore = semanticScore;

                    } else {

                        finalScore =
                                (semanticScore * 0.75)
                                        + (keywordScore * 0.25);
                    }

                    System.out.println(
                            "semantic=" + semanticScore
                                    + " | keyword=" + keywordScore
                                    + " | final=" + finalScore
                                    + " | "
                                    + getPreview(chunk.getText())
                    );

                    return new ScoredChunk(
                            chunk,
                            finalScore
                    );
                })
                .sorted(
                        Comparator.comparingDouble(
                                ScoredChunk::score
                        ).reversed()
                )
                .limit(topK)
                .toList();

        System.out.println("===== TOP RESULTS =====");

        for (ScoredChunk result : results) {

            System.out.println(
                    "FINAL SCORE: " + result.score()
                            + " | "
                            + getPreview(
                            result.chunk().getText()
                    )
            );
        }

        System.out.println("=======================\n");

        return results;
    }

    private String getPreview(String text) {

        String cleanedText =
                text
                        .replace("\n", " ")
                        .replace("\r", " ")
                        .trim();

        return cleanedText.substring(
                0,
                Math.min(
                        100,
                        cleanedText.length()
                )
        );
    }

    /**
     * Extract meaningful keywords from the question/chunk.
     *
     * Generic conversational words are ignored because
     * they occur in many unrelated chunks and can distort
     * retrieval.
     */
    private Set<String> extractKeywords(String text) {

        Set<String> stopWords = Set.of(

                // Question words
                "what",
                "which",
                "where",
                "when",
                "who",
                "why",
                "how",

                // Verb forms
                "is",
                "are",
                "was",
                "were",
                "be",
                "been",
                "being",

                "did",
                "do",
                "does",
                "done",

                "has",
                "have",
                "had",

                "can",
                "could",
                "would",
                "should",

                // Articles / connectors
                "the",
                "a",
                "an",
                "and",
                "or",
                "of",
                "to",
                "in",
                "on",
                "for",
                "with",
                "from",
                "by",
                "about",
                "as",

                // Pronouns
                "your",
                "you",
                "my",
                "me",
                "i",
                "he",
                "his",
                "she",
                "her",
                "they",
                "them",

                // Conversational words
                "tell",
                "please",
                "complete",
                "currently",
                "right",
                "now",

                // Generic action words
                "use",
                "used",
                "using",
                "work",
                "worked",
                "working",

                // Generic professional words
                "professional",
                "experience",
                "role",

                // Other generic words
                "type",
                "know"
        );

        return Arrays.stream(
                        text.toLowerCase()
                                .replaceAll(
                                        "[^a-z0-9+#. ]",
                                        " "
                                )
                                .split("\\s+")
                )
                .map(String::trim)
                .filter(word ->
                        !word.isBlank()
                                && word.length() > 2
                                && !stopWords.contains(word)
                )
                .collect(Collectors.toSet());
    }

    /**
     * Calculates a weighted keyword score.
     *
     * Technical/domain-specific terms receive more weight
     * than generic words.
     */
    private double calculateKeywordScore(
            Set<String> questionKeywords,
            String chunkText) {

        if (questionKeywords.isEmpty()) {
            return 0.0;
        }

        Set<String> chunkKeywords =
                extractKeywords(chunkText);

        double score = 0.0;
        double totalWeight = 0.0;

        for (String keyword : questionKeywords) {

            double weight =
                    getKeywordWeight(keyword);

            totalWeight += weight;

            if (chunkKeywords.contains(keyword)) {
                score += weight;
            }
        }

        if (totalWeight == 0.0) {
            return 0.0;
        }

        return score / totalWeight;
    }

    /**
     * Gives domain-specific keywords more importance.
     */
    private double getKeywordWeight(String keyword) {

        return switch (keyword) {

            // Highly specific technical/domain terms
            case "java",
                    "spring",
                    "springboot",
                    "springsecurity",
                    "react",
                    "javascript",
                    "sql",
                    "mysql",
                    "postgresql",
                    "aws",
                    "xray",
                    "microservices",
                    "oauth",
                    "jwt",
                    "wcag",
                    "aria",
                    "ets",
                    "toeic",
                    "heartility",
                    "python",
                    "scikit-learn",
                    "machine",
                    "learning",
                    "c++",
                    "cpp" -> 2.0;

            // Moderately specific terms
            case "technology",
                    "technologies",
                    "backend",
                    "frontend",
                    "authentication",
                    "accessibility",
                    "api",
                    "apis",
                    "database",
                    "databases",
                    "cloud",
                    "rest",
                    "testing",
                    "security",
                    "microservice" -> 1.5;

            // Everything else
            default -> 1.0;
        };
    }

    private double cosineSimilarity(
            List<Double> vectorA,
            List<Double> vectorB) {

        if (vectorA.size() != vectorB.size()) {

            throw new IllegalArgumentException(
                    "Embedding dimensions do not match."
            );
        }

        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {

            double a = vectorA.get(i);
            double b = vectorB.get(i);

            dotProduct += a * b;
            magnitudeA += a * a;
            magnitudeB += b * b;
        }

        if (magnitudeA == 0 || magnitudeB == 0) {
            return 0.0;
        }

        return dotProduct /
                (
                        Math.sqrt(magnitudeA)
                                * Math.sqrt(magnitudeB)
                );
    }

    public record ScoredChunk(
            KnowledgeChunk chunk,
            double score
    ) {
    }

    @Override
    public void run(String... args) {

        System.out.println(
                "Building portfolio vector store..."
        );

        buildVectorStore();

        System.out.println(
                "Portfolio vector store ready."
        );
    }
}