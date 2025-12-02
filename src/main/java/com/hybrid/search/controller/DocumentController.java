package com.hybrid.search.controller;

import com.hybrid.search.model.Document;
import com.hybrid.search.service.lexical.SolrSearchService;
import com.hybrid.search.service.semantic.SemanticSearchService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for document indexing
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final SolrSearchService solrSearchService;
    private final SemanticSearchService semanticSearchService;

    public DocumentController(SolrSearchService solrSearchService,
                             SemanticSearchService semanticSearchService) {
        this.solrSearchService = solrSearchService;
        this.semanticSearchService = semanticSearchService;
    }

    /**
     * Indexes a single document in both Solr and Vector Store
     */
    @PostMapping
    public ResponseEntity<String> indexDocument(@Valid @RequestBody Document document) {
        log.info("Indexing document with id: {}", document.getId());
        solrSearchService.indexDocument(document);
        semanticSearchService.indexDocument(document);
        return ResponseEntity.ok("Document indexed successfully");
    }

    /**
     * Indexes multiple documents in both Solr and Vector Store
     */
    @PostMapping("/batch")
    public ResponseEntity<String> indexDocuments(@Valid @RequestBody List<Document> documents) {
        log.info("Indexing {} documents", documents.size());
        solrSearchService.indexDocuments(documents);
        semanticSearchService.indexDocuments(documents);
        return ResponseEntity.ok("Documents indexed successfully");
    }
}

