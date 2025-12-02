package com.hybrid.search.service.semantic;

import com.hybrid.search.model.Document;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Semantic Layer - Uses Spring AI to generate embeddings and perform vector search
 */
@Slf4j
@Service
public class SemanticSearchService {

    private final VectorStore vectorStore;
    private final EmbeddingClient embeddingClient;

    public SemanticSearchService(EmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
        this.vectorStore = new SimpleInMemoryVectorStore(embeddingClient);
        log.info("Initialized Semantic Search Service with in-memory vector store");
    }

    /**
     * Performs semantic search using vector embeddings
     */
    public List<com.hybrid.search.model.Document> search(String query, int maxResults) {
        try {
            SearchRequest searchRequest = SearchRequest.query(query)
                    .withTopK(maxResults);

            List<org.springframework.ai.document.Document> results = vectorStore.similaritySearch(searchRequest);

            List<com.hybrid.search.model.Document> documents = results.stream()
                    .map(this::convertToDocument)
                    .collect(Collectors.toList());

            log.info("Semantic search returned {} results for query: {}", documents.size(), query);
            return documents;
        } catch (Exception e) {
            log.error("Error performing semantic search", e);
            return new ArrayList<>();
        }
    }

    /**
     * Indexes a document by generating embeddings and storing in vector store
     */
    public void indexDocument(com.hybrid.search.model.Document document) {
        try {
            String content = String.format("Title: %s\nContent: %s\nType: %s\nCategory: %s",
                    document.getTitle(), document.getContent(), document.getType(), document.getCategory());

            org.springframework.ai.document.Document aiDocument = new org.springframework.ai.document.Document(document.getId(), content, Map.of(
                    "id", document.getId(),
                    "title", document.getTitle(),
                    "type", document.getType(),
                    "category", document.getCategory()
            ));

            vectorStore.add(List.of(aiDocument));
            log.info("Indexed document in vector store with id: {}", document.getId());
        } catch (Exception e) {
            log.error("Error indexing document in vector store", e);
        }
    }

    /**
     * Indexes multiple documents in vector store
     */
    public void indexDocuments(List<com.hybrid.search.model.Document> documents) {
        try {
            List<org.springframework.ai.document.Document> aiDocuments = documents.stream()
                    .map(doc -> {
                        String content = String.format("Title: %s\nContent: %s\nType: %s\nCategory: %s",
                                doc.getTitle(), doc.getContent(), doc.getType(), doc.getCategory());
                        return new org.springframework.ai.document.Document(doc.getId(), content, Map.of(
                                "id", doc.getId(),
                                "title", doc.getTitle(),
                                "type", doc.getType(),
                                "category", doc.getCategory()
                        ));
                    })
                    .collect(Collectors.toList());

            vectorStore.add(aiDocuments);
            log.info("Indexed {} documents in vector store", documents.size());
        } catch (Exception e) {
            log.error("Error indexing documents in vector store", e);
        }
    }

    /**
     * Converts Spring AI Document to our Document model
     */
    private com.hybrid.search.model.Document convertToDocument(org.springframework.ai.document.Document aiDoc) {
        Map<String, Object> metadata = aiDoc.getMetadata();
        return new com.hybrid.search.model.Document(
                (String) metadata.get("id"),
                (String) metadata.get("title"),
                aiDoc.getContent(),
                (String) metadata.get("type"),
                (String) metadata.get("category")
        );
    }
}

