# Spring Boot RabbitMQ Pub/Sub

A simple demonstration of asynchronous messaging using Spring Boot and RabbitMQ.

## Overview

This project demonstrates a decoupling scenario where a Publisher service generates tasks and a Subscriber service processes them asynchronously. Failed messages are automatically retried with exponential backoff and moved to a Dead Letter Queue (DLQ) after exhausting retry attempts.

## Tech Stack

- Java 21
- Spring Boot 4
- Spring AMQP
- RabbitMQ
- Docker

## Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven

## Getting Started

1. **Start RabbitMQ:**

   ```bash
   docker-compose up -d
   ```

   - Management UI: http://localhost:8080
   - Credentials: `guest` / `guest`

2. **Build the modules:**

   ```bash
   cd publisher && ./mvnw clean install
   cd ../subscriber && ./mvnw clean install
   ```

3. **Run applications:**

   ```bash
   cd publisher && ./mvnw spring-boot:run
   cd ../subscriber && ./mvnw spring-boot:run
   ```

   - Publisher: http://localhost:8081
   - Subscriber: http://localhost:8082

## Usage

### Publish a Message

```bash
curl -X POST http://localhost:8081/publish/json \
  -H "Content-Type: application/json" \
  -d '{"content": "Hello World", "sender": "Tester"}'
```

### Test DLQ (Error Handling)

Send a message with content "fail" to trigger retries and move to DLQ:

```bash
curl -X POST http://localhost:8081/publish/json \
  -H "Content-Type: application/json" \
  -d '{"content": "fail", "sender": "Tester"}'
```
