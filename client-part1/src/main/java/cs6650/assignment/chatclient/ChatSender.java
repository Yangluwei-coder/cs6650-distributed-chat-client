package cs6650.assignment.chatclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger; // 必须导入

@ClientEndpoint
public class ChatSender implements Runnable {
    private final String serverUri;
    private final BlockingQueue<ChatMessage> queue;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Part 1 counter
    private final AtomicInteger successCount;
    private final AtomicInteger failCount;

    public ChatSender(String serverUri, BlockingQueue<ChatMessage> queue,
                      AtomicInteger successCount, AtomicInteger failCount) {
        this.serverUri = serverUri;
        this.queue = queue;
        this.successCount = successCount;
        this.failCount = failCount;
    }

    @Override
    public void run() {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        try (Session session = container.connectToServer(this, new URI(serverUri))) {
            session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    // avoid IllegalStateException
                }
            });
            while (true) {
                // if queue is empty 3 seconds, all messages are sent
                ChatMessage msg = queue.poll(3, TimeUnit.SECONDS);
                if (msg == null) break;

                try {
                    String payload = objectMapper.writeValueAsString(msg);
                    session.getBasicRemote().sendText(payload);

                    // add when succeed
                    successCount.incrementAndGet();

                } catch (Exception e) {
                    // fail, add fail count
                    failCount.incrementAndGet();
                    System.err.println("Send Error: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Connection Error: " + e.getMessage());
        }
    }
}