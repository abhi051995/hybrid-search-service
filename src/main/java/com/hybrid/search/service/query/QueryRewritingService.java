package com.hybrid.search.service.query;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Query Rewriting Service - Uses LLM via Spring AI to rephrase/expand user queries
 */
@Slf4j
@Service
public class QueryRewritingService {

    private final ChatClient chatClient;
    private static final String QUERY_REWRITE_PROMPT = """
            You are a search query optimization assistant. Your task is to improve search queries 
            for better retrieval results in both lexical (keyword-based) and semantic (meaning-based) search systems.
            
            Original query: {query}
            
            Please provide an improved version of this query that:
            1. Preserves the original intent
            2. Expands with relevant synonyms and related terms for better lexical matching
            3. Clarifies ambiguous terms for better semantic understanding
            4. Maintains natural language flow
            
            Return only the improved query, without any additional explanation or formatting.
            """;

    public QueryRewritingService(ChatClient chatClient) {
        this.chatClient = chatClient;
        log.info("Initialized Query Rewriting Service");
    }

    /**
     * Rewrites/expands the user query using LLM to enhance search accuracy
     */
    public String rewriteQuery(String originalQuery) {
        try {
            PromptTemplate promptTemplate = new PromptTemplate(QUERY_REWRITE_PROMPT);
            Prompt prompt = promptTemplate.create(Map.of("query", originalQuery));
            
            String rewrittenQuery = chatClient.call(prompt).getResult().getOutput().getContent();
            log.info("Rewritten query: '{}' -> '{}'", originalQuery, rewrittenQuery);
            
            return rewrittenQuery.trim();
        } catch (Exception e) {
            log.error("Error rewriting query, returning original", e);
            return originalQuery;
        }
    }
}

