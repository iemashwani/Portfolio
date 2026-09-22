package com.ashwani.portfolio_backend.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class KnowledgeChunkService {

    private final KnowledgeService knowledgeService;

    /*
     * Matches standalone uppercase headings such as:
     *
     * PERSONAL PROFILE
     * PROFESSIONAL AUTHENTICATION EXPERIENCE
     * JAVA
     * SPRING BOOT
     * REACT
     * MACHINE LEARNING
     * EDUCATIONAL BACKGROUND
     * etc.
     *
     * It intentionally ignores normal sentences.
     */
    private static final Pattern HEADING_PATTERN =
            Pattern.compile(
                    "(?m)^\\s*[A-Z][A-Z0-9 /&'()\\-.,+#]{2,}\\s*$"
            );

    /*
     * Target maximum size of an embedding chunk.
     *
     * We don't want one huge chunk containing the entire
     * knowledge base, but we also don't want tiny chunks
     * such as only:
     *
     * NAME
     * Ashwani Singh
     */
    private static final int MAX_CHUNK_SIZE = 2200;

    public KnowledgeChunkService(
            KnowledgeService knowledgeService) {

        this.knowledgeService = knowledgeService;
    }

    public List<String> createChunks() {

        String knowledge =
                knowledgeService.getKnowledge();

        List<String> chunks = new ArrayList<>();

        if (knowledge == null || knowledge.isBlank()) {

            System.out.println(
                    "Knowledge file is empty or could not be loaded."
            );

            return chunks;
        }

        knowledge = knowledge
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .trim();

        /*
         * Find all uppercase heading positions.
         */
        var matcher =
                HEADING_PATTERN.matcher(knowledge);

        List<Integer> headingPositions =
                new ArrayList<>();

        while (matcher.find()) {
            headingPositions.add(matcher.start());
        }

        /*
         * If no headings are found, fall back to
         * paragraph-based chunking.
         */
        if (headingPositions.isEmpty()) {

            return createParagraphChunks(knowledge);
        }

        /*
         * Build logical sections.
         */
        List<String> sections = new ArrayList<>();

        for (int i = 0;
             i < headingPositions.size();
             i++) {

            int start =
                    headingPositions.get(i);

            int end =
                    (i + 1 < headingPositions.size())
                            ? headingPositions.get(i + 1)
                            : knowledge.length();

            String section =
                    knowledge
                            .substring(start, end)
                            .trim();

            if (!section.isBlank()) {
                sections.add(section);
            }
        }

        /*
         * Combine related small sections into
         * useful embedding-sized chunks.
         */
        StringBuilder currentChunk =
                new StringBuilder();

        for (String section : sections) {

            /*
             * If adding this section would make the
             * chunk too large, save the current chunk.
             */
            if (!currentChunk.isEmpty()
                    && currentChunk.length()
                    + section.length()
                    + 2
                    > MAX_CHUNK_SIZE) {

                chunks.add(
                        currentChunk
                                .toString()
                                .trim()
                );

                currentChunk.setLength(0);
            }

            if (!currentChunk.isEmpty()) {
                currentChunk.append("\n\n");
            }

            currentChunk.append(section);
        }

        /*
         * Add final chunk.
         */
        if (!currentChunk.isEmpty()) {

            chunks.add(
                    currentChunk
                            .toString()
                            .trim()
            );
        }

        System.out.println(
                "Knowledge sections detected: "
                        + sections.size()
        );

        System.out.println(
                "Knowledge chunks created: "
                        + chunks.size()
        );

        for (int i = 0;
             i < chunks.size();
             i++) {

            System.out.println(
                    "Chunk "
                            + (i + 1)
                            + " size: "
                            + chunks.get(i).length()
                            + " characters"
            );
        }

        return chunks;
    }

    private List<String> createParagraphChunks(
            String knowledge) {

        List<String> chunks =
                new ArrayList<>();

        String[] paragraphs =
                knowledge.split("\\n\\s*\\n");

        StringBuilder current =
                new StringBuilder();

        for (String paragraph : paragraphs) {

            String cleaned =
                    paragraph.trim();

            if (cleaned.isBlank()) {
                continue;
            }

            if (!current.isEmpty()
                    && current.length()
                    + cleaned.length()
                    + 2
                    > MAX_CHUNK_SIZE) {

                chunks.add(
                        current.toString().trim()
                );

                current.setLength(0);
            }

            if (!current.isEmpty()) {
                current.append("\n\n");
            }

            current.append(cleaned);
        }

        if (!current.isEmpty()) {

            chunks.add(
                    current.toString().trim()
            );
        }

        System.out.println(
                "Knowledge chunks created: "
                        + chunks.size()
        );

        return chunks;
    }
}