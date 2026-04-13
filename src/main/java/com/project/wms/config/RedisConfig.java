package com.project.wms.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RedisConfig {

    // Dùng redis 1 cách thủ công
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connection) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(connection);

        // Tạo serializer cho Value (Object)
        Jackson2JsonRedisSerializer<Object> serializer = commonSerializer();

        // 2. Cấu hình Serializer cho Key và Value
        // Key dùng String cho dễ đọc
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value dùng cho Json
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;

    }

    // Cấu hình ko cần quá phức tạp có thể dùng @EnableCaching @Cacheable
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<Object> serializer = commonSerializer();

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * Tách riêng Serializer để dùng chung cho cả RedisTemplate và CacheManager
     * Giúp đảm bảo dữ liệu lưu xuống luôn đồng nhất định dạng JSON
     */
    private Jackson2JsonRedisSerializer<Object> commonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // Quan trọng: Giúp map đúng kiểu dữ liệu khi lấy ra (deserialize)
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL);

        return new Jackson2JsonRedisSerializer<>(mapper, Object.class);
    }

}
