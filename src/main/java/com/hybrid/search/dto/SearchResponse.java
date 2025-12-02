package com.hybrid.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private String originalQuery;
    private String rewrittenQuery;
    private List<SearchResult> results;
    private Long totalResults;
    private Long lexicalResultsCount;
    private Long semanticResultsCount;
}

