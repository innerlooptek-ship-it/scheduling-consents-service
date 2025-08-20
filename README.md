# Scheduling Consents Service

A Spring Boot microservice that provides consent management functionality for healthcare scheduling systems. This service was extracted from the IQE (Intelligent Questionnaire Engine) to provide focused consent processing capabilities.

## Overview

The Scheduling Consents Service handles:
- Patient consent retrieval and validation
- MCIT integration for consent data
- Group scheduling consent summarization
- Consent filtering based on business rules
- GraphQL API for consent operations

## Key Features

- **GraphQL API**: `getSchedulingConsents` endpoint for consent retrieval
- **MCIT Integration**: Retrieves patient consent data from MCIT services
- **Rule-based Filtering**: Uses MVEL expressions for consent filtering
- **Group Scheduling**: Supports consent summarization for group appointments
- **Reactive Programming**: Built with Spring WebFlux for non-blocking operations

## Technology Stack

- **Framework**: Spring Boot 3.4.7
- **Language**: Java 17
- **Database**: Apache Cassandra (Reactive)
- **API**: GraphQL
- **Build Tool**: Gradle with Kotlin DSL
- **Testing**: JUnit 5, TestContainers

## API Endpoints

### GraphQL Endpoint
- **URL**: `/graphql`
- **Query**: `getSchedulingConsents`
- **Parameters**:
  - `id`: Patient identifier
  - `idType`: Type of identifier
  - `consentInput`: Consent request details
  - `profile`: Enrollment profile information

## Configuration

Key configuration properties:
- `spring.cassandra.*`: Cassandra connection settings
- `feature.live.mcGroupScheduling`: Enable group scheduling features
- `api.configs.consents.*`: External service configurations
- `custom.scheduleEncryptDecryptKey`: Encryption key for patient data

## Building and Running

```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test
```

## GraphQL Playground

Access the GraphQL playground at: `/graphiql`

## Health Checks

Health endpoint available at: `/actuator/health`

## Extracted From

This service was extracted from the [IQE Rules Repository](https://github.com/innerlooptek-ship-it/rules) to provide focused consent management capabilities.

## Link to Devin Run

https://app.devin.ai/sessions/dd9395e0e57646d4bd9538ad07534a08

## Requested by

@innerlooptek-ship-it
