package com.hybrid.search.controller;

import com.hybrid.search.dto.SearchRequest;
import com.hybrid.search.dto.SearchResponse;
import com.hybrid.search.dto.SearchResult;
import com.hybrid.search.model.Document;
import com.hybrid.search.service.hybrid.HybridSearchService;
import com.hybrid.search.service.lexical.SolrSearchService;
import com.hybrid.search.service.semantic.SemanticSearchService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for Hybrid Search Service
 */
@Slf4j
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final HybridSearchService hybridSearchService;
    private final SolrSearchService solrSearchService;
    private final SemanticSearchService semanticSearchService;

    public SearchController(HybridSearchService hybridSearchService,
                           SolrSearchService solrSearchService,
                           SemanticSearchService semanticSearchService) {
        this.hybridSearchService = hybridSearchService;
        this.solrSearchService = solrSearchService;
        this.semanticSearchService = semanticSearchService;
    }

    /**
     * Performs hybrid search combining lexical and semantic results
     */
    @PostMapping("/hybrid")
    public ResponseEntity<SearchResponse> hybridSearch(@Valid @RequestBody SearchRequest request) {
        log.info("Received hybrid search request: {}", request.getQuery());
        
        HybridSearchService.HybridSearchResult result = hybridSearchService.performHybridSearch(request);
        
        SearchResponse response = new SearchResponse(
                result.getOriginalQuery(),
                result.getRewrittenQuery(),
                result.getResults(),
                result.getTotalResults(),
                result.getLexicalResultsCount(),
                result.getSemanticResultsCount()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Performs lexical-only search using Solr
     */
    @GetMapping("/lexical")
    public ResponseEntity<List<Document>> lexicalSearch(@RequestParam String query,
                                                       @RequestParam(defaultValue = "10") int maxResults) {
        log.info("Received lexical search request: {}", query);
        List<Document> results = solrSearchService.search(query, maxResults);
        return ResponseEntity.ok(results);
    }

    /**
     * Performs semantic-only search using vector embeddings
     */
    @GetMapping("/semantic")
    public ResponseEntity<List<Document>> semanticSearch(@RequestParam String query,
                                                        @RequestParam(defaultValue = "10") int maxResults) {
        log.info("Received semantic search request: {}", query);
        List<Document> results = semanticSearchService.search(query, maxResults);
        return ResponseEntity.ok(results);
    }
}

