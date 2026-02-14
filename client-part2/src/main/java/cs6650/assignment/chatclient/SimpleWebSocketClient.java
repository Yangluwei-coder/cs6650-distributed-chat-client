package cs6650.assignment.chatclient;

import java.util.concurrent.*;

public class SimpleWebSocketClient {
    private static final int TOTAL_MESSAGES = 500_000;
    private static final int WARMUP_THREADS = 32;
    private static final int MAIN_THREADS = 32;

    public static void main(String[] args) throws Exception {
        MetricsCollector metrics = new MetricsCollector("results/metrics.csv");

        // --- Little's Law Prediction ---
        double measuredMeanLatencySec = 0.00038;
        double predictedThroughput = MAIN_THREADS / measuredMeanLatencySec;

        // --- WARMUP PHASE ---
        System.out.println("Starting Warmup...");
        runPhase(metrics, WARMUP_THREADS, 32000);
        System.out.println("Warmup Complete.");

        System.out.println("Cooling down for 5 seconds...");
        Thread.sleep(5000);

        // --- MAIN PHASE ---
        System.out.println("Starting Main Phase...");
        long start = System.currentTimeMillis();
        metrics.enable(true);
        runPhase(metrics, MAIN_THREADS, TOTAL_MESSAGES);
        long end = System.currentTimeMillis();

        metrics.printSummary(end - start, MAIN_THREADS, predictedThroughput);
    }

    private static void runPhase(MetricsCollector metrics, int numThreads, int numMsg) throws InterruptedException {
        BlockingQueue<ChatMessage> queue = new LinkedBlockingQueue<>(10000);
        new Thread(new MessageGenerator(queue, numMsg)).start();

        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            // record reconnection
            metrics.recordConnection();
            pool.submit(new ChatSender("ws://54.146.230.194:8080/chat/room" + (i % 20), queue, metrics));
            try {
                Thread.sleep(200); // reduce handshake pressure
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.MINUTES);
    }
}