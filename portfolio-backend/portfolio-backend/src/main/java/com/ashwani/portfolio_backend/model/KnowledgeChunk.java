package com.ashwani.portfolio_backend.model;

import java.util.List;

public class KnowledgeChunk {

    private String text;
    private List<Double> embedding;

    public KnowledgeChunk(String text, List<Double> embedding) {
        this.text = text;
        this.embedding = embedding;
    }

    public String getText() {
        return text;
    }

    public List<Double> getEmbedding() {
        return embedding;
    }
}