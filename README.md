# Spring Boot RabbitMQ Pub/Sub

A simple demonstration of asynchronous messaging using Spring Boot and RabbitMQ.

## Overview

This project simulates a decoupling scenario where a **Publisher** generates tasks and a **Subscriber** processes them asynchronously.

### Features

- **Dead Letter Queue (DLQ):** Failed messages are moved to a DLQ after retry attempts
- **Retry with Backoff:** Exponential backoff retry (3 attempts, 2s initial, 2x multiplier)
- **Prefetch Count = 1:** Fair dispatching for load balancing
- **Dockerized:** RabbitMQ environment managed via Docker Compose

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

2. **Build Modules:**

   ```bash
   cd publisher && ./mvnw clean install
   cd ../subscriber && ./mvnw clean install
   ```

3. **Run Applications:**
   - Start `PublisherApplication` (port 8081)
   - Start `SubscriberApplication` (port 8082)

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
