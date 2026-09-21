package com.ashwani.portfolio_backend.service;

public interface AIProvider {

    String getName();

    String generateAnswer(
            String question,
            String context,
            String conversationHistory
    );
}