package microservices.book.gamification.game;

import microservices.book.event.challenge.ChallengeSolvedEvent;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@RequiredArgsConstructor
@Slf4j
@Service
public class GameEventHandler implements StreamListener<String, ObjectRecord<String, ChallengeSolvedEvent>>, InitializingBean, DisposableBean {

    private final GameService gameService;

    private StreamMessageListenerContainer<String, ObjectRecord<String, ChallengeSolvedEvent>>  listenerContainer;
    private Subscription subscription;
    private final RedisTemplate<String, Object> redisTemplate;
    @Value("${redis.attempts.streamkey}")
    private String STREAM_KEY = "attempts";
    @Value("${redis.attempts.consumer.group}")
    private String CONSUNMER_GROUP = "attempts-consumer-group";
    @Value("${redis.attempts.consumer.name}")
    private String CONSUMERR_NAME = "attempts.consumer";

    @Override
    public void onMessage(ObjectRecord<String, ChallengeSolvedEvent> message) {
        log.info("Challenge Solved Event received: {}", message.getValue().getAttemptId());
        try {
            gameService.newAttemptForUser(message.getValue());
            redisTemplate.opsForStream().acknowledge(STREAM_KEY,CONSUNMER_GROUP, message.getId());
        } catch (final Exception e) {
            log.error("Error when trying to process ChallengeSolvedEvent", e);
        }
    }
    @Override
    public void destroy() throws Exception {
        if(this.subscription != null) {
            subscription.cancel();
        }

        if(this.listenerContainer != null) {
            listenerContainer.stop();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, ObjectRecord<String, ChallengeSolvedEvent>> options = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .builder()
                .batchSize(10)
                .pollTimeout(Duration.ofSeconds(1))
                .hashKeySerializer(new StringRedisSerializer())
                .hashValueSerializer(new StringRedisSerializer())
                .targetType(ChallengeSolvedEvent.class)
                .build();

        this.listenerContainer = StreamMessageListenerContainer.create(redisTemplate.getConnectionFactory(), options);
        try{
            if(!redisTemplate.hasKey(STREAM_KEY)){
                redisTemplate.getConnectionFactory().getConnection().streamCommands().xGroupCreate(STREAM_KEY.getBytes(),
                        CONSUNMER_GROUP,
                        ReadOffset.latest(),
                        true
                );
            }
        } catch (RedisSystemException e) {
            System.err.println(e.getCause().getMessage());
        }
        this.subscription = listenerContainer.receive(Consumer.from(CONSUNMER_GROUP, CONSUMERR_NAME), StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()), this);
        listenerContainer.start();
    }
}