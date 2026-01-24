# Football Java

Java football club ranking system and UEFA draw simulator built with Spring Boot.

## Quick Start

### 1. Configure API Key

Copy the environment example and add your API key:

```bash
cp .env.example .env
```

Edit `.env` and add your key from [football-data.org](https://www.football-data.org/client/register):

```
FOOTBALL_API_KEY=your-actual-api-key
```

### 2. Run the Application

```bash
# Load environment and run
source .env && export FOOTBALL_API_KEY && mvn spring-boot:run

# Or run directly with key
FOOTBALL_API_KEY=your-key mvn spring-boot:run
```

### 3. Access the App

Open http://localhost:8080

## Features

- **Rankings Dashboard**: View worldwide club rankings with search/filter
- **UEFA Draw Simulator**: Simulate Champions/Europa/Conference League draws
- **API Integration**: Live updates from football-data.org with club crests

## Running Tests

```bash
# All tests
mvn test

# Unit tests only
mvn test -Dtest="com.football.unit.**"

# Integration tests
mvn test -Dtest="com.football.integration.**"

# E2E tests
mvn test -Dtest="com.football.e2e.**"
```

## Docker

### Build and Run

```bash
# With docker-compose (uses .env file)
docker-compose up -d

# Or build manually
docker build -t football-java .
docker run -p 8080:8080 -e FOOTBALL_API_KEY=your-key football-java
```

### Stop

```bash
docker-compose down
```

## SonarQube (Code Quality Analysis)

### Start SonarQube

```bash
# Start SonarQube + PostgreSQL
docker-compose -f docker-compose-sonar.yml up -d

# Wait for startup (1-2 minutes first time)
# Access: http://localhost:9000
# Default login: admin/admin (change on first login)
```

### Run Analysis

```bash
# Run tests with coverage
mvn clean test jacoco:report

# Analyze code
mvn sonar:sonar -Dsonar.login=admin -Dsonar.password=your-new-password

# View results at: http://localhost:9000/dashboard?id=football-java
```

### Stop SonarQube

```bash
docker-compose -f docker-compose-sonar.yml down

# Remove all data
docker-compose -f docker-compose-sonar.yml down -v
```

## Tech Stack

- Java 21
- Spring Boot 3.2
- Spring Data JPA (H2/PostgreSQL)
- SpringDoc OpenAPI (Swagger)
- Bootstrap 5
- Thymeleaf
- JUnit 5 + AssertJ
- WireMock (integration tests)
- Docker
