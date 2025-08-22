# JIRA Story: Migrate JSON Configuration to Cassandra Database

## Story Title
**Migrate Consent Configuration from JSON Files to Cassandra Database Storage**

## Story Type
**Feature/Enhancement**

## Priority
**Medium**

## Labels
- `database-migration`
- `configuration-management`
- `cassandra`
- `performance-improvement`
- `scalability`

---

## Problem Statement

The Scheduling Consents Service currently uses static JSON file-based configuration for consent management, loading a 317-line complex nested configuration file (`consents_vaccine.json`) at application startup. This approach has several limitations:

1. **Static Configuration**: Changes require application restart and redeployment
2. **Scalability Issues**: Configuration is loaded into memory and cached, limiting dynamic updates
3. **Environment Management**: Different environments require separate deployments for configuration changes
4. **Operational Overhead**: Configuration updates require full application lifecycle management
5. **Limited Flexibility**: No runtime configuration management or A/B testing capabilities

## Current Architecture Analysis

### Current JSON Configuration Structure
The system currently loads a complex 317-line JSON configuration with the following hierarchy:

```json
[
  {
    "lob": "CLINIC",
    "modality": "BnMInPerson", 
    "brand": "MC",
    "consents": [
      {
        "consentContext": "Review|StateRegistry",
        "consent": {
          "consents": [
            // Nested consent definitions with conditional logic
          ]
        }
      }
    ]
  }
]
```

### Key Configuration Elements
- **LOB/Modality/Brand Hierarchy**: Top-level categorization for consent configurations
- **Consent Contexts**: "Review" and "StateRegistry" contexts with different consent flows
- **Conditional Logic**: MVEL expressions for dynamic consent display (`state == 'NY'`, `age < 18`, etc.)
- **Consent Types**: checkbox, radio, input, passive consent types
- **Nested Relationships**: Complex parent-child consent relationships with combined types
- **State-Specific Rules**: Location-based consent requirements (NY HIV, TX immunization, AZ bill of rights)
- **Age-Based Logic**: Minor vs adult consent handling
- **Group Scheduling**: Special consent handling for group appointments

### Current Loading Mechanism
- **GetConsentConfigLoader**: Loads JSON at startup using `@PostConstruct`
- **Memory Caching**: Configuration cached in `ConcurrentHashMap` structures
- **Static File Path**: Hardcoded path to `consents/consents_vaccine.json`
- **Configuration Properties**: Managed via `DHSSchedulingConfigs.consentsConfig`

## Proposed Solution

### New Cassandra-Based Architecture
Replace the static JSON file with dynamic Cassandra database storage that enables:
- Runtime configuration updates without application restarts
- Environment-specific configuration management
- Version control and audit trails for configuration changes
- A/B testing capabilities for consent flows
- Improved scalability and performance

### Cassandra Data Model Design

#### Primary Tables

**1. consent_configurations**
```cql
CREATE TABLE consent_configurations (
    id UUID PRIMARY KEY,
    lob TEXT,
    modality TEXT,
    brand TEXT,
    version INT,
    is_active BOOLEAN,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by TEXT,
    INDEX (lob, modality, brand)
);
```

**2. consent_contexts**
```cql
CREATE TABLE consent_contexts (
    id UUID PRIMARY KEY,
    config_id UUID,
    context_name TEXT,
    text TEXT,
    sub_text TEXT,
    sort_order INT,
    created_at TIMESTAMP,
    FOREIGN KEY (config_id) REFERENCES consent_configurations(id)
);
```

**3. consent_definitions**
```cql
CREATE TABLE consent_definitions (
    id UUID PRIMARY KEY,
    context_id UUID,
    parent_consent_id UUID,
    consent_name TEXT,
    consent_type TEXT,
    text TEXT,
    sub_text TEXT,
    value TEXT,
    value_type TEXT,
    required BOOLEAN,
    is_hidden BOOLEAN,
    is_conditional BOOLEAN,
    rule_expression TEXT,
    consent_link TEXT,
    consent_link_text TEXT,
    sort_order INT,
    created_at TIMESTAMP,
    FOREIGN KEY (context_id) REFERENCES consent_contexts(id),
    FOREIGN KEY (parent_consent_id) REFERENCES consent_definitions(id)
);
```

**4. consent_options**
```cql
CREATE TABLE consent_options (
    id UUID PRIMARY KEY,
    consent_id UUID,
    option_text TEXT,
    option_value TEXT,
    option_value_type TEXT,
    is_default BOOLEAN,
    sort_order INT,
    FOREIGN KEY (consent_id) REFERENCES consent_definitions(id)
);
```

