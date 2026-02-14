# Distributed Chat System - Performance Client

## Project Overview
This project is a multithreaded Java WebSocket client designed to stress-test a distributed chat server deployed on **AWS EC2 (t2.micro)**. The client evaluates system stability, throughput, and latency under varying concurrency levels, specifically focusing on the challenges of WebSocket broadcasting on single-core cloud instances.

---

## Key Features
* **Rate Limiting**: Implemented `Thread.sleep(10)` to ensure 100% message delivery success and prevent server-side buffer overflow.
* **Connection Staggering**: Introduced a 200ms delay between thread startups to mitigate CPU spikes during the WebSocket handshake phase.
* **Decoupled Architecture**: Utilized a Producer-Consumer pattern with `LinkedBlockingQueue` for efficient message handling.
* **Data Collection**: Comprehensive logging of timestamps, latencies, and success status to `metrics.csv`.

---

## Prerequisites
* **Java**: JDK 17 or higher.
* **Maven**: For dependency management and building.
* **Python 3**: For running the performance analysis scripts (requires `pandas` and `matplotlib`).

---

## Getting Started

### 1. Build the Project
Navigate to the project root and run:
```bash
mvn clean install
