package com.hybrid.search.service.hybrid;

import com.hybrid.search.dto.SearchRequest;
import com.hybrid.search.dto.SearchResult;
import com.hybrid.search.model.Document;
import com.hybrid.search.service.lexical.SolrSearchService;
import com.hybrid.search.service.query.QueryRewritingService;
import com.hybrid.search.service.semantic.SemanticSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hybrid Logic Service - Combines lexical (Solr) and semantic (Vector) search results
 * Implements interleaving and ranking similar to CareerBuilder's approach
 */
@Slf4j
@Service
public class HybridSearchService {

    private final SolrSearchService solrSearchService;
    private final SemanticSearchService semanticSearchService;
    private final QueryRewritingService queryRewritingService;

    public HybridSearchService(SolrSearchService solrSearchService,
                              SemanticSearchService semanticSearchService,
                              QueryRewritingService queryRewritingService) {
        this.solrSearchService = solrSearchService;
        this.semanticSearchService = semanticSearchService;
        this.queryRewritingService = queryRewritingService;
    }

    /**
     * Performs hybrid search combining lexical and semantic results
     */
    public HybridSearchResult performHybridSearch(SearchRequest request) {
        // Step 1: Rewrite query using LLM
        String rewrittenQuery = queryRewritingService.rewriteQuery(request.getQuery());

        // Step 2: Perform parallel searches
        List<Document> lexicalResults = solrSearchService.search(rewrittenQuery, request.getMaxResults());
        List<Document> semanticResults = semanticSearchService.search(rewrittenQuery, request.getMaxResults());

        // Step 3: Combine and rank results
        List<SearchResult> combinedResults = combineAndRankResults(
                lexicalResults,
                semanticResults,
                request.getLexicalWeight(),
                request.getSemanticWeight()
        );

        return new HybridSearchResult(
                request.getQuery(),
                rewrittenQuery,
                combinedResults,
                (long) lexicalResults.size(),
                (long) semanticResults.size()
        );
    }

    /**
     * Combines lexical and semantic results with weighted scoring and interleaving
     */
    private List<SearchResult> combineAndRankResults(
            List<Document> lexicalResults,
            List<Document> semanticResults,
            Double lexicalWeight,
            Double semanticWeight) {

        Map<String, SearchResult> resultMap = new HashMap<>();

        // Add lexical results with weighted scores
        for (int i = 0; i < lexicalResults.size(); i++) {
            Document doc = lexicalResults.get(i);
            double normalizedScore = 1.0 - (i * 0.1); // Normalize based on position
            double weightedScore = normalizedScore * lexicalWeight;

            resultMap.compute(doc.getId(), (id, existing) -> {
                if (existing == null) {
                    return new SearchResult(doc, weightedScore, "lexical");
                } else {
                    // Combine scores if document appears in both results
                    existing.setScore(existing.getScore() + weightedScore);
                    existing.setSource("hybrid");
                    return existing;
                }
            });
        }

        // Add semantic results with weighted scores
        for (int i = 0; i < semanticResults.size(); i++) {
            Document doc = semanticResults.get(i);
            double normalizedScore = 1.0 - (i * 0.1); // Normalize based on position
            double weightedScore = normalizedScore * semanticWeight;

            resultMap.compute(doc.getId(), (id, existing) -> {
                if (existing == null) {
                    return new SearchResult(doc, weightedScore, "semantic");
                } else {
                    // Combine scores if document appears in both results
                    existing.setScore(existing.getScore() + weightedScore);
                    existing.setSource("hybrid");
                    return existing;
                }
            });
        }

        // Sort by combined score (descending) and return
        return resultMap.values().stream()
                .sorted(Comparator.comparing(SearchResult::getScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Inner class to hold hybrid search results
     */
    public static class HybridSearchResult {
        private final String originalQuery;
        private final String rewrittenQuery;
        private final List<SearchResult> results;
        private final Long lexicalResultsCount;
        private final Long semanticResultsCount;

        public HybridSearchResult(String originalQuery, String rewrittenQuery,
                                 List<SearchResult> results,
                                 Long lexicalResultsCount, Long semanticResultsCount) {
            this.originalQuery = originalQuery;
            this.rewrittenQuery = rewrittenQuery;
            this.results = results;
            this.lexicalResultsCount = lexicalResultsCount;
            this.semanticResultsCount = semanticResultsCount;
        }

        public String getOriginalQuery() { return originalQuery; }
        public String getRewrittenQuery() { return rewrittenQuery; }
        public List<SearchResult> getResults() { return results; }
        public Long getLexicalResultsCount() { return lexicalResultsCount; }
        public Long getSemanticResultsCount() { return semanticResultsCount; }
        public Long getTotalResults() { return (long) results.size(); }
    }
}

