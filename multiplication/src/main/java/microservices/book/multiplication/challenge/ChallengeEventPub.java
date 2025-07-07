package microservices.book.multiplication.challenge;

import microservices.book.event.challenge.ChallengeSolvedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Write ChallengeSolvedEvent to redis stream with key attempts
 */
@Service
public class ChallengeEventPub {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String streamKey;

    public ChallengeEventPub(final  RedisTemplate<String, Object> redisTemplate,
                             @Value("${redis.attempts.streamkey}")
                             final String streamKey) {
        this.redisTemplate = redisTemplate;
        this.streamKey = streamKey;
    }

    public void challengeSolved(final ChallengeAttempt challengeAttempt) {
        ChallengeSolvedEvent event = buildEvent(challengeAttempt);
        ObjectRecord<String, ChallengeSolvedEvent> objectRecord = StreamRecords.objectBacked(event).withStreamKey(streamKey);
        redisTemplate.opsForStream().add(objectRecord);
    }

    private ChallengeSolvedEvent buildEvent(final ChallengeAttempt attempt) {
        return new ChallengeSolvedEvent(attempt.getId(),
                attempt.isCorrect(), attempt.getFactorA(),
                attempt.getFactorB(), attempt.getUser().getId(),
                attempt.getUser().getAlias());
    }
}