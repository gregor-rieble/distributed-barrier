# Distributed Barrier

A small Spring Boot application that exposes a distributed synchronization barrier over HTTP.

`distributed-barrier` lets multiple independent clients wait until a configured number of parties have reached the same
point. Once all required parties have arrived, the barrier is released and all waiting requests complete.

This can be useful for demos, integration tests, orchestration experiments, distributed system exercises, or any
situation where several processes need to rendezvous before continuing.

## Table of Contents

<!-- @formatter:off -->
<!-- TOC -->
* [Distributed Barrier](#distributed-barrier)
  * [Table of Contents](#table-of-contents)
  * [Features](#features)
  * [How It Works](#how-it-works)
  * [Requirements](#requirements)
  * [Getting Started](#getting-started)
    * [Clone the Repository](#clone-the-repository)
    * [Build the Application](#build-the-application)
    * [Run Locally](#run-locally)
  * [Usage](#usage)
    * [Wait at the Barrier](#wait-at-the-barrier)
    * [Wait with a Custom Timeout](#wait-with-a-custom-timeout)
  * [Example](#example)
  * [Configuration](#configuration)
    * [Configuration Properties](#configuration-properties)
  * [HTTP API](#http-api)
    * [`GET /barrier/await`](#get-barrierawait)
      * [Query Parameters](#query-parameters)
      * [Responses](#responses)
  * [Running with Docker](#running-with-docker)
    * [Build the Image](#build-the-image)
    * [Run the Container](#run-the-container)
    * [Run with Configuration Overrides](#run-with-configuration-overrides)
  * [Actuator](#actuator)
  * [Development](#development)
    * [Run from Source](#run-from-source)
    * [Run Tests](#run-tests)
    * [Package without Tests](#package-without-tests)
  * [Notes](#notes)
  * [License](#license)
<!-- TOC -->
<!-- @formatter:on -->

## Features

- HTTP-based barrier endpoint
- Configurable number of required parties
- Per-request timeout support
- Optional automatic shutdown after a configured number of successful barrier completions
- Spring Boot Actuator support
- Docker-friendly build and runtime setup
- Built with Java 25 and Spring Boot 4

## How It Works

Clients call the barrier endpoint and block until enough parties have arrived.

For example, if the barrier is configured for `3` parties:

1. Client A calls the endpoint and waits.
2. Client B calls the endpoint and waits.
3. Client C calls the endpoint.
4. The barrier completes.
5. All three requests return successfully.
6. The barrier is recreated for the next group of parties.

If a client times out before the barrier completes, the barrier is broken and the remaining waiting clients receive an
error.

## Requirements

- Java 25
- Maven 3.9+
- Optional: Docker

## Getting Started

### Clone the Repository

```bash
git clone <repository-url> cd distributed-barrier
```

### Build the Application

```bash
mvn clean package
```

### Run Locally

```bash
java -jar target/distributed-barrier-1.0-SNAPSHOT.jar
```

The application starts on the default Spring Boot port:

```text
http://localhost:8080
```

## Usage

### Wait at the Barrier

```bash
curl http://localhost:8080/barrier/await
```

The request will wait until the configured number of parties has called the endpoint.

### Wait with a Custom Timeout

```bash
curl "http://localhost:8080/barrier/await?timeoutSeconds=30"
```

If the barrier does not complete within the given timeout, the request fails with a timeout response.

## Example

Assume the barrier is configured for `2` parties.

Open two terminals.

Terminal 1:

```bash
curl http://localhost:8080/barrier/await
```

This request waits.

Terminal 2:

```bash
curl http://localhost:8080/barrier/await
```

Once the second request arrives, both requests complete.

## Configuration

The application can be configured using Spring Boot configuration, for example in `application.yaml`.

Common configuration options include:

```yaml
barrier:
    num-parties: 2
    default-timeout-seconds: 60
    shutdown-after-completions: 0
```

### Configuration Properties

| Property                             | Description                                                                                                     | Example |
|--------------------------------------|-----------------------------------------------------------------------------------------------------------------|---------|
| `barrier.num-parties`                | Number of parties required to complete the barrier                                                              | `2`     |
| `barrier.default-timeout-seconds`    | Default timeout used when no request-specific timeout is provided                                               | `60`    |
| `barrier.shutdown-after-completions` | Number of successful completions after which the application shuts down. Use `0` to disable automatic shutdown. | `0`     |

## HTTP API

### `GET /barrier/await`

Waits until the configured number of parties has reached the barrier.

#### Query Parameters

| Name             | Required | Description                                    |
|------------------|----------|------------------------------------------------|
| `timeoutSeconds` | No       | Overrides the default timeout for this request |

#### Responses

| Status                      | Meaning                                                                |
|-----------------------------|------------------------------------------------------------------------|
| `200 OK`                    | Barrier completed successfully                                         |
| `409 Conflict`              | The barrier was broken, likely because another party left or timed out |
| `410 Gone`                  | The application is shutting down                                       |
| `503 Service Unavailable`   | Waiting for the barrier timed out                                      |
| `500 Internal Server Error` | Unexpected barrier failure                                             |

## Running with Docker

### Build the Image

```bash
docker build -t distributed-barrier .
```

### Run the Container

```bash
docker run --rm -p 8080:8080 distributed-barrier
```

The application is then available at:

```text
http://localhost:8080
```

### Run with Configuration Overrides

Spring Boot properties can be overridden with environment variables:

```bash
docker run --rm -p 8080:8080 \
  -e BARRIER_NUM_PARTIES=3 \
  -e BARRIER_DEFAULT_TIMEOUT_SECONDS=30 \
  -e BARRIER_SHUTDOWN_AFTER_COMPLETIONS=5 \
    distributed-barrier
```

On Windows PowerShell:

```powershell
docker run --rm -p 8080:8080 `
    -e BARRIER_NUM_PARTIES=3 `
    -e BARRIER_DEFAULT_TIMEOUT_SECONDS=30 `
    -e BARRIER_SHUTDOWN_AFTER_COMPLETIONS=5 `
        distributed-barrier
```

## Actuator

Spring Boot Actuator is included. Depending on your configuration, actuator endpoints

```text
/actuator
```

For example:

```bash
curl http://localhost:8080/actuator/health
```

## Development

### Run from Source

```bash
mvn spring-boot:run
```

### Run Tests

```bash
mvn test
```

### Package without Tests

```bash
mvn clean package -DskipTests
```

## Notes

This project uses an in-memory barrier. It coordinates callers within a single running application instance.

If you run multiple instances of this application behind a load balancer, each instance has its own independent barrier.
For a truly shared distributed barrier across multiple application instances, you would need an external coordination
backend such as a database, Redis, ZooKeeper, etcd, or another consensus/coordination system.

## License

This project is currently provided without an explicit license.

If you intend others to use, modify, or distribute it, consider adding a license such as MIT, Apache License 2.0, or
GPL.
