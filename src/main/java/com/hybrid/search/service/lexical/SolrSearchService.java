package com.hybrid.search.service.lexical;

import com.hybrid.search.model.Document;
import lombok.extern.slf4j.Slf4j;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.Http2SolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Core Search Service - Implements lexical search using Apache Solr
 */
@Slf4j
@Service
public class SolrSearchService {

    private final SolrClient solrClient;
    private final String collection;

    public SolrSearchService(@Value("${solr.url:http://localhost:8983/solr}") String solrUrl,
                            @Value("${solr.collection:hybrid_search}") String collection) {
        this.collection = collection;
        this.solrClient = new Http2SolrClient.Builder(solrUrl).build();
        log.info("Initialized Solr client for collection: {}", collection);
    }

    /**
     * Performs lexical search on Solr index
     */
    public List<Document> search(String query, int maxResults) {
        try {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(query);
            solrQuery.setRows(maxResults);
            solrQuery.setFields("id", "title", "content", "type", "category");

            QueryResponse response = solrClient.query(collection, solrQuery);
            SolrDocumentList documents = response.getResults();

            List<Document> results = new ArrayList<>();
            for (SolrDocument doc : documents) {
                Document document = new Document(
                    (String) doc.getFieldValue("id"),
                    (String) doc.getFieldValue("title"),
                    (String) doc.getFieldValue("content"),
                    (String) doc.getFieldValue("type"),
                    (String) doc.getFieldValue("category")
                );
                results.add(document);
            }

            log.info("Solr search returned {} results for query: {}", results.size(), query);
            return results;
        } catch (SolrServerException | IOException e) {
            log.error("Error performing Solr search", e);
            return new ArrayList<>();
        }
    }

    /**
     * Indexes a document in Solr
     */
    public void indexDocument(Document document) {
        try {
            org.apache.solr.common.SolrInputDocument solrDoc = new org.apache.solr.common.SolrInputDocument();
            solrDoc.addField("id", document.getId());
            solrDoc.addField("title", document.getTitle());
            solrDoc.addField("content", document.getContent());
            solrDoc.addField("type", document.getType());
            solrDoc.addField("category", document.getCategory());

            solrClient.add(collection, solrDoc);
            solrClient.commit(collection);
            log.info("Indexed document with id: {}", document.getId());
        } catch (SolrServerException | IOException e) {
            log.error("Error indexing document", e);
        }
    }

    /**
     * Indexes multiple documents in Solr
     */
    public void indexDocuments(List<Document> documents) {
        try {
            List<org.apache.solr.common.SolrInputDocument> solrDocs = new ArrayList<>();
            for (Document doc : documents) {
                org.apache.solr.common.SolrInputDocument solrDoc = new org.apache.solr.common.SolrInputDocument();
                solrDoc.addField("id", doc.getId());
                solrDoc.addField("title", doc.getTitle());
                solrDoc.addField("content", doc.getContent());
                solrDoc.addField("type", doc.getType());
                solrDoc.addField("category", doc.getCategory());
                solrDocs.add(solrDoc);
            }
            solrClient.add(collection, solrDocs);
            solrClient.commit(collection);
            log.info("Indexed {} documents", documents.size());
        } catch (SolrServerException | IOException e) {
            log.error("Error indexing documents", e);
        }
    }
}

