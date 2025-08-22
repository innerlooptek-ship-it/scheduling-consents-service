# JIRA Story: Extract Consent Processing Logic to New External API

## Story Title
**Extract Heavy Business Logic from ConsentsRestService to New External Consent Processing API**

## Story Type
**Epic/Feature**

## Priority
**Medium**

## Labels
- `architecture-improvement`
- `microservices`
- `refactoring`
- `api-extraction`
- `performance`

---

## Problem Statement

While the current Scheduling Consents Service has lightweight controllers (`ConsentsController` for GraphQL and `ConsentsRestController` for REST), the underlying business logic in `ConsentsRestService` contains substantial complexity that should be extracted into a dedicated external API. This will further improve separation of concerns and make the current service truly lightweight by focusing only on protocol adaptation (GraphQL/REST) while delegating core consent processing to a specialized service.

## Current Architecture Analysis

### Current Flow
1. **GraphQL Controller** (`ConsentsController`) → **GraphQL Service Wrapper** (`ConsentsService`) → **REST Service** (`ConsentsRestService`)
2. **REST Controller** (`ConsentsRestController`) → **REST Service** (`ConsentsRestService`)

### Current Responsibilities in ConsentsRestService (320+ lines)
- **MCIT Integration**: Patient consent retrieval from external MCIT services
- **Complex Validation**: Multi-context validation logic (VM, vaccine scheduling)
- **Rule-Based Filtering**: MVEL expression evaluation for consent filtering
- **Group Scheduling Logic**: Complex consent summarization for group appointments
- **Patient Data Encryption/Decryption**: Secure handling of patient identifiers
- **Configuration-Driven Processing**: Dynamic consent loading based on LOB/modality/brand
- **Multi-layered Data Transformation**: Complex object mapping between internal and external models

### Key Dependencies to Extract
- `ConsentsMcitService` - MCIT patient consent integration
- `ConsentServiceHelper` - Rule-based filtering and group scheduling logic
- `ValidatorConsentsService` - Complex validation rules
- `GetConsentConfigLoader` - Configuration management
- External integrations and encryption utilities

## Proposed Solution

### New Architecture
1. **GraphQL Controller** → **GraphQL Adapter Service** → **New Consent Processing API**
2. **REST Controller** → **REST Adapter Service** → **New Consent Processing API**

### New Consent Processing API Responsibilities
- Core consent business logic processing
- MCIT integration and external service calls
- Rule-based consent filtering using MVEL
- Group scheduling consent summarization
- Patient data encryption/decryption
- Configuration-driven consent processing
- Complex validation orchestration

### Lightweight Adapter Services (Remaining in Current Service)
- **ConsentsService**: GraphQL format conversion and basic orchestration
- **ConsentsRestService**: REST format conversion and basic orchestration
- Protocol-specific error handling and logging
- Request/response transformation between GraphQL/REST and internal API

## Acceptance Criteria

### 1. API Design
- [ ] Design RESTful API endpoints for the new Consent Processing Service
- [ ] Define clear request/response contracts with proper versioning
- [ ] Implement proper error handling and status codes
- [ ] Include comprehensive API documentation (OpenAPI/Swagger)

### 2. Service Extraction
- [ ] Extract core business logic from `ConsentsRestService.getConsents()` method
- [ ] Move MCIT integration logic to new service
- [ ] Transfer consent filtering and group scheduling logic
- [ ] Migrate validation orchestration to new service
- [ ] Preserve all existing functionality and business rules

### 3. Adapter Layer Implementation
- [ ] Refactor `ConsentsService` to call new external API
- [ ] Refactor `ConsentsRestService` to become lightweight adapter
- [ ] Maintain existing GraphQL and REST endpoint contracts
- [ ] Ensure backward compatibility for all existing clients

### 4. Configuration and Dependencies
- [ ] Move consent configuration loading to new service
- [ ] Transfer MCIT service dependencies
- [ ] Migrate encryption/decryption utilities
- [ ] Update dependency injection and configuration management

