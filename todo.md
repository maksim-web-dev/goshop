# Task: Update GoShop Microservices Technical Documentation

Analyze the actual source code of the GoShop multi-module Gradle project and update the existing `README.md` file inside each implemented microservice.

**Goal:** Create accurate technical documentation that can be used by another software architect to review the microservice architecture, analyze inter-service communication, identify missing functionality, and design a new `api-service` responsible for API aggregation.

## General Requirements

1. Analyze the actual implementation, including Controllers, DTOs, Services, Repositories, Entities, Flyway migrations, configuration files, and build dependencies.
2. Document only functionality that is actually implemented.
3. Clearly mark missing functionality, incomplete implementations, and TODOs.
4. Do not invent endpoints, fields, integrations, or business logic.
5. Preserve existing useful README information.
6. Update documentation only. Do not modify application source code.
7. Write the documentation in English.
8. Use Markdown tables where appropriate.

## Required README Sections

### 1. Service Overview

- Service purpose and responsibilities.
- Implemented functionality.
- Main business capabilities.
- Service boundaries and data ownership.

### 2. Technology Stack

- Kotlin and Java versions.
- Spring Boot version.
- Database technologies.
- Libraries and frameworks.
- Messaging, caching, and external integrations.

### 3. Database Model

Document all JPA entities:

- Entity name.
- Fields and their Kotlin/Java types.
- Database column types where relevant.
- Primary keys and generation strategy.
- Foreign keys and relationships.
- Unique constraints and indexes.
- Enum types and possible values.

Include the relationships between entities.

### 4. REST API Endpoints

For every implemented endpoint document:

- HTTP method and complete path.
- Controller and handler method.
- Purpose.
- Path variables.
- Query parameters.
- Request body DTO with all fields, types, required/optional status, and validation constraints.
- Response DTO with all fields and types.
- HTTP response status codes.
- Example request JSON.
- Example response JSON.
- Possible errors.

For paginated endpoints, document pagination parameters, sorting, filtering, and response structure.

### 5. Business Logic

Describe the main service-layer operations:

- Method names and responsibilities.
- Validation rules.
- Business constraints.
- Main processing algorithms.
- Transaction boundaries where relevant.
- Special cases and edge cases.

Focus on functionality important for integrating this service with other microservices.

### 6. Inter-Service Communication

Document all implemented communication with other services:

- Target service.
- HTTP endpoints called.
- Request and response DTOs.
- Communication technology.
- Synchronous or asynchronous behavior.
- Timeout, retry, and fallback configuration.
- Error handling.

If no communication is implemented, explicitly state this.

### 7. Kafka and Asynchronous Events

If messaging is implemented, document:

- Topic names.
- Producers and consumers.
- Event names.
- Event payload fields and types.
- Partition keys.
- Consumer groups.
- Retry and error handling.
- Idempotency mechanisms.

If messaging is not implemented, state that explicitly.

### 8. Error Handling

Document:

- Global exception handlers.
- Error response JSON format.
- HTTP status mapping.
- Validation errors.
- Business exceptions.
- Handling of unavailable dependencies.

### 9. Configuration and Deployment

Document:

- Application port and context path.
- Spring profiles.
- Environment variables.
- Database configuration.
- Dockerfile and Docker Compose integration.
- Actuator health endpoints.
- Kubernetes readiness, liveness, and graceful shutdown configuration, if implemented.

Do not include actual secrets or credentials.

### 10. Testing

Document:

- Existing unit tests.
- Integration tests.
- Testcontainers usage.
- API/controller tests.
- What functionality is currently covered.
- Known test gaps.

### 11. Known Limitations and Missing Functionality

Document:

- Unimplemented features.
- Incomplete business logic.
- Missing inter-service integrations.
- Existing TODOs.
- Architectural limitations visible in the source code.

Do not implement missing features.

## Additional Architecture Analysis

Update a separate file:

`docs/architecture/goshop-documentation/architecture-review.md`

Include:

1. A table of all GoShop microservices and their responsibilities.
2. A matrix of existing inter-service dependencies.
3. An inventory of entity identifier types across services, including cross-service references.
4. Identified inconsistencies in API contracts and entity identifiers.
5. Existing Kafka events and their consumers.
6. Capabilities required for a future API aggregation service.
7. Missing endpoints or batch operations that may be needed by `api-service`.
8. Potential synchronous communication bottlenecks.
9. Current readiness for Docker and Kubernetes deployment.
10. Recommendations for future improvements, clearly separated from the actual implementation.

## Important Constraints

- Documentation must reflect the actual source code, not the originally planned architecture.
- Clearly distinguish IMPLEMENTED, PARTIALLY IMPLEMENTED, and NOT IMPLEMENTED functionality.
- When information cannot be determined from source code, mark it as UNKNOWN.
- Do not assume that an entity field is necessarily part of the API DTO.
- Do not mark a feature as working solely because a controller or service class exists.
- Do not add speculative architecture to the service READMEs.
- Do not modify business logic, database schemas, or API contracts.

The final result should allow another developer or AI assistant to understand and integrate the existing microservices without having to read the entire codebase.