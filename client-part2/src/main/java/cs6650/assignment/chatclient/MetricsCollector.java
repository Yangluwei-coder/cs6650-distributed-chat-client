package cs6650.assignment.chatclient;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class MetricsCollector {
    private final List<Double> latencies = Collections.synchronizedList(new ArrayList<>());
    private final LongAdder success = new LongAdder();
    private final LongAdder failure = new LongAdder();
    private final LongAdder connections = new LongAdder();
    private volatile boolean enabled = true;

    private final BufferedWriter writer;

    public MetricsCollector(String csvPath) throws IOException {
        File file = new File(csvPath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) parentDir.mkdirs();
        writer = new BufferedWriter(new FileWriter(file));
        writer.write("timestamp,messageType,latencyMs,status,roomId\n");
    }

    public void enable(boolean enabled) { this.enabled = enabled; }

    public void recordConnection() { connections.increment(); }

    public void record(long timestamp, long latencyNanos, boolean ok, String messageType, String roomId) {
        if (!enabled) return;
        double latencyMs = latencyNanos / 1_000_000.0;
        latencies.add(latencyMs);
        if (ok) success.increment();
        else failure.increment();

        try {
            synchronized (writer) {
                writer.write(String.format("%d,%s,%.2f,%s,%s\n",
                        timestamp, messageType, latencyMs, ok ? "OK" : "FAIL", roomId));
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Little's Law analysis
    public void printSummary(long totalRuntimeMs, int mainThreads, double predictedThroughput) throws IOException {
        writer.flush();
        writer.close();

        if (latencies.isEmpty()) return;

        List<Double> sorted = new ArrayList<>(latencies);
        Collections.sort(sorted);
        int n = sorted.size();
        double mean = sorted.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double p99 = sorted.get((int) (n * 0.99));
        double seconds = totalRuntimeMs / 1000.0;
        double actualThroughput = success.sum() / seconds;

        System.out.println("\n====== PERFORMANCE METRICS ======");
        System.out.println("Successful messages: " + success.sum());
        System.out.println("Failed messages: " + failure.sum());
        System.out.println("Total runtime (Wall Time): " + totalRuntimeMs + " ms");
        System.out.println("Mean latency: " + String.format("%.2f", mean) + " ms");
        System.out.println("P99 latency: " + String.format("%.2f", p99) + " ms");
        System.out.println("Overall throughput: " + String.format("%.2f", actualThroughput) + " msg/s");

        System.out.println("\n====== CONNECTION STATISTICS ======");
        System.out.println("Total connections attempted: " + connections.sum());
        // reconnection = all connections - initial threads
        System.out.println("Total reconnections: " + Math.max(0, connections.sum() - mainThreads));

        System.out.println("\n====== LITTLE'S LAW ANALYSIS ======");
        System.out.printf("Predicted Throughput: %.2f msg/s\n", predictedThroughput);
        System.out.printf("Actual Throughput: %.2f msg/s\n", actualThroughput);
        System.out.printf("Efficiency: %.2f%%\n", (actualThroughput / predictedThroughput) * 100);
    }
}