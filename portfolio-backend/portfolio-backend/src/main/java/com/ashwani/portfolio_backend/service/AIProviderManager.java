package com.ashwani.portfolio_backend.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIProviderManager {

    private final List<AIProvider> providers;

    public AIProviderManager(List<AIProvider> providers) {
        this.providers = providers;
    }

    public String generateAnswer(
            String question,
            String context,
            String conversationHistory
    ) {

        for (AIProvider provider : providers) {

            try {

                System.out.println(
                        "Trying AI provider: " +
                                provider.getName()
                );

                String answer = provider.generateAnswer(
                        question,
                        context,
                        conversationHistory
                );

                if (answer != null && !answer.isBlank()) {

                    System.out.println(
                            "AI provider succeeded: " +
                                    provider.getName()
                    );

                    return answer;
                }

                System.out.println(
                        "AI provider returned an empty response: " +
                                provider.getName()
                );

            } catch (Exception e) {

                System.out.println(
                        "AI provider failed: " +
                                provider.getName() +
                                " - " +
                                e.getMessage()
                );

            }
        }

        System.out.println(
                "All AI providers failed."
        );

        return "I'm temporarily unable to answer right now. Please try again later.";
    }
}