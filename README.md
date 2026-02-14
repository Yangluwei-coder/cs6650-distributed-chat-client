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

### 2. Run the Client
To execute the load test with 32 threads (Optimal Point), run the following command in your terminal:Bashjava -cp target/client-part2-1.0-SNAPSHOT.jar cs6650.assignment.chatclient.SimpleWebSocketClient
Note: Ensure the server IP in SimpleWebSocketClient.java is updated to your current EC2 Public IPv4 address (e.g., 54.146.230.194).
### 3. Generate Performance ChartsOnce the test completes and the results/metrics.csv file is generated, run the analysis script to visualize the performance data:Bashpython3 analyze_metrics.py
Performance Results (32 Threads)The following metrics reflect the system's stability at the identified "Optimal Point":MetricValueSuccess Rate100%Peak Throughput~49,850.07 msg/sMean Latency0.38 msP99 Latency0.22 msThroughput Stability AnalysisThe chart below illustrates the steady-state throughput of ~2,750 msg/s, aggregated in 10-second buckets.
Startup Phase: The initial ramp-up (0-30s) confirms the effectiveness of the Connection Staggering strategy in preventing CPU spikes during handshakes.Steady State: The consistent flat line (30-190s) validates that Rate Limiting (Thread.sleep(10)) maintained system equilibrium without buffer overflow.
