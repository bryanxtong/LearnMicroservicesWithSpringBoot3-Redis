package microservices.book.gamification.configuration;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer(
            ObservationRegistry observationRegistry) {
        return builder -> {
            ClientResources clientResources = ClientResources.builder()
                    .tracing(new MicrometerTracing(observationRegistry, "gamification"))
                    .build();
            builder.clientResources(clientResources);
        };
    }
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        //for redis stream object record
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}