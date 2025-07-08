package microservices.book.multiplication.configuration;

import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import io.lettuce.core.resource.ClientResources;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer(
            ObservationRegistry observationRegistry) {
        return builder -> {
            ClientResources clientResources = ClientResources.builder()
                    .tracing(new MicrometerTracing(observationRegistry, "multiplication"))
                    .build();
            builder.clientResources(clientResources);
        };
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        //for redis stream ObjectRecord
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
