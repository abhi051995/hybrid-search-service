# Hybrid Search Service

A Spring Boot microservice that combines traditional lexical search (Apache Solr) with semantic search (Spring AI Vector Store) to provide enhanced search capabilities. The service includes query rewriting using Large Language Models (LLM) to improve search accuracy.

## Features

### 1. Core Search (Lexical)
- **Technology**: Apache Solr, Java 17+, Spring Boot
- **Description**: Maintains an index of mock documents (job descriptions, product catalogs) in Solr and implements standard lexical/keyword-based search.

### 2. Semantic Layer
- **Technology**: Spring AI, Vector Store (in-memory SimpleVectorStore)
- **Description**: Uses Spring AI to generate embeddings for documents and stores them in a vector database for semantic similarity search.

### 3. Hybrid Logic
- **Technology**: Spring Boot Microservice
- **Description**: A service that accepts user queries, performs both traditional lexical (Solr) search and semantic (Spring AI/Vector) search, then interleaves and ranks the results using weighted scoring.

### 4. Query Rewriting
- **Technology**: Spring AI (Prompt Engineering/LLM)
- **Description**: Uses a Large Language Model (LLM) via Spring AI to rephrase or expand the user's initial query before it goes to Solr/Vector Search, enhancing search accuracy.

## Architecture

```
┌─────────────────┐
│   REST API      │
│  (Controllers)  │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Hybrid Search   │
│    Service      │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐ ┌──────────────┐
│  Solr  │ │ Query        │
│ Search │ │ Rewriting    │
└────────┘ └──────┬───────┘
                  │
                  ▼
         ┌────────────────┐
         │ Semantic Search │
         │  (Vector Store) │
         └─────────────────┘
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+ (with access to Spring Milestone repository for Spring AI dependencies)
- Apache Solr 9.x (running on localhost:8983)
- OpenAI API key (for embeddings and LLM-based query rewriting)

## Setup Instructions

### 1. Install and Start Apache Solr

```bash
# Download Solr (if not already installed)
# https://solr.apache.org/downloads.html

# Start Solr
solr start

# Create a collection
solr create -c hybrid_search

# Define schema (optional - Solr will auto-detect fields)
```

### 2. Configure Application

1. Clone or download this repository
2. Set your OpenAI API key as an environment variable:
   ```bash
   export OPENAI_API_KEY=your-api-key-here
   ```
   
   Or update `application.properties`:
   ```properties
   spring.ai.openai.api-key=your-api-key-here
   ```

### 3. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Search Endpoints

#### Hybrid Search (Recommended)
```bash
POST /api/search/hybrid
Content-Type: application/json

{
  "query": "software engineer java",
  "maxResults": 10,
  "lexicalWeight": 0.5,
  "semanticWeight": 0.5
}
```

#### Lexical Search Only
```bash
GET /api/search/lexical?query=java developer&maxResults=10
```

#### Semantic Search Only
```bash
GET /api/search/semantic?query=software engineering position&maxResults=10
```

### Document Indexing Endpoints

#### Index Single Document
```bash
POST /api/documents
Content-Type: application/json

{
  "id": "doc1",
  "title": "Senior Java Developer",
  "content": "Job description content...",
  "type": "job_description",
  "category": "Engineering"
}
```

#### Batch Index Documents
```bash
POST /api/documents/batch
Content-Type: application/json

[
  {
    "id": "doc1",
    "title": "Title 1",
    "content": "Content 1",
    "type": "job_description",
    "category": "Engineering"
  },
  {
    "id": "doc2",
    "title": "Title 2",
    "content": "Content 2",
    "type": "product_catalog",
    "category": "Electronics"
  }
]
```

## Example Usage

### Using cURL

```bash
# Hybrid search
curl -X POST http://localhost:8080/api/search/hybrid \
  -H "Content-Type: application/json" \
  -d '{
    "query": "software engineer position",
    "maxResults": 5,
    "lexicalWeight": 0.6,
    "semanticWeight": 0.4
  }'

# Lexical search
curl "http://localhost:8080/api/search/lexical?query=java&maxResults=5"

# Semantic search
curl "http://localhost:8080/api/search/semantic?query=programming job&maxResults=5"
```

### Response Format

```json
{
  "originalQuery": "software engineer",
  "rewrittenQuery": "software engineer position with programming skills",
  "results": [
    {
      "document": {
        "id": "job1",
        "title": "Senior Java Developer",
        "content": "...",
        "type": "job_description",
        "category": "Engineering"
      },
      "score": 0.85,
      "source": "hybrid"
    }
  ],
  "totalResults": 5,
  "lexicalResultsCount": 3,
  "semanticResultsCount": 4
}
```

## Configuration

Key configuration properties in `application.properties`:

```properties
# Solr Configuration
solr.url=http://localhost:8983/solr
solr.collection=hybrid_search

# Spring AI - OpenAI
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.options.model=gpt-4
spring.ai.openai.embedding.options.model=text-embedding-3-small
```

## How It Works

1. **Query Rewriting**: User query is sent to an LLM (via Spring AI) to generate an improved version with synonyms, expansions, and clarifications.

2. **Parallel Search**: The rewritten query is used to perform:
   - **Lexical Search**: Traditional keyword-based search in Solr
   - **Semantic Search**: Vector similarity search using embeddings

3. **Result Combination**: Results from both searches are combined using:
   - Weighted scoring (configurable lexical/semantic weights)
   - Interleaving of results
   - Deduplication (documents appearing in both results get combined scores)

4. **Ranking**: Final results are ranked by combined score in descending order.

## Sample Data

The application automatically initializes with sample documents including:
- Job descriptions (Engineering, Data roles)
- Product catalogs (Electronics, Furniture)

You can add more documents via the indexing endpoints.

## Technologies Used

- **Java 17+**: Programming language
- **Spring Boot 4.0**: Application framework
- **Apache Solr 9.6**: Lexical search engine
- **Spring AI 1.0.0-M4**: AI/ML integration framework
- **OpenAI API**: For embeddings and LLM-based query rewriting
- **Maven**: Build tool
- **Lombok**: Reducing boilerplate code

## Project Structure

```
src/main/java/com/hybrid/search/
├── HybridSearchServiceApplication.java
├── config/
│   └── DataInitializationConfig.java
├── controller/
│   ├── SearchController.java
│   └── DocumentController.java
├── dto/
│   ├── SearchRequest.java
│   ├── SearchResponse.java
│   └── SearchResult.java
├── model/
│   └── Document.java
└── service/
    ├── hybrid/
    │   └── HybridSearchService.java
    ├── lexical/
    │   └── SolrSearchService.java
    ├── query/
    │   └── QueryRewritingService.java
    └── semantic/
        └── SemanticSearchService.java
```

## Future Enhancements

- [ ] Support for other vector databases (Pinecone, Weaviate, etc.)
- [ ] Advanced ranking algorithms (BM25, RRF - Reciprocal Rank Fusion)
- [ ] Query caching for improved performance
- [ ] Metrics and monitoring integration
- [ ] Docker containerization
- [ ] Kubernetes deployment configurations

## License

This project is open source and available for use.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

