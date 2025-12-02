package com.hybrid.search.config;

import com.hybrid.search.model.Document;
import com.hybrid.search.service.lexical.SolrSearchService;
import com.hybrid.search.service.semantic.SemanticSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration for initializing sample data
 * This creates mock documents (job descriptions, product catalogs) for demonstration
 */
@Slf4j
@Configuration
public class DataInitializationConfig {

    @Bean
    public CommandLineRunner initializeData(SolrSearchService solrSearchService,
                                           SemanticSearchService semanticSearchService) {
        return args -> {
            log.info("Initializing sample data...");
            
            List<Document> sampleDocuments = createSampleDocuments();
            
            // Index documents in both Solr and Vector Store
            solrSearchService.indexDocuments(sampleDocuments);
            semanticSearchService.indexDocuments(sampleDocuments);
            
            log.info("Sample data initialization completed. Indexed {} documents.", sampleDocuments.size());
        };
    }

    private List<Document> createSampleDocuments() {
        return Arrays.asList(
            // Job Descriptions
            new Document("job1", "Senior Java Developer", 
                "We are looking for an experienced Java developer with Spring Boot expertise. " +
                "The ideal candidate should have 5+ years of experience in building microservices " +
                "and RESTful APIs. Knowledge of Solr, Elasticsearch, or similar search technologies is a plus.",
                "job_description", "Engineering"),
            
            new Document("job2", "Full Stack Developer", 
                "Join our team as a Full Stack Developer working with React, Node.js, and Java. " +
                "You'll be building modern web applications and working with cloud technologies like AWS.",
                "job_description", "Engineering"),
            
            new Document("job3", "Data Engineer", 
                "Seeking a Data Engineer to design and implement data pipelines. Experience with " +
                "Apache Spark, Kafka, and data warehousing solutions required.",
                "job_description", "Data"),
            
            // Product Catalogs
            new Document("product1", "Wireless Bluetooth Headphones", 
                "Premium wireless headphones with noise cancellation, 30-hour battery life, " +
                "and superior sound quality. Perfect for music lovers and professionals.",
                "product_catalog", "Electronics"),
            
            new Document("product2", "Smart Fitness Watch", 
                "Track your fitness goals with this advanced smartwatch featuring heart rate monitoring, " +
                "GPS tracking, and 50+ workout modes. Water-resistant design.",
                "product_catalog", "Electronics"),
            
            new Document("product3", "Ergonomic Office Chair", 
                "Comfortable ergonomic chair with lumbar support, adjustable height, and breathable mesh. " +
                "Ideal for long work hours and home office setups.",
                "product_catalog", "Furniture"),
            
            new Document("product4", "Mechanical Gaming Keyboard", 
                "RGB backlit mechanical keyboard with Cherry MX switches. Perfect for gaming and typing " +
                "with customizable key mapping and macro support.",
                "product_catalog", "Electronics"),
            
            new Document("job4", "DevOps Engineer", 
                "DevOps Engineer needed to manage CI/CD pipelines, container orchestration with Kubernetes, " +
                "and cloud infrastructure on AWS. Terraform and Ansible experience preferred.",
                "job_description", "Engineering"),
            
            new Document("product5", "4K Ultra HD Monitor", 
                "27-inch 4K monitor with HDR support, 144Hz refresh rate, and USB-C connectivity. " +
                "Perfect for gaming, design work, and professional use.",
                "product_catalog", "Electronics")
        );
    }
}

