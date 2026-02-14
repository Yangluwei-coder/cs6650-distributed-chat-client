package cs6650.assignment.chatclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@ClientEndpoint
public class ChatSender implements Runnable {
    private final String serverUri;
    private final BlockingQueue<ChatMessage> queue;
    private final MetricsCollector metrics;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatSender(String serverUri, BlockingQueue<ChatMessage> queue, MetricsCollector metrics) {
        this.serverUri = serverUri;
        this.queue = queue;
        this.metrics = metrics;
    }

    @Override
    public void run() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        boolean connected = false;
        int maxRetries = 3;
        int retryCount = 0;

        while (!connected && retryCount < maxRetries) {
            try {
                try (Session session = container.connectToServer(this, new URI(serverUri))) {
                    connected = true;

                    session.addMessageHandler(new jakarta.websocket.MessageHandler.Whole<String>() {
                        @Override
                        public void onMessage(String message) {
                        }
                    });

                    while (true) {
                        ChatMessage msg = queue.poll(3, TimeUnit.SECONDS);
                        if (msg == null) break;

                        long startTime = System.nanoTime();
                        boolean success = false;

                        try {
                            String payload = objectMapper.writeValueAsString(msg);
                            session.getBasicRemote().sendText(payload);
                            Thread.sleep(10);
                            success = true;
                        } catch (Exception e) {
                            System.err.println("Send Error: " + e.getMessage());
                        } finally {
                            long latencyNanos = System.nanoTime() - startTime;
                            metrics.record(
                                    System.currentTimeMillis(),
                                    latencyNanos,
                                    success,
                                    msg.getMessageType(),
                                    msg.getRoomId()
                            );
                        }
                    }
                }
            } catch (Exception e) {
                retryCount++;
                // count total reconnections
                metrics.recordConnection();
                System.err.println("Connection attempt " + retryCount + " failed for " + serverUri + ": " + e.getMessage());

                if (retryCount < maxRetries) {
                    try {
                        Thread.sleep(1000); // after 1 second to reconnect
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }
}