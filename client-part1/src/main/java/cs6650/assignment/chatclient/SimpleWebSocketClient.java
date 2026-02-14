package cs6650.assignment.chatclient;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SimpleWebSocketClient {
    private static final int TOTAL_MESSAGES = 500_000;
    private static final int WARMUP_THREADS = 32;
    private static final int MAIN_THREADS = 32;

    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failCount = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        String serverUrl = "ws://54.146.230.194:8080/chat/";

        // --- WARMUP PHASE ---
        System.out.println("Starting Warmup (32 threads, 32,000 messages)...");
        runPhase(serverUrl, WARMUP_THREADS, 32000);

        successCount.set(0);
        failCount.set(0);

        // --- MAIN PHASE ---
        System.out.println("Starting Main Phase (500,000 messages)...");
        long start = System.currentTimeMillis();

        runPhase(serverUrl, MAIN_THREADS, TOTAL_MESSAGES);

        long end = System.currentTimeMillis();
        long wallTime = end - start;

        // output
        System.out.println("========================================");
        System.out.println("1. Successful messages: " + successCount.get());
        System.out.println("2. Failed messages: " + failCount.get());
        System.out.println("3. Total runtime (ms): " + wallTime);
        System.out.println("4. Throughput (msgs/sec): " + (successCount.get() * 1000.0 / wallTime));
        System.out.println("5. Threads used: " + MAIN_THREADS);
        System.out.println("========================================");
    }

    private static void runPhase(String url, int numThreads, int numMsg) throws InterruptedException {
        BlockingQueue<ChatMessage> queue = new LinkedBlockingQueue<>(10000);
        new Thread(new MessageGenerator(queue, numMsg)).start();

        ExecutorService pool = Executors.newFixedThreadPool(numThreads);
        for (int i = 0; i < numThreads; i++) {
            pool.submit(new ChatSender(url + "room" + (i % 20), queue, successCount, failCount));
        }

        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.MINUTES);
    }
}