### 5. Testing Strategy
- [ ] Comprehensive unit tests for new Consent Processing API
- [ ] Integration tests between adapter services and new API
- [ ] End-to-end testing for GraphQL and REST endpoints
- [ ] Performance testing to ensure no regression
- [ ] Contract testing between services

### 6. Deployment and Migration
- [ ] Create deployment pipeline for new Consent Processing Service
- [ ] Implement feature flags for gradual rollout
- [ ] Create rollback strategy in case of issues
- [ ] Update monitoring and alerting for new service
- [ ] Document operational procedures

### 7. Performance and Scalability
- [ ] Ensure response times remain within acceptable limits
- [ ] Implement proper caching strategies in new service
- [ ] Design for horizontal scaling of consent processing
- [ ] Monitor and optimize inter-service communication

## Technical Implementation Details

### New Service Endpoints
```
POST /api/v1/consent-processing/consents
- Input: ConsentProcessingRequest (normalized format)
- Output: ConsentProcessingResponse (normalized format)

GET /api/v1/consent-processing/health
- Health check endpoint

GET /api/v1/consent-processing/config/{flow}/{lob}/{modality}/{brand}
- Configuration retrieval endpoint
```

### Data Models
- Create normalized internal data models for consent processing
- Design clear contracts between adapter and processing services
- Maintain backward compatibility with existing GraphQL/REST models

### Error Handling
- Implement circuit breaker pattern for external service calls
- Design proper error propagation between services
- Maintain existing error response formats for clients

## Benefits

1. **True Lightweight Controllers**: Controllers become pure protocol adapters
2. **Improved Separation of Concerns**: Business logic isolated in dedicated service
3. **Better Scalability**: Consent processing can scale independently
4. **Enhanced Maintainability**: Clearer boundaries between presentation and business logic
5. **Reusability**: New API can be consumed by other services
6. **Testing Isolation**: Business logic can be tested independently

## Risks and Mitigation

### Risks
- **Increased Latency**: Additional network hop between services
- **Complexity**: More moving parts in the system
- **Deployment Coordination**: Need to coordinate deployments between services

### Mitigation
- Implement proper caching strategies
- Use async communication where appropriate
- Implement comprehensive monitoring and alerting
- Create detailed deployment and rollback procedures

## Dependencies

- Infrastructure team for new service deployment
- DevOps team for CI/CD pipeline setup
- QA team for comprehensive testing strategy
- Architecture review board approval

## Story Points Estimation

**Story Points: 21**

### Breakdown:
- **API Design and Contracts**: 3 points
- **Core Business Logic Extraction**: 8 points (most complex part)
- **Adapter Layer Refactoring**: 5 points
- **Testing Strategy Implementation**: 3 points
- **Deployment and Configuration**: 2 points

### Justification:
This is a large, complex refactoring involving:
- 320+ lines of complex business logic to extract
- Multiple external service integrations (MCIT)
- Complex rule-based processing with MVEL
- Group scheduling logic with intricate patient reference tracking
- Multi-layered validation systems
- Need to create entirely new microservice
- Comprehensive testing across multiple service boundaries
- Deployment coordination and migration strategy

The high point value reflects the architectural significance and the need to maintain 100% backward compatibility while introducing a new service boundary.

## Definition of Done

- [ ] New Consent Processing API is deployed and operational
- [ ] All existing functionality preserved with no breaking changes
- [ ] Performance benchmarks meet or exceed current metrics
- [ ] Comprehensive test coverage (unit, integration, e2e)
- [ ] Documentation updated (API docs, architecture diagrams, runbooks)
- [ ] Monitoring and alerting configured for new service
- [ ] Code review completed and approved
- [ ] Architecture review board approval obtained
- [ ] Successful production deployment with feature flag rollout

---

**Created by**: @innerlooptek-ship-it  
**Date**: August 22, 2025  
**Epic**: Microservices Architecture Improvement  
**Sprint**: TBD
