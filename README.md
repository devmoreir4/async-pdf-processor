# Spring Boot RabbitMQ Pub/Sub

A simple demonstration of asynchronous messaging using Spring Boot and RabbitMQ, featuring patterns like Dead Letter Queues (DLQ) and JSON serialization.

## Overview

This project simulates a decoupling scenario where a **Publisher** generates tasks and a **Subscriber** processes them asynchronously.

### Features
- **JSON Messaging:** Strict JSON object mapping using Jackson.
- **Reliability:** configured **Dead Letter Queue (DLQ)** for handling failed messages.
- **Load Control:** implemented **Prefetch Count = 1** for fair dispatching.
- **Dockerized:** RabbitMQ environment managed via Docker Compose.

## Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven

## Getting Started

1.  **Start RabbitMQ:**
    ```bash
    docker-compose up -d
    ```
    - **Management UI:** [http://localhost:8080](http://localhost:8080)
    - **Credentials:** `admin` / `123`

2.  **Build Modules:**
    ```bash
    mvn clean install
    ```
    *(Run this in both `publisher` and `subscriber` directories)*

3.  **Run Applications:**
    Start `PublisherApplication` and `SubscriberApplication`.

## Usage

### Publish a JSON Message
Send a POST request to the publisher:

```bash
curl -X POST -H "Content-Type: application/json" \
     -d "{\"content\":\"Hello World\", \"sender\":\"Tester\"}" \
     http://localhost:8081/publish/json
```

### Test Error Handling (DLQ)
Send a message with content "fail" to trigger an exception and move the message to the DLQ:

```bash
curl -X POST -H "Content-Type: application/json" \
     -d "{\"content\":\"fail\", \"sender\":\"Tester\"}" \
     http://localhost:8081/publish/json
```