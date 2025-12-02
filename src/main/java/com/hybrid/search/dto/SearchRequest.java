package com.hybrid.search.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    @NotBlank(message = "Query cannot be blank")
    private String query;
    
    private Integer maxResults = 10;
    private Double lexicalWeight = 0.5; // Weight for lexical search results
    private Double semanticWeight = 0.5; // Weight for semantic search results
}

