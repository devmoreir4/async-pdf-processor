# Spring RabbitMQ Pub/Sub

A Spring Boot project demonstrating asynchronous messaging with **RabbitMQ**, split into a publisher service that sends JSON messages to a queue and a subscriber service that consumes and processes them in the background.

## Tech Stack

- **Java 21**: Language
- **Spring Boot 4.0.1**: Application framework
- **Spring AMQP**: RabbitMQ integration
- **Spring Web MVC**: REST endpoints
- **Jakarta Validation**: Request validation
- **Jackson**: JSON serialization
- **Lombok**: Boilerplate reduction
- **SLF4J / Logback**: Application logging
- **RabbitMQ 3 Management**: Message broker and management UI
- **Docker Compose**: Local RabbitMQ environment

## Prerequisites

- Java 21+
- Maven 3.6+
- Docker & Docker Compose

## Getting Started

Start RabbitMQ:

```bash
docker-compose up -d
```

RabbitMQ runs with:

- **Management UI:** http://localhost:8080
- **AMQP:** `localhost:5672`
- **Credentials:** `guest` / `guest`

Run the publisher service:

```bash
cd publisher
./mvnw spring-boot:run
```

The publisher starts on port **8081**.

Run the subscriber service in another terminal:

```bash
cd subscriber
./mvnw spring-boot:run
```

The subscriber starts on port **8082**.

## Build

```bash
cd publisher
./mvnw clean install

cd ../subscriber
./mvnw clean install
```

## API

### Publish Message

**`POST /publish/json`**

Publishes a JSON message to the RabbitMQ queue. The subscriber consumes the message asynchronously.

**Request body:**

```json
{
  "content": "Hello World",
  "sender": "Carlos"
}
```

**Responses:**

| Status                      | Description                                  |
| --------------------------- | -------------------------------------------- |
| `200 OK`                    | Message published successfully               |
| `400 Bad Request`           | Validation error (missing or invalid fields) |
| `500 Internal Server Error` | Unexpected error                             |

**Example (curl):**

```bash
curl -X POST http://localhost:8081/publish/json \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Hello World",
    "sender": "Carlos"
  }'
```

### Simulate Failure

Send a message with `content` set to `fail` to trigger the subscriber retry flow and send the message to the dead letter queue after the configured attempts.

```bash
curl -X POST http://localhost:8081/publish/json \
  -H "Content-Type: application/json" \
  -d '{
    "content": "fail",
    "sender": "Carlos"
  }'
```

## Health Endpoints

- **`GET http://localhost:8081/health`**: Publisher health status
- **`GET http://localhost:8082/health`**: Subscriber health status

## RabbitMQ Queues

- **`task_queue`**: Main queue
- **`task_queue.dlq`**: Dead letter queue

The subscriber retries failed messages up to **3 attempts** with exponential backoff before rejecting them to the DLQ.

## Running Tests

```bash
cd publisher
./mvnw test

cd ../subscriber
./mvnw test
```

## License

MIT
