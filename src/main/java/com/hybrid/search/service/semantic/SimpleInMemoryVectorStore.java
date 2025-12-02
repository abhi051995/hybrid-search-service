package com.hybrid.search.service.semantic;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Simple in-memory vector store implementation
 * Stores documents with their embeddings for similarity search
 */
public class SimpleInMemoryVectorStore implements VectorStore {

    private final EmbeddingClient embeddingClient;
    private final Map<String, StoredDocument> documents = new ConcurrentHashMap<>();

    public SimpleInMemoryVectorStore(EmbeddingClient embeddingClient) {
        this.embeddingClient = embeddingClient;
    }

    @Override
    public void add(List<Document> documents) {
        for (Document doc : documents) {
            // Generate embedding for the document content
            List<Double> embedding = embeddingClient.embed(doc.getContent());
            this.documents.put(doc.getId(), new StoredDocument(doc, embedding));
        }
    }

    @Override
    public Optional<Boolean> delete(List<String> idList) {
        idList.forEach(documents::remove);
        return Optional.of(true);
    }

    @Override
    public List<Document> similaritySearch(SearchRequest request) {
        if (documents.isEmpty()) {
            return new ArrayList<>();
        }

        // Generate embedding for the query string
        List<Double> queryEmbedding = embeddingClient.embed(request.getQuery());

        // Calculate cosine similarity for all documents
        List<ScoredDocument> scoredDocs = documents.values().stream()
                .map(storedDoc -> {
                    double similarity = cosineSimilarity(queryEmbedding, storedDoc.embedding);
                    return new ScoredDocument(storedDoc.document, similarity);
                })
                .sorted((a, b) -> Double.compare(b.score, a.score)) // Sort descending by score
                .limit(request.getTopK())
                .collect(Collectors.toList());

        return scoredDocs.stream()
                .map(sd -> sd.document)
                .collect(Collectors.toList());
    }

    /**
     * Calculate cosine similarity between two vectors
     */
    private double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1.size() != vec2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private static class StoredDocument {
        final Document document;
        final List<Double> embedding;

        StoredDocument(Document document, List<Double> embedding) {
            this.document = document;
            this.embedding = embedding;
        }
    }

    private static class ScoredDocument {
        final Document document;
        final double score;

        ScoredDocument(Document document, double score) {
            this.document = document;
            this.score = score;
        }
    }
}

