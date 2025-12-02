package com.hybrid.search.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a document that can be indexed in both Solr (lexical) and Vector Store (semantic)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    private String id;
    private String title;
    private String content;
    private String type; // e.g., "job_description", "product_catalog"
    private String category;
}

