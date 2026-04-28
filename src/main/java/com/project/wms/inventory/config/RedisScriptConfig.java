package com.project.wms.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisScriptConfig {

    @Bean
    public DefaultRedisScript<Long> decreaseStockScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // Trỏ đến file trong resources/scripts/
        script.setLocation(new ClassPathResource("scripts/decrease_stock.lua"));
        // Khai báo kiểu trả về là Long (khớp với decrby của Redis)
        script.setResultType(Long.class);
        return script;
    }

    @Bean
    public DefaultRedisScript<Long> increaseStockScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // Trỏ đến file trong resources/scripts/
        script.setLocation(new ClassPathResource("scripts/increase_stock.lua"));
        // Khai báo kiểu trả về là Long (khớp với incrby của Redis)
        script.setResultType(Long.class);
        return script;
    }
}