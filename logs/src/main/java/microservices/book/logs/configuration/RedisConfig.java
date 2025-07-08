package microservices.book.logs.configuration;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer(
            ObservationRegistry observationRegistry) {
        return builder -> {
            ClientResources clientResources = ClientResources.builder()
                    .tracing(new MicrometerTracing(observationRegistry, "logs"))
                    .build();
            builder.clientResources(clientResources);
        };
    }
}