**5. consent_relationships**
```cql
CREATE TABLE consent_relationships (
    id UUID PRIMARY KEY,
    parent_consent_id UUID,
    child_consent_id UUID,
    relationship_type TEXT,
    created_at TIMESTAMP,
    FOREIGN KEY (parent_consent_id) REFERENCES consent_definitions(id),
    FOREIGN KEY (child_consent_id) REFERENCES consent_definitions(id)
);
```

### Migration Strategy

#### Phase 1: Database Schema Setup
- Create Cassandra keyspace and tables
- Set up proper indexing for efficient queries
- Create data migration scripts from JSON to Cassandra

#### Phase 2: Entity and Repository Implementation
- Create Spring Data Cassandra entities
- Implement repository interfaces for CRUD operations
- Add caching layer for performance optimization

#### Phase 3: Configuration Loader Refactoring
- Refactor `GetConsentConfigLoader` to use Cassandra repositories
- Implement fallback mechanism during transition
- Add configuration validation and error handling

#### Phase 4: Data Migration and Validation
- Migrate existing JSON data to Cassandra tables
- Validate data integrity and consent logic preservation
- Performance testing and optimization

## Acceptance Criteria

### 1. Database Schema Implementation
- [ ] Create Cassandra keyspace for consent configurations
- [ ] Implement all required tables with proper relationships
- [ ] Set up appropriate indexes for query performance
- [ ] Create data migration scripts from JSON to Cassandra
- [ ] Validate referential integrity and constraints

### 2. Entity and Repository Layer
- [ ] Create Spring Data Cassandra entities for all tables
- [ ] Implement repository interfaces with custom query methods
- [ ] Add proper entity relationships and mappings
- [ ] Implement caching strategy for frequently accessed configurations
- [ ] Add connection pooling and performance optimization

### 3. Configuration Loading Refactoring
- [ ] Refactor `GetConsentConfigLoader.cacheConsentConfigData()` method
- [ ] Replace JSON file loading with Cassandra repository calls
- [ ] Maintain existing `consentDataMap` and `consentByContextMap` structure
- [ ] Implement configuration refresh mechanism without restart
- [ ] Add proper error handling and fallback strategies

### 4. Data Migration and Validation
- [ ] Create migration scripts to populate Cassandra from existing JSON
- [ ] Validate all 317 lines of JSON configuration are properly migrated
- [ ] Ensure all conditional logic (MVEL expressions) is preserved
- [ ] Verify nested consent relationships are maintained
- [ ] Test state-specific and age-based consent rules

### 5. Configuration Management Features
- [ ] Implement version control for configuration changes
- [ ] Add audit trail for configuration updates
- [ ] Create admin interface for configuration management
- [ ] Implement environment-specific configuration support
- [ ] Add configuration validation and testing capabilities

### 6. Performance and Scalability
- [ ] Ensure query performance meets or exceeds current file-based loading
- [ ] Implement proper caching strategy with cache invalidation
- [ ] Add monitoring and metrics for configuration loading
- [ ] Test with multiple concurrent configuration requests
- [ ] Validate memory usage optimization

### 7. Testing Strategy
- [ ] Unit tests for all new repository and entity classes
- [ ] Integration tests for Cassandra connectivity and queries
- [ ] Migration testing with full JSON to Cassandra conversion
- [ ] Performance testing comparing file vs database loading
- [ ] End-to-end testing of consent flows with database configuration

### 8. Deployment and Operations
- [ ] Update application.yaml with Cassandra connection configuration
- [ ] Create deployment scripts for database schema setup
- [ ] Implement configuration backup and restore procedures
- [ ] Add monitoring and alerting for configuration loading
- [ ] Document operational procedures for configuration management

## Technical Implementation Details

### Spring Data Cassandra Configuration
```yaml
spring:
  cassandra:
    keyspace-name: scheduling_consents
    contact-points: localhost:9042
    local-datacenter: datacenter1
    schema-action: create-if-not-exists
    connection:
      pool:
        max-requests-per-connection: 1024
        max-queue-size: 256
```

### Entity Example
```java
@Table("consent_configurations")
public class ConsentConfiguration {
    @PrimaryKey
    private UUID id;
    
    @Column("lob")
    private String lob;
    
    @Column("modality") 
    private String modality;
    
    @Column("brand")
    private String brand;
    
    @Column("version")
    private Integer version;
    
    @Column("is_active")
    private Boolean isActive;
    
    // Additional fields and relationships
}
```

### Repository Interface
```java
@Repository
public interface ConsentConfigurationRepository extends CassandraRepository<ConsentConfiguration, UUID> {
    
    @Query("SELECT * FROM consent_configurations WHERE lob = ?0 AND modality = ?1 AND brand = ?2 AND is_active = true")
    Optional<ConsentConfiguration> findActiveByLobModalityBrand(String lob, String modality, String brand);
    
    @Query("SELECT * FROM consent_configurations WHERE is_active = true")
    List<ConsentConfiguration> findAllActive();
}
```

### Updated Configuration Loader
```java
@Component
public class GetConsentConfigLoader {
    
    @Autowired
    private ConsentConfigurationRepository configRepository;
    
    @Autowired
    private ConsentContextRepository contextRepository;
    
    @PostConstruct
    private void cacheConsentConfigData() {
        // Load from Cassandra instead of JSON file
        List<ConsentConfiguration> configs = configRepository.findAllActive();
        // Transform to existing data structures for backward compatibility
        // Implement caching and refresh mechanisms
    }
    
    @Scheduled(fixedRate = 300000) // Refresh every 5 minutes
    public void refreshConfiguration() {
        // Reload configuration from database
    }
}
```

## Benefits

1. **Dynamic Configuration Management**: Update consents without application restarts
2. **Improved Scalability**: Database-driven configuration scales better than file-based
3. **Environment Flexibility**: Different configurations per environment without code changes
4. **Audit and Version Control**: Track configuration changes and maintain history
5. **A/B Testing Capability**: Enable different consent flows for testing
6. **Operational Efficiency**: Reduce deployment overhead for configuration changes
7. **Better Performance**: Optimized database queries vs file parsing
8. **Configuration Validation**: Database constraints ensure data integrity

## Risks and Mitigation

### Risks
- **Database Dependency**: Application becomes dependent on Cassandra availability
- **Migration Complexity**: Complex nested JSON structure migration
- **Performance Impact**: Potential latency from database calls vs in-memory cache
- **Data Consistency**: Ensuring referential integrity across related tables

### Mitigation
- Implement robust caching strategy with fallback mechanisms
- Create comprehensive migration testing and validation
- Add database connection pooling and query optimization
- Implement proper error handling and circuit breaker patterns
- Maintain backward compatibility during transition period

## Dependencies

- Cassandra cluster setup and configuration
- Spring Data Cassandra dependency updates
- Database migration tooling and scripts
- DevOps team for deployment pipeline updates
- QA team for comprehensive migration testing

## Story Points Estimation

**Story Points: 13**

### Breakdown:
- **Database Schema Design and Setup**: 3 points
  - Design normalized schema for complex nested structure
  - Create tables, indexes, and relationships
  - Set up Cassandra keyspace and configuration

- **Entity and Repository Implementation**: 3 points
  - Create Spring Data Cassandra entities
  - Implement repository interfaces with custom queries
  - Add proper relationships and mappings

- **Data Migration Scripts**: 2 points
  - Parse existing 317-line JSON structure
  - Transform and load data into Cassandra tables
  - Validate data integrity and completeness

- **Configuration Loader Refactoring**: 3 points
  - Refactor GetConsentConfigLoader to use database
  - Maintain backward compatibility with existing data structures
  - Implement caching and refresh mechanisms

- **Testing and Validation**: 2 points
  - Unit and integration testing
  - Migration testing and validation
  - Performance testing and optimization

### Justification:
This is a medium-large story involving:
- Complex data model design for nested JSON structure (317 lines)
- Database schema creation and migration scripting
- Significant refactoring of configuration loading mechanism
- Need to preserve all existing conditional logic and relationships
- Comprehensive testing across multiple layers
- Performance optimization and caching implementation

The 13-point estimation reflects the technical complexity of migrating a complex nested configuration structure while maintaining full backward compatibility and adding new dynamic capabilities.

## Definition of Done

- [ ] Cassandra schema created and deployed
- [ ] All JSON configuration data successfully migrated to database
- [ ] Configuration loading refactored to use Cassandra repositories
- [ ] All existing consent logic and conditional rules preserved
- [ ] Performance benchmarks meet or exceed current file-based loading
- [ ] Comprehensive test coverage (unit, integration, migration)
- [ ] Documentation updated (schema, migration procedures, operations)
- [ ] Code review completed and approved
- [ ] Successful deployment with configuration refresh capabilities
- [ ] Monitoring and alerting configured for database configuration loading

---

**Created by**: @innerlooptek-ship-it  
**Date**: August 22, 2025  
**Epic**: Configuration Management Improvement  
**Sprint**: TBD  
**Related Stories**: Consent API Extraction (prerequisite)
