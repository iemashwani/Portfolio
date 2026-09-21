package com.ashwani.portfolio_backend.service;

import com.ashwani.portfolio_backend.model.KnowledgeChunk;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

        return chunks.stream()
                .map(chunk -> new ScoredChunk(
                        chunk,
                        cosineSimilarity(
                                questionEmbedding,
                                chunk.getEmbedding()
                        )
                ))
                .sorted(
                        Comparator.comparingDouble(
                                ScoredChunk::score
                        ).reversed()
                )
                .limit(topK)
                .toList();
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
                (Math.sqrt(magnitudeA) *
                        Math.sqrt(magnitudeB));
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