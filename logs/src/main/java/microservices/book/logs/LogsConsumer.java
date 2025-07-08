package microservices.book.logs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Read the logs with lettuce
 */
@Service
@Slf4j
public class LogsConsumer implements SmartLifecycle {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private static final String KEY = "logs";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private ObjectMapper objectMapper = new ObjectMapper();

    public void logsConsumer() {
        while (running.get()) {
            try {
                List<byte[]> list = stringRedisTemplate.execute((RedisCallback<List<byte[]>>) connection -> connection.listCommands().bLPop(5, KEY.getBytes()));
                if (null != list && list.size() == 2) {
                    LogEvent logEvent = objectMapper.readValue(list.get(1), LogEvent.class);
                    Marker marker = MarkerFactory.getMarker(logEvent.getSource());
                    String level = logEvent.getLevel();
                    switch (level) {
                        case "INFO" -> log.info(marker, logEvent.toString());
                        case "ERROR" -> log.error(marker, logEvent.toString());
                        case "WARN" -> log.warn(marker, logEvent.toString());
                    }
                } else {
                    if (!running.get()) {
                        break;
                    }
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            executor.execute(this::logsConsumer);
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executor.shutdownNow();
            }
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}
