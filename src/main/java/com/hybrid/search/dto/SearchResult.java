package com.hybrid.search.dto;

import com.hybrid.search.model.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private Document document;
    private Double score;
    private String source; // "lexical", "semantic", or "hybrid"
}

