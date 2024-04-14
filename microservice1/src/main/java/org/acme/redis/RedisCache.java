package org.acme.redis;

import io.quarkus.redis.datasource.value.ValueCommands;
import io.quarkus.redis.datasource.RedisDataSource;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RedisCache {

    private final ValueCommands<String, String> redisCommands;

    public RedisCache(RedisDataSource ds) {
        redisCommands = ds.value(String.class);
    }

    public void setex(String key, String value) {
        redisCommands.setex(key, 30, value);
    }

    public String get(String key) {
        return redisCommands.get(key);
    }
}
