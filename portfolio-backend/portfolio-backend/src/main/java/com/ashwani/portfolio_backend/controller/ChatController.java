package com.ashwani.portfolio_backend.controller;

import com.ashwani.portfolio_backend.service.GeminiService;
import com.ashwani.portfolio_backend.service.KnowledgeService;
import com.ashwani.portfolio_backend.service.KnowledgeChunkService;
import com.ashwani.portfolio_backend.service.VectorStoreService;
import com.ashwani.portfolio_backend.service.RagService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ashwani.portfolio_backend.model.RagResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
public class ChatController {

    @Value("${WHATSAPP_NUMBER}")
    private String whatsappNumber;

    private final GeminiService geminiService;
    private final KnowledgeService knowledgeService;
    private final KnowledgeChunkService knowledgeChunkService;
    private final VectorStoreService vectorStoreService;
    private final RagService ragService;

    public ChatController(
            GeminiService geminiService,
            KnowledgeService knowledgeService,
            KnowledgeChunkService knowledgeChunkService,
            VectorStoreService vectorStoreService,
            RagService ragService) {

        this.geminiService = geminiService;
        this.knowledgeService = knowledgeService;
        this.knowledgeChunkService = knowledgeChunkService;
        this.vectorStoreService = vectorStoreService;
        this.ragService = ragService;
    }

    @GetMapping("/api/chat/test")
    public String test() {
        return "AI Portfolio Assistant Backend is running!";
    }

    @GetMapping("/api/knowledge")
    public String knowledge() {
        return knowledgeService.getKnowledge();
    }

    @GetMapping("/api/gemini/test")
    public String geminiTest(
            @RequestParam(defaultValue = "Say hello to Ashwani") String question) {

        return geminiService.askGemini(question);
    }

    @GetMapping("/api/embedding/test")
    public String embeddingTest(
            @RequestParam(defaultValue = "Ashwani is a Full Stack Developer") String text) {

        List<Double> embedding = geminiService.createEmbedding(text);

        return "Embedding generated successfully. Dimensions: "
                + embedding.size();
    }

    @GetMapping("/api/chunks/test")
    public List<String> chunksTest() {
        return knowledgeChunkService.createChunks();
    }

    @GetMapping("/api/vector/test")
    public String vectorTest() {

        vectorStoreService.buildVectorStore();

        return "Vector store created with "
                + vectorStoreService.getChunks().size()
                + " chunks.";
    }

    @GetMapping("/api/search/test")
    public List<String> searchTest(
            @RequestParam String question) {

        List<VectorStoreService.ScoredChunk> results =
                vectorStoreService.search(question, 3);

        return results.stream()
                .map(result -> result.chunk().getText())
                .toList();
    }

    @GetMapping("/api/rag/test")
    public RagResponse ragTest(
            @RequestParam String conversationId,
            @RequestParam String question) {

        return ragService.ask(
                conversationId,
                question
        );
    }

    @GetMapping("/api/contact/whatsapp")
    public Map<String, String> getWhatsAppLink(
            @RequestParam String question) {

        String message =
                "Hi Ashwani, I was using your portfolio AI assistant " +
                        "and had this question:\n\n" +
                        "\"" + question + "\"\n\n" +
                        "The AI assistant couldn't answer it. " +
                        "I'd like to discuss it with you.";

        String encodedMessage =
                URLEncoder.encode(
                        message,
                        StandardCharsets.UTF_8
                );

        String whatsappUrl =
                "https://wa.me/" +
                        whatsappNumber +
                        "?text=" +
                        encodedMessage;

        return Map.of("url", whatsappUrl);
    }
}