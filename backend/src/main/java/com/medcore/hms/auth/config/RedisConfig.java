package com.medcore.hms.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for the Auth module.
 *
 * <p>Configures a {@link RedisTemplate} that uses {@link StringRedisSerializer} for both
 * keys and values. Refresh tokens are stored as:
 * <pre>
 *   KEY:   "refreshToken::{uuid}"
 *   VALUE: "{user-email}"
 * </pre>
 * Using string serialization keeps Redis entries human-readable and avoids Java
 * serialization issues across deployments.
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Use String serializer for both keys and values
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
