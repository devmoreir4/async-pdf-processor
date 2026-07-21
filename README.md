# Async PDF Processor

An asynchronous PDF processing system built with Spring Boot, RabbitMQ, and PostgreSQL.
It provides an API for non-blocking PDF uploads and a background worker that extracts
text using native PDF parsing or OCR. Processing status, retries, and dead-lettering
are persisted across the messaging and database layers.

## Tech Stack

- Java 21
- Spring Boot 4.0.1 (Web, AMQP, Data JPA)
- RabbitMQ with publisher confirms, retries, and dead-letter queues
- PostgreSQL 17 with Flyway migrations
- Apache PDFBox 3.0.5
- Tesseract OCR
- Jackson with `jackson-datatype-jsr310`
- JUnit 5, Mockito, and H2

## Prerequisites

- Linux (or WSL on Windows)
- JDK 21
- Docker and Docker Compose
- Tesseract with Portuguese and English language data (only needed to run the
  `subscriber` against scanned PDFs)

On Debian or Ubuntu:

```bash
sudo apt update
sudo apt install tesseract-ocr tesseract-ocr-por tesseract-ocr-eng
```

Verify the installation:

```bash
tesseract --version
tesseract --list-langs
```

## Environment Variables

Copy `.env.example` to `.env`, adjust the values, and export them before starting the
modules.

```bash
cp .env.example .env
set -a && source .env && set +a
```

All defaults match the credentials already configured in `docker-compose.yml`, so the
project runs out of the box with no `.env` file for local development.

## Getting Started

Start RabbitMQ and PostgreSQL:

```bash
docker compose up -d
```

RabbitMQ Management is available at <http://localhost:8080> using `guest` / `guest`.

Run the publisher:

```bash
cd publisher
./mvnw spring-boot:run
```

In another terminal, run the subscriber:

```bash
cd subscriber
./mvnw spring-boot:run
```

Both applications resolve `document.storage.root` as `../storage` by default, which
only works correctly when each module is started from its own directory as shown above.

## API Reference

### Upload a PDF

```bash
curl -i -X POST http://localhost:8081/documents \
  -F "file=@document.pdf;type=application/pdf"
```

```http
HTTP/1.1 202 Accepted
Location: http://localhost:8081/documents/7d280c7a-6191-4cd0-9da6-21a82f061ea5
```

```json
{
  "documentId": "7d280c7a-6191-4cd0-9da6-21a82f061ea5",
  "status": "QUEUED",
  "statusUrl": "http://localhost:8081/documents/7d280c7a-6191-4cd0-9da6-21a82f061ea5"
}
```

Files are accepted up to 20 MB and validated by their `%PDF-` signature, not by
filename extension or `Content-Type`.

### Check Processing Status

```bash
curl http://localhost:8081/documents/7d280c7a-6191-4cd0-9da6-21a82f061ea5
```

Possible states are `QUEUED`, `PROCESSING`, `COMPLETED`, and `FAILED`. A completed job
includes its result in `extractedText`. A failed job includes an `error` object with a
code (`INVALID_DOCUMENT` for permanent failures, `PROCESSING_FAILED` otherwise) and a
message.

### Health Checks

```bash
curl http://localhost:8081/health
curl http://localhost:8082/health
```

Both endpoints return `200 OK` while their respective applications are running.

## Messaging, Retry, and Dead Letter Queue

- The publisher waits for a broker confirmation (`waitForConfirmsOrDie`, 5s timeout)
  before accepting an upload as queued.
- The subscriber processes messages with `prefetchCount=1`, since OCR is CPU-intensive
  and shouldn't be parallelized ahead of capacity.
- Failures are classified as **permanent** (invalid, corrupted, or encrypted PDFs; a
  missing/misconfigured Tesseract binary) or **transient** (temporarily unavailable
  shared storage, OCR timeouts, non-zero Tesseract exit codes).
- Transient failures are retried up to two additional times, with an exponential
  backoff starting at 2 seconds and capped at 10 seconds. Permanent failures skip
  retries entirely.
- Once retries are exhausted (or immediately for permanent failures), the job is marked
  `FAILED` in PostgreSQL and the message is routed to `pdf.processing.dlq` via the
  queue's dead letter exchange.

## Project Structure

```
publisher/   Spring Boot service: upload API, storage, job persistence, message publishing
subscriber/  Spring Boot service: message consumption, PDFBox/Tesseract extraction, retry & DLQ recovery
storage/     Shared local directory for uploaded PDFs (git-ignored, kept via .gitkeep)
docker-compose.yml   RabbitMQ + PostgreSQL for local development
.env.example         Template for the environment variables described above
```

Each module includes the Flyway migration for the shared `document_jobs` table, allowing
either service to initialize and validate the schema.

## Testing

Ensure Maven is running with JDK 21:

```bash
java -version
```

Run both test suites:

```bash
cd publisher
./mvnw test

cd ../subscriber
./mvnw test
```

Tests use an in-memory H2 database (PostgreSQL compatibility mode) and disable the
RabbitMQ listener auto-startup, so neither PostgreSQL, RabbitMQ, nor Tesseract need to
be running to execute them.

## Stopping the Infrastructure

```bash
docker compose down
```

To also remove the PostgreSQL volume:

```bash
docker compose down -v
```

## License

MIT